package fr.gardoll.ace.controller.sampler;

import java.io.Closeable;
import java.io.IOException;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.comm.FlowControl ;
import fr.gardoll.ace.controller.comm.Parity ;
import fr.gardoll.ace.controller.comm.SerialCom;
import fr.gardoll.ace.controller.comm.StopBit ;
import fr.gardoll.ace.controller.common.InitializationException ;
import fr.gardoll.ace.controller.common.SerialComException ;

public class InterfaceMoteur implements Closeable
{
  // nombre de signaux en sortis de l'interface.
  private final static int NB_BITS = 7 ;
  
  private static final Logger _LOG = LogManager.getLogger(InterfaceMoteur.class.getName());
  
  private final SerialCom _port;
  
  public InterfaceMoteur(SerialCom port) throws InitializationException
  {
    this._port = port ;
    try
    {
      this._port.setVitesse(9600) ;
      this._port.setByteSize(8);
      this._port.setStopBit(StopBit.ONESTOPBIT);
      this._port.setParite(Parity.NOPARITY);
      this._port.setControlFlux(FlowControl.XON_XOFF);
      this._port.setTimeOut(100) ;
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while initializing the port '%s': %s",
          this._port.getId(), e.getMessage());
      _LOG.fatal(msg);
      throw new InitializationException(msg, e);
    }
  }

  @Override
  public void close() throws IOException
  {
    this._port.close() ;
  }
  
  public static void main(String[] args)
  {
    // TODO Auto-generated method stub

  }
  
  // traitement de la réponse de l'interface.
  // en cas d'erreur => exception
  private void traitementReponse(String reponse)
  {
    if(reponse.isEmpty())
    {
      String msg = "sampler disconnected";
      _LOG.fatal(msg);
      throw new RuntimeException();
    }
    
    switch (reponse.charAt(1)) // XXX Is it the right char to look at ?
    {  
      case '0': { break ; } // commande bien exécutée par l'interface
      
      case '1': { break ; } // toujours en mouvement pour la fonction moving

      case 'E':
      { 
        String msg = String.format("sampler failure: %s", reponse);
        _LOG.fatal(msg);
        throw new RuntimeException(msg);
      }

      // attention incompatibilité avec where et position négative !!!
      // case '-' : { throw EInterfaceMoteur (IM_ERREUR_RECEPTION_2) ; break ;}
      
      // XXX untranslated code.
      // case 0   : { throw EInterfaceMoteur ( IM_ERREUR_RECEPTION_3 ) ; break ; }

    }
  }
  
  // renvoie la réponse de l'interface. Temporisation en milisecondes.
  private String traitementOrdre(String ordre, long temporisation)
                                 throws SerialComException
  {  
    String reponse = "";
    
    this._port.ecrire( ordre ) ;

    try
    {
      Thread.sleep(temporisation);
    }
    catch (InterruptedException e)
    {
      String msg = String.format("interruption while waiting sampler response: %s",
          e.getMessage());
      _LOG.debug(msg);
    }

    reponse = this.lectureReponse() ;

    this.traitementReponse (reponse) ;
    
    return reponse  ;
  }
  
  private String traitementOrdre(String ordre) throws SerialComException
  {
    return this.traitementOrdre (ordre, 0l) ;
  }
  
  private String lectureReponse() throws SerialComException
  {
    String message_brute = this._port.lire() ;

    String message_renvoye = message_brute.replaceAll("#", "") ;
    
    return message_renvoye ;
  }  
  
  public void move(int nbPas1 , int nbPas2) throws SerialComException
  { 
    String ordre = String.format("move (%s,%s)\r", nbPas1, nbPas2) ;
    this.traitementOrdre (ordre) ;
  }
  
  //détection fin de mouvement
  public boolean moving(TypeAxe axe) throws SerialComException
  {  
    String ordre = String.format("moving (%s)\r", axe);
    // valeur 1 ==> en mouvement
    return  (this.traitementOrdre(ordre).charAt(1) == '1' ) ; // XXX Is it the right char to look at ? 
  }

  // avance jusqu'à fin de butée
  // 0 :pas bougé, 1 : butée positive, -1 : butée négative
  public void movel(int axe1, int axe2) throws SerialComException
  {  
    String ordre = String.format("movel (%s,%s)\r", axe1, axe2);
    this.traitementOrdre(ordre) ;
  }
  
  //réinitialisation de l'interface
  public void reset() throws SerialComException

  {
    this._port.ecrire("new\r") ;

    // attente obligatoire à cause de la lenteur de l'interface
    //  pour cette fonction . 800 ms
    try
    {
      Thread.sleep(800);
    }
    catch (InterruptedException e)
    {
      String msg = String.format("interruption while waiting sampler reset: %s",
          e.getMessage());
      _LOG.debug(msg);
    } 

    this.lectureReponse ();

    // ATTENTION PAS DE VERIFICATION REPONSE !!! 
  }
  
  public void preSecale(int denominateur) throws SerialComException
  {  
    String ordre = String.format("prescale (%s)\r", denominateur);
    this.traitementOrdre (ordre) ;
  }
  
  public void param(TypeAxe axe, int base, int top, int accel)
                                                       throws SerialComException
  {
    this.param(axe, base, top, accel, 0);
  }
  
  public void param(TypeAxe axe, int base, int top, int accel, int deaccel)
                                                       throws SerialComException

  {  
    String ordre = null;

    if (deaccel == 0)
    {
      ordre = String.format("param (%s,%s,%s,%s)\r", axe, base, top, accel);
    }
    else
    {
      ordre = String.format("param (%s,%s,%s,%s,%s)\r", axe, base, top, accel, deaccel);
    }

    // long traitement par l'interface
    this.traitementOrdre(ordre, 2000) ;
  }

  public void datum(TypeAxe axe) throws SerialComException

  {  
    String ordre = String.format("datum (%s)\r", axe);
    this.traitementOrdre (ordre) ;
  }
  
  public void singleLine(boolean choix) throws SerialComException
  { 
    String ordre = String.format("singleline (%s)\r", choix); // XXX convertion of boolean
    this.traitementOrdre (ordre) ;
  }
  
  //précondition : le threadSequence doit être détruit ( threadterminate) ou inexistant
  public void stop() throws SerialComException
  { 
    this._port.ecrire("stop ()\r") ;

    String reponse = lectureReponse() ;

    this.traitementReponse ( reponse ) ;
  }
  
  public void manual() throws SerialComException
  {  
    this.traitementOrdre("manual ()\r") ;
  }

  public void halt() throws SerialComException
  { 
    // la temporisation dépend directement
    // de la vitesse de point et de la décéleration
    this.traitementOrdre ("halt()\r" , 1500) ;
  } 
                                           
  public int where(TypeAxe axe) throws SerialComException
  {  
    String ordre = String.format("where (%s)\r", axe);
    String response = this.traitementOrdre(ordre, 100);
    return Integer.valueOf(response);
  }

  //voir manuel de l'interface
  public void out(byte octet) throws SerialComException // XXX Is it the right type ?
  { 
    String ordre = String.format("out(%s)\r", octet); // XXX check the man.
    this.traitementOrdre(ordre);
  }

  //voir manuel de l'interface
  public void out(int bit, boolean valeur) throws SerialComException // XXX Is it the right type ?
  { 
    if (bit > NB_BITS )
    {
      String msg = String.format("the value of the bit '%s' cannot be greater than %s",
          bit, NB_BITS);
      _LOG.fatal(msg);
      throw new RuntimeException(msg);
    }

    String ordre = String.format("out(%s,%s)\r", bit, valeur); // XXX check the man.
    this.traitementOrdre(ordre);
  }
}
