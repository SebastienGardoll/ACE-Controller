package fr.gardoll.ace.controller.autosampler;

import java.io.Closeable ;
import java.io.IOException ;

import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.com.JSerialComm ;
import fr.gardoll.ace.controller.com.SerialComException ;
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.Log ;
import fr.gardoll.ace.controller.core.ThreadControl ;
import fr.gardoll.ace.controller.settings.ConfigurationException ;
import fr.gardoll.ace.controller.settings.GeneralSettings ;

public class Passeur implements Closeable
{
  public final static int NB_POSITION = 6 ;
  
  private final static double RAPPORT_REDUCTEUR_MOTEUR = 40. ; //DEPENDANT DE LA MECANIQUE
  private final static double NB_PAS_TOUR_BRAS = 400. ; // en demi pas
  private final static double NB_PAS_TOUR_MOTEUR = 400. ;//demi pas sans réducteur
  private final static double NB_PAS_TOUR_CARROUSEL = RAPPORT_REDUCTEUR_MOTEUR * NB_PAS_TOUR_MOTEUR  ; //demi pas
  private final static double HAUTEUR_TOUR_BRAS = 3. ; // en mm
  
  // Coefficient pour la distance plateau porteur tuyau pour la poubelle
  private static final double _COEFF = 2.5 ;

  private final static int VIB_ID = 2 ; //numéro du signal utilisé pour faire vibrer le bras
  private final static long VIBRATION_TEMPS = 500l ; //temps en ms de vibration
  private final static int TRASH_POSITION = 0;
  
  private static final Logger _LOG = Log.HIGH_LEVEL;
  
  private int x = 0; //nombre de demi pas pour l'axe carrousel

  private int y = 0; //nombre de demi pas pour l'axe bras

  private int sav_x = 0; //position du carrousel enregistrée est recalculé si appel setOrigineXX

  private int sav_y = 0; //position du bras enregistrée est recalculé si appel setOrigineXX

  private boolean sav_butee = false;//flag de butee enregistré

  private boolean _pause = false;

  //ce flag sert en cas de pause à savoir si le bras devait
  // s'arrêter à une butée ( donc pas de x ou y précisé ) ou non
  private boolean _butee = false; 
  
  private final MotorController interfaceMoteur;
  
  /* ne pas inclure d'attente de fin de mouvement dans les procédures de mouvement
  car incompatible avec le système de pause */ 

  // Les arguments donnée aux méthodes ne sont pas vérifiés car grâce aux butée de
  // fin de course, la sécurité est assurée.

  //require nbPasCarrousel > 0
  //require diametre > 0
  public Passeur(MotorController interfaceMoteur)
      throws InitializationException, ConfigurationException
  {
    _LOG.debug("initializing the autosampler");
    
    GeneralSettings settings = GeneralSettings.instance();
    
    int diametre = settings.getDiametreCarrousel();
    int nbPasCarrousel = settings.getNbPasCarrousel();
    
    this.interfaceMoteur = interfaceMoteur;
    
    this.reset() ;
    this.setModeDirect();

    if (nbPasCarrousel <= 0 || diametre  <= 0)
    {
      String msg = String.format("the value of the diameter '%s' and the steps '%s' of the step motor cannot be negative or null",
          diametre, nbPasCarrousel);
      throw new InitializationException(msg);
    }
  }
  
  @Override
  public void close() throws IOException
  {
    _LOG.debug("closing the autosampler");
    this.interfaceMoteur.close();
  }
  
  public boolean isPaused () { return _pause; } ;
  
  public void moveCarrousel(int position)
  {
    this.moveCarrousel(position, 0);
  }
  
