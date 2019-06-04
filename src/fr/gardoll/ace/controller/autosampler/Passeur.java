package fr.gardoll.ace.controller.autosampler;

import java.io.Closeable ;
import java.io.IOException ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.com.JSerialComm ;
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.SerialComException ;

//TODO: singleton.
//TODO: add logging
public class Passeur implements Closeable
{
  public final static int RAPPORT_REDUCTEUR_MOTEUR = 40 ; //DEPENDANT DE LA MECANIQUE
  public final static int NB_PAS_TOUR_BRAS = 400 ; // en demi pas
  public final static int NB_PAS_TOUR_MOTEUR = 400 ;//demi pas sans réducteur
  public final static int NB_PAS_TOUR_CARROUSEL = RAPPORT_REDUCTEUR_MOTEUR * NB_PAS_TOUR_MOTEUR  ; //demi pas
  public final static int HAUTEUR_TOUR_BRAS = 3 ; // en mm

  public final static int VIB_ID = 2 ; //numéro du signal utilisé pour faire vibrer le bras
  public final static int VIBRATION_TEMPS = 500 ; //temps en ms de vibration
  public final static int TRASH_POSITION = 0;
  
  private static final Logger _LOG = LogManager.getLogger(Passeur.class.getName());
  
  private int x = 0; //nombre de demi pas pour l'axe carrousel

  private int y = 0; //nombre de demi pas pour l'axe bras

  private int sav_x = 0; //position du carrousel enregistrée est recalculé si appel setOrigineXX

  private int sav_y = 0; //position du bras enregistrée est recalculé si appel setOrigineXX

  private boolean sav_butee = false;//flag de butee enregistré

  private final int nbPasCarrousel ;

  private final double rayon ;

  private boolean _pause = false;

  //ce flag sert en cas de pause à savoir si le bras devait
  // s'arrêter à une butée ( donc pas de x ou y précisé ) ou non
  private boolean _butee = false; 
  
  private final InterfaceMoteur interfaceMoteur;
  
  /* ne pas inclure d'attente de fin de mouvement dans les procédures de mouvement
  car incompatible avec le système de pause */ 

  // Les arguments donnée aux méthodes ne sont pas vérifiés car grâce aux butée de
  // fin de course, la sécurité est assurée.

  //require nbPasCarrousel > 0
  //require diametre > 0
  public Passeur(InterfaceMoteur interfaceMoteur, int nbPasCarrousel, int diametre)
      throws InitializationException, InterruptedException
  {
    this.interfaceMoteur = interfaceMoteur;
    this.reset() ;
    this.setModeDirect();

    if (nbPasCarrousel <= 0 || diametre  <= 0)
    {
      String msg = String.format("the value of the diameter '%s' and the steps '%s' of the step motor cannot be negative or null",
          diametre, nbPasCarrousel);
      _LOG.fatal(msg);
      throw new InitializationException(msg);
    }
    else
    {  
      this.nbPasCarrousel = nbPasCarrousel ;
      this.rayon = diametre / 2.  ;
    }
  }
  
  @Override
  public void close() throws IOException
  {
    this.interfaceMoteur.close();
  }
  
  public boolean isPaused () { return _pause; } ;
  
  public void moveCarrousel(int position) throws InterruptedException
  {
    this.moveCarrousel(position, 0);
  }
  
  // le numéro de la position. 0 => poubelle
  // modificateur => ajout d'un nombre de demi pas
  // modificateur ou en nombre de pas si position = 0
  public void moveCarrousel(int position, int modificateur) throws InterruptedException
  {
    this.x = position * nbPasCarrousel + modificateur ;
    
    try
    {
      this.interfaceMoteur.move(this.x , this.y);
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while moving the autosampler carousel to position '%s' with offset '%s': %s",
          position, modificateur, e.getMessage());
      _LOG.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  //en nombre de demi pas
  public void moveBras(int nbPas) throws InterruptedException
  {
    this.y = nbPas ;
    
    try
    {
      this.interfaceMoteur.move(this.x, this.y);
    }
    catch (SerialComException e)
    {
      String msg = String.format("error while moving the autosampler arm of '%s' steps: %s",
          nbPas, e.getMessage());
      _LOG.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
  }
  
  // effectue un mouvement simultané
  // fait car problème de temps de réponse car halt fait par interface
  public void moveCarrouselEtBras (int position, int nbPas) throws InterruptedException
  {
    this.x = position * nbPasCarrousel ;
    this.y = nbPas ;
    try
    {
      this.interfaceMoteur.move(this.x , this.y);
    }
    catch (SerialComException e)
    {
      String msg = String.format("error while moving the autosampler arm of '%s' steps and carousel to position '%s': %s",
          nbPas, position, e.getMessage());
      _LOG.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  //le bras revient à son origine
  public void moveOrigineBras() throws InterruptedException
  {
    try
    {
      this.interfaceMoteur.move(this.x, 0);
      this.y = 0 ;
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while moving autosampler arm to base: %s",
          e.getMessage());
      _LOG.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  //avance le bras jusqu'à la butée haute sans setOrigineBras
  public void moveButeBras() throws InterruptedException
  {  
    try
    {
      this.interfaceMoteur.movel(0, 1);
      this._butee = true ;
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while moving autosampler arm to upper limit: %s",
          e.getMessage());
      _LOG.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
  }
  
  //attente de la fin de mouvement
  public void finMoveCarrousel() throws InterruptedException
  {
    boolean isMoving = true;
    do
    { 
      Thread.sleep(100) ;
      
      try
      {
        isMoving = this.interfaceMoteur.moving(TypeAxe.carrousel);
      }
      catch (SerialComException e)
      {
        String msg = String.format("error while waiting for the end of the autosampler carousel move: %s", e.getMessage());
        _LOG.fatal(msg, e);
        throw new RuntimeException(msg, e);
      }
    }
    while (isMoving) ;
  }

  //attente de la fin de mouvement
  public void finMoveBras() throws InterruptedException
  {
    try
    {
      boolean isMoving = true;
      do
      { 
        Thread.sleep (100) ;
        
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
      String msg = String.format("error while waiting the end of the autosampler arm move: %s", e.getMessage());
      _LOG.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
  }
  
  //envoie la commande new à l'interface.
  public void reset() throws InterruptedException
  { 
    try
    {
      this.interfaceMoteur.reset();
      this.x = 0 ; this.y = 0 ;
    }
    catch (SerialComException e)
    {
      String msg = String.format("error while resetting the autosampler interface: %s", e.getMessage());
      _LOG.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
  }
  
  //la position courante devient l'origine du bras <=> y = 0
  //sav_y est recalculé dans le nouveau référenciel
  public void setOrigineBras() throws InterruptedException
  { 
    try
    {
      this.interfaceMoteur.datum(TypeAxe.bras);
      this.sav_y = 0 - this.y + this.sav_y ;
      this.y = 0 ;
    }
    catch (SerialComException e)
    {
      String msg = String.format("error while setting the autosampler arm base: %s", e.getMessage());
      _LOG.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  // la position courante devient l'origine du carrousel <=> x = 0
  // sav_x est recalculé dans le nouveau référenciel
  public void setOrigineCarrousel() throws InterruptedException
  { 
    try
    {
      this.interfaceMoteur.datum(TypeAxe.carrousel);
      this.sav_x = 0 - this.x + this.sav_x ;
      this.x = 0 ;
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while setting the autosampler carousel base: %s", e.getMessage());
      _LOG.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  //sur les deux axes
  public void setOrigine() throws InterruptedException
  { 
    this.setOrigineBras() ;
    this.setOrigineCarrousel() ;
  }
  
  // commande directe de l'interface, pas de
  // mémorisation des commandes envoyées.
  public void setModeDirect() throws InterruptedException
  {  
    try
    {
      this.interfaceMoteur.singleLine(true);
    }
    catch (SerialComException e)
    {
      String msg = String.format("error while setting autosampler direct mode: %s", e.getMessage());
      _LOG.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  //permet d'utiliser le potentiomètre numérique
  public void setModeManuel() throws InterruptedException
  { 
    try
    {
      this.interfaceMoteur.manual() ;
    }
    catch (SerialComException e)
    {
      String msg = String.format("error while setting autosampler manual mode: %s", e.getMessage());
      _LOG.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
  }
  
  //pause du passeur avec enregistrement de la position courante
  public void pause() throws InterruptedException
  {
    try
    {
      if (this.interfaceMoteur.moving(TypeAxe.carrousel) ||
          this.interfaceMoteur.moving(TypeAxe.bras))
      { 
        this.interfaceMoteur.halt();
      }
         
      this._pause = true ;
      this.saveCurrentPosition();
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while pausing the autosampler: %s", e.getMessage());
      _LOG.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
  }
  
  // TODO: test.
  public void returnToInit() throws InterruptedException
  {
    this.moveButeBras();
    this.moveCarrousel(TRASH_POSITION);
  }
  
  // TODO: test.
  public void cancel() throws InterruptedException
  {
    try
    {
      this.interfaceMoteur.halt();
      this.returnToInit();
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while cancelling the autosampler: %s", e.getMessage());
      _LOG.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
  }
  
  //reprise sur pause avec retour à la position sauvegardée dans pause()
  public void reprise(boolean brasFirst) throws InterruptedException
  {  
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
            this.interfaceMoteur.move(this.x, this.y) ;
          }
          catch (SerialComException e)
          {
            String msg = String.format("error while resuming the autosampler move: %s", e.getMessage());
            _LOG.fatal(msg, e);
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
  
  public static int rapportReducteur () { return RAPPORT_REDUCTEUR_MOTEUR ;}


  public static int nbPasTourBras () { return NB_PAS_TOUR_BRAS ; }


  public static int hauteurTourBras () { return HAUTEUR_TOUR_BRAS ; }

  //converti une dimension exprimée en mm en nombre de demi pas pour le bras.
  //arrondi au demi pas supérieur
  //dimension en mm en rapport avec le bras !
  public static int convertBras(double dimension)
  {
    double result = Math.ceil( (dimension * NB_PAS_TOUR_BRAS) / HAUTEUR_TOUR_BRAS ) ;
    return ((int) (result));
  }
  
  //converti une dimension exprimée en mm en nombre de demi pas pour le carrousel.
  //arrondi au demi pas inférieur
  //dimension en mm en rapport avec au carrousel !
  public int convertCarrousel(double dimension)
  {
    double result = (Math.asin(dimension / rayon) *  NB_PAS_TOUR_CARROUSEL) / ( 2 * Math.PI) ; 
    return ((int) (result));
  }

  public void vibration() throws InterruptedException
  {  
    try
    {
      this.interfaceMoteur.out(VIB_ID, true);

      Thread.sleep(VIBRATION_TEMPS) ;

      this.interfaceMoteur.out(VIB_ID, false);
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while vibrating the autosampler arm: %s", e.getMessage());
      _LOG.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
  }
  
  //le carrousel revient à son point d'origine
  public void moveOrigineCarrousel() throws InterruptedException
  {
    try
    {
      this.interfaceMoteur.move(0 , this.y);
      this.x = 0 ;
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while moving the autosampler carousel to base: %s", e.getMessage());
      _LOG.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
  }
  
  //bouge de nbPosition
  //!= moveCarrousel où on précise le numéro de la position par rapport à 0 
  public void moveCarrouselRelatif(int nbPosition) throws InterruptedException
  {
     try
     {
       this.x += nbPosition * this.nbPasCarrousel  ;
       this.interfaceMoteur.move(this.x , this.y);
     }
     catch(SerialComException e)
     {
       String msg = String.format("error while moving the autosampler carousel to position '%s' (relative): %s",
           nbPosition, e.getMessage());
       _LOG.fatal(msg, e);
       throw new RuntimeException(msg, e);
     }
  }

  //enregistre la position du bras et du carrousel
  //dans les variables save_x et save_y
  //est recalculé si appel setOrigineXX mais attention à son utilité par la suite
  //appel obligatoire si l'on doit manipuler le passeur pendant une pause
  public void saveCurrentPosition()
  {  
    this.sav_x = this.x ; this.sav_y = this.y ; this.sav_butee = this._butee ;
  }

  //revient à la position enregistrée par saveCurrentPosition
  //détermine si le bras bouge avant le carrousel
  //appel obligatoire si l'on doit manipuler le passeur pendant une pause
  public void returnSavedPosition(boolean brasFirst) throws InterruptedException
  {
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
        Passeur autosampler = new Passeur(autoSamplerInt, 640, 360))
    {
      int carouselPosition = 4;
      _LOG.info(String.format("moving carousel to position %s", carouselPosition));
      autosampler.moveCarrousel(carouselPosition);
      
      _LOG.info("waiting for the carousel");
      autosampler.finMoveCarrousel();
      
      _LOG.info(String.format("moving carousel back"));
      autosampler.moveCarrousel(0);
      
      _LOG.info("waiting for the carousel");
      autosampler.finMoveCarrousel();
      
      int armNbStep = 30;
      _LOG.info(String.format("moving arm to %s nb of step", armNbStep));
      autosampler.moveBras(armNbStep);
      
      _LOG.info("waiting for the arm");
      autosampler.finMoveBras();
      
      _LOG.info(String.format("moving arm back"));
      autosampler.moveButeBras();
      
      _LOG.info("waiting for the arm");
      autosampler.finMoveBras();
      
      _LOG.info(String.format("moving carousel to position %s and arm to %s nb of step", carouselPosition, armNbStep));
      autosampler.moveCarrousel(carouselPosition, armNbStep);
      
      Thread.sleep(500);
      
      _LOG.debug("pausing");
      autosampler.pause();
      
      Thread.sleep(1500);
      
      _LOG.debug("resuming");
      autosampler.reprise(true);
      
      Thread.sleep(200);
      
      _LOG.debug("cancelling");
      autosampler.cancel();      
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
}