  // le numéro de la position. 0 => poubelle
  // modificateur => ajout d'un nombre de demi pas
  // modificateur ou en nombre de pas si position = 0
  public void moveCarrousel(int position, int modificateur) throws ConfigurationException
  {
    _LOG.debug(String.format("move the carousel to the position %s with %s offset",
        position, modificateur));
    this.x = position * GeneralSettings.instance().getNbPasCarrousel() + modificateur ;
    
    try
    {
      this.interfaceMoteur.move(this.x , this.y);
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while moving the autosampler carousel to position '%s' with offset '%s'",
          position, modificateur);
      throw new RuntimeException(msg, e);
    }
  }

  //en nombre de demi pas
  public void moveBras(int nbPas)
  {
    _LOG.debug(String.format("move the arm to the position %s", nbPas));
    this.y = nbPas ;
    
    try
    {
      this.interfaceMoteur.move(this.x, this.y);
    }
    catch (SerialComException e)
    {
      String msg = String.format("error while moving the autosampler arm to position '%s'",
          nbPas);
      throw new RuntimeException(msg, e);
    }
  }
  
  // effectue un mouvement simultané
  // fait car problème de temps de réponse car halt fait par interface
  public void moveCarrouselEtBras (int position, int nbPas) throws ConfigurationException
  {
    _LOG.debug(String.format("move the carousel to position %s and arm to position %s",
        position, nbPas));
    this.x = position * GeneralSettings.instance().getNbPasCarrousel() ;
    this.y = nbPas ;
    try
    {
      this.interfaceMoteur.move(this.x , this.y);
    }
    catch (SerialComException e)
    {
      String msg = String.format("error while moving the autosampler arm to position '%s' steps and carousel to position '%s'",
          nbPas, position);
      throw new RuntimeException(msg, e);
    }
  }

  //le bras revient à son origine
  public void moveOrigineBras()
  {
    try
    {
      _LOG.debug("move the arm to its origin");
      this.interfaceMoteur.move(this.x, 0);
      this.y = 0 ;
    }
    catch(SerialComException e)
    {
      String msg = "error while moving autosampler arm to base";
      throw new RuntimeException(msg, e);
    }
  }

  //avance le bras jusqu'à la butée haute sans setOrigineBras
  public void moveButeBras()
  {  
    try
    {
      _LOG.debug("move the arm to the top");
      this.interfaceMoteur.movel(0, 1);
      this._butee = true ;
    }
    catch(SerialComException e)
    {
      String msg = "error while moving autosampler arm to top";
      throw new RuntimeException(msg, e);
    }
  }
  
  public void moveArmToTrash() throws ConfigurationException
  {
    _LOG.debug("move the arm to trash");
    
    _LOG.debug("first reference the arm from the top");
    this.moveButeBras();
    this.finMoveBras();
    this.setOrigineBras();
    
    _LOG.debug("then get the arm to the trash");
    
    GeneralSettings settings = GeneralSettings.instance();
    int carouselThickness = settings.getEpaisseur();
    int refCarousel = settings.getRefCarrousel();
    
    int nbSteps = Passeur.convertBras(Passeur._COEFF * carouselThickness
        - refCarousel);
    this.moveBras(nbSteps) ;
  }
  
  //attente de la fin de mouvement
  public void finMoveCarrousel()
  {
    _LOG.debug("waiting for the carousel");
    boolean isMoving = true;
    do
    { 
      ThreadControl.check();
      
      try
      {
        Thread.sleep(100) ;
      }
      catch (InterruptedException e)
      {
        throw new RuntimeException(e);
      }
      
      ThreadControl.check();
      
      try
      {
        isMoving = this.interfaceMoteur.moving(TypeAxe.carrousel);
      }
      catch (SerialComException e)
      {
        String msg = "error while waiting for the end of the autosampler carousel move";
        throw new RuntimeException(msg, e);
      }
    }
    while (isMoving) ;
    
    _LOG.debug("carousel reached the position");
    
    ThreadControl.check();
  }

  //attente de la fin de mouvement
  public void finMoveBras()
  {
    try
    {
      _LOG.debug("waiting for the arm");
      boolean isMoving = true;
      do
      { 
        ThreadControl.check();
        
        try
        {
          Thread.sleep (100) ;
        }
        catch (InterruptedException e)
        {
          throw new RuntimeException(e);
        }
        
        ThreadControl.check();
        
        isMoving = this.interfaceMoteur.moving(TypeAxe.bras);
      }
      while (isMoving) ;

      //uniquement pour le bras, utile pour moveCarrouselRelatif
      if (this._butee)
      { 
        //la position du bras en fin de butée est actualisée
        //si occurence d'une pause jusqu'à fin de move du bras
        this.y = this.interfaceMoteur.where(TypeAxe.bras) ; 
        this._butee = false ;               
      }
    }
    catch(SerialComException e)
    {
      String msg = "error while waiting the end of the autosampler arm move";
      throw new RuntimeException(msg, e);
    }
    
    _LOG.debug("arm reached the position");
    
    ThreadControl.check();
  }
  
  //envoie la commande new à l'interface.
  public void reset()
  { 
    try
    {
      _LOG.debug("resetting the autosampler interface");
      this.interfaceMoteur.reset();
      this.x = 0 ; this.y = 0 ;
    }
    catch (SerialComException e)
    {
      String msg = "error while resetting the autosampler interface";
      throw new RuntimeException(msg, e);
    }
  }
  
  //la position courante devient l'origine du bras <=> y = 0
  //sav_y est recalculé dans le nouveau référenciel
  public void setOrigineBras()
  { 
    try
    {
      _LOG.debug("setting the origin for the arm");
      this.interfaceMoteur.datum(TypeAxe.bras);
      this.sav_y = 0 - this.y + this.sav_y ;
      this.y = 0 ;
    }
    catch (SerialComException e)
    {
      String msg = "error while setting the arm origin";
      throw new RuntimeException(msg, e);
    }
  }

  // la position courante devient l'origine du carrousel <=> x = 0
  // sav_x est recalculé dans le nouveau référenciel
  public void setOrigineCarrousel()
  { 
    try
    {
      _LOG.debug("setting the origin for the carousel");
      this.interfaceMoteur.datum(TypeAxe.carrousel);
      this.sav_x = 0 - this.x + this.sav_x ;
      this.x = 0 ;
    }
    catch(SerialComException e)
    {
      String msg = "error while setting the carousel origin";
      throw new RuntimeException(msg, e);
    }
  }

  //sur les deux axes
  public void setOrigine()
  { 
    _LOG.debug("setting the origin for the arm and the carousel");
    this.setOrigineBras() ;
    this.setOrigineCarrousel() ;
  }
  
  // commande directe de l'interface, pas de
  // mémorisation des commandes envoyées.
  public void setModeDirect()
  {  
    try
    {
      _LOG.debug("setting the ack single line mode");
      this.interfaceMoteur.singleLine(true);
    }
    catch (SerialComException e)
    {
      String msg = "error while setting autosampler direct mode";
      throw new RuntimeException(msg, e);
    }
  }

  //permet d'utiliser le potentiomètre numérique
  public void setModeManuel()
  { 
    try
    {
      _LOG.debug("setting the autosampler manual mode");
      this.interfaceMoteur.manual() ;
    }
    catch (SerialComException e)
    {
      String msg = "error while setting autosampler manual mode";
      throw new RuntimeException(msg, e);
    }
  }
  
  //pause du passeur avec enregistrement de la position courante
  public void pause()
  {
    try
    {
      _LOG.debug("pausing the autosampler");
      if (this.interfaceMoteur.moving(TypeAxe.carrousel) ||
          this.interfaceMoteur.moving(TypeAxe.bras))
      { 
        _LOG.debug("halting the autosampler");
        this.interfaceMoteur.halt();
      }
      else
      {
        _LOG.debug("the autosampler is already halted");
      }
         
      this._pause = true ;
      this.saveCurrentPosition();
    }
    catch(SerialComException e)
    {
      String msg = "error while pausing the autosampler";
      throw new RuntimeException(msg, e);
    }
  }
  
  public void cancel()
  {
    try
    {
      _LOG.debug("cancelling the autosampler");
      if (this.interfaceMoteur.moving(TypeAxe.bras) ||
          this.interfaceMoteur.moving(TypeAxe.carrousel))
      {
        _LOG.debug("halting the autosampler");
        this.interfaceMoteur.halt();
      }
      else
      {
        _LOG.debug("the autosampler is already halted");
      }
    }
    catch(SerialComException e)
    {
      String msg = "error while cancelling the autosampler";
      throw new RuntimeException(msg, e);
    }
  }
  
  public void reinit()
  {
    _LOG.debug("reinitializing the autosampler");
    this.moveButeBras();
    this.finMoveBras();
    this.setOrigineBras() ;
    this.moveCarrousel(TRASH_POSITION);
    this.finMoveCarrousel();
  }
  
  //reprise sur pause avec retour à la position sauvegardée dans pause()
  public void reprise(boolean brasFirst)
  {  
    _LOG.debug(String.format("resuming the autosampler (with arm first: %s)", brasFirst));
    if (this._pause)
    {
      //cas où il n'y a pas eu de manip sur le passeur
      if ( (this.sav_x == this.x) && (this.sav_y == this.y) && (this.sav_butee == this._butee) )
      { 
        if (this._butee)
        {
          this.moveButeBras() ;
        }
        else
        {
          //ancien mécanisme de reprise quand la manip sur le passeur n'était pas possible en pause
          try
          {
            _LOG.debug(String.format("resume by moving to x=%s ; y=%s",
                this.x, this.y));
            this.interfaceMoteur.move(this.x, this.y) ;
          }
          catch (SerialComException e)
          {
            String msg = "error while resuming the autosampler move";
            throw new RuntimeException(msg, e);
          }
        }
      }
      else
      {
        this.returnSavedPosition(brasFirst) ; 
      }

      this._pause = false ;
    }
  }
  
  //converti une dimension exprimée en mm en nombre de demi pas pour le bras.
  //arrondi au demi pas supérieur
  //dimension en mm en rapport avec le bras !
  public static int convertBras(double dimension)
  {
    double raw = Math.ceil( (dimension * NB_PAS_TOUR_BRAS) / HAUTEUR_TOUR_BRAS ) ;
    int result = ((int) (raw));
    _LOG.debug(String.format("convert %s millimeter into %s number of arm steps",
                             dimension, result));
    return result;
  }
  
  //converti une dimension exprimée en mm en nombre de demi pas pour le carrousel.
  //arrondi au demi pas inférieur
  //dimension en mm en rapport avec au carrousel !
  public int convertCarrousel(double dimension) throws ConfigurationException
  {
    double rayon = GeneralSettings.instance().getDiametreCarrousel() / 2.  ;
    
    double raw = (Math.asin(dimension / rayon) *  NB_PAS_TOUR_CARROUSEL) / ( 2. * Math.PI) ; 
    int result = ((int) (raw));
    _LOG.debug(String.format("convert %s millimeter into %s number of carousel steps",
                              dimension, result));
    return result;
  }

  public void vibration()
  {  
    try
    {
      _LOG.debug("vibrate the arm");
      this.interfaceMoteur.out(VIB_ID, true);

      try
      {
        Thread.sleep(VIBRATION_TEMPS) ;
      }
      catch (InterruptedException e)
      {
        throw new RuntimeException(e);
      }

      this.interfaceMoteur.out(VIB_ID, false);
    }
    catch(SerialComException e)
    {
      String msg = "error while vibrating the autosampler arm";
      throw new RuntimeException(msg, e);
    }
  }
  
  //le carrousel revient à son point d'origine
  public void moveOrigineCarrousel()
  {
    try
    {
      _LOG.debug("move carousel to its origin");
      this.interfaceMoteur.move(0 , this.y);
      this.x = 0 ;
    }
    catch(SerialComException e)
    {
      String msg = "error while moving the carousel to origin";
      throw new RuntimeException(msg, e);
    }
  }
  
  //bouge de nbPosition
  //!= moveCarrousel où on précise le numéro de la position par rapport à 0 
  public void moveCarrouselRelatif(int nbPosition) throws ConfigurationException
  {
     try
     {
       _LOG.debug(String.format("move carousel to the relative position %s", nbPosition));
       this.x += nbPosition * GeneralSettings.instance().getNbPasCarrousel()  ;
       this.interfaceMoteur.move(this.x , this.y);
     }
     catch(SerialComException e)
     {
       String msg = String.format("error while moving the autosampler carousel to position '%s' (relative)",
           nbPosition);
       throw new RuntimeException(msg, e);
     }
  }
  
  //enregistre la position du bras et du carrousel
  //dans les variables save_x et save_y
  //est recalculé si appel setOrigineXX mais attention à son utilité par la suite
  //appel obligatoire si l'on doit manipuler le passeur pendant une pause
  private void saveCurrentPosition()
  {  
    _LOG.debug(String.format("save the current position: x = %s ; y = %s ; top = %s",
        this.x, this.y, this._butee));
    this.sav_x = this.x ; this.sav_y = this.y ; this.sav_butee = this._butee ;
  }

  //revient à la position enregistrée par saveCurrentPosition
  //détermine si le bras bouge avant le carrousel
  //appel obligatoire si l'on doit manipuler le passeur pendant une pause
  private void returnSavedPosition(boolean brasFirst)
  {
    _LOG.debug(String.format("return to the saved position (with bras first: %s)", brasFirst));
    
    if (brasFirst)
    {
      if (this.sav_butee)
      {
        // sav_y est inutile et dangereux en cas de moveButeBras
        moveButeBras() ; 
      }
      else
      {
        moveBras(this.sav_y) ;
      }

      finMoveBras() ;
      moveCarrousel(0, this.sav_x) ;
      finMoveCarrousel() ;
    }
    else
    {
      moveCarrousel(0, this.sav_x) ;
      finMoveCarrousel() ;
      
      if (this.sav_butee)
      {
        // sav_y est inutile et dangereux en cas de moveButeBras
        moveButeBras() ;
      }
      else
      {
        moveBras(this.sav_y) ;
      }
      
      finMoveBras() ;
    }
  }
  
  public static void main(String[] args)
  {
    String portPath = "/dev/ttyUSB0"; // To be modified.
    JSerialComm port = new JSerialComm(portPath);
    
    try(InterfaceMoteur autoSamplerInt = new InterfaceMoteur(port) ;
        Passeur autosampler = new Passeur(autoSamplerInt))
    {
      int carouselPosition = 10;
      int armNbStep = Passeur.convertBras(-50); // Convert -50 millimeter into number of steps.
      
      _LOG.info(String.format("moving carousel to position %s", carouselPosition));
      autosampler.moveCarrousel(carouselPosition);
      
      _LOG.info("waiting for the carousel");
      autosampler.finMoveCarrousel();
      
      _LOG.info(String.format("moving carousel back"));
      autosampler.moveCarrousel(0);
      
      _LOG.info("waiting for the carousel");
      autosampler.finMoveCarrousel();
      
      _LOG.info(String.format("moving arm to position %s", armNbStep));
      autosampler.moveBras(armNbStep);
      
      _LOG.info("waiting for the arm");
      autosampler.finMoveBras();
      
      _LOG.info(String.format("moving arm back"));
      autosampler.moveButeBras();
      
      _LOG.info("waiting for the arm");
      autosampler.finMoveBras();
      
      _LOG.info(String.format("moving carousel to position %s and arm to position %s", carouselPosition, armNbStep));
      autosampler.moveCarrouselEtBras(carouselPosition, armNbStep);
      
      Thread.sleep(1000);
      
      _LOG.debug("pausing");
      autosampler.pause();
      
      _LOG.info(String.format("where arm: %s", autoSamplerInt.where(TypeAxe.bras)));
      _LOG.info(String.format("where carousel: %s", autoSamplerInt.where(TypeAxe.carrousel)));
      
      Thread.sleep(5000);
      
      _LOG.debug("resuming");
      autosampler.reprise(true);
      
      Thread.sleep(1500);
      
      _LOG.info(String.format("where arm: %s", autoSamplerInt.where(TypeAxe.bras)));
      _LOG.info(String.format("where carousel: %s", autoSamplerInt.where(TypeAxe.carrousel)));
      
      _LOG.debug("cancelling");
      autosampler.cancel();
      
      _LOG.info(String.format("where arm: %s", autoSamplerInt.where(TypeAxe.bras)));
      _LOG.info(String.format("where carousel: %s", autoSamplerInt.where(TypeAxe.carrousel)));
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
}
