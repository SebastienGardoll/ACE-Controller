package fr.gardoll.ace.controller.autosampler;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.com.FlowControl ;
import fr.gardoll.ace.controller.com.Parity ;
import fr.gardoll.ace.controller.com.SerialCom;
import fr.gardoll.ace.controller.com.SerialMode ;
import fr.gardoll.ace.controller.com.StopBit ;
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.SerialComException ;
import fr.gardoll.ace.controller.core.ThreadControl ;

//TODO: singleton.
public class InterfaceMoteur implements Closeable
{
  private static final int OPENING_DELAY = 1000;
  
  // nombre de signaux en sortis de l'interface.
  private final static int NB_BITS = 7 ;
  
  private static final Logger _LOG = LogManager.getLogger(InterfaceMoteur.class.getName());
  
  private final SerialCom _port;
  
  private ThreadControl _threadCtrl = null;
  
  public InterfaceMoteur(SerialCom port) throws InitializationException
  {
    this._port = port ;
    try
    {
      _LOG.debug(String.format("setting the autosampler com port '%s'", this._port.getId()));
      this._port.setReadBufferSize(256);
      this._port.setMode(SerialMode.FULL_BLOCKING, SerialMode.FULL_BLOCKING);
      this._port.setCharset(Charset.forName("ASCII"));
      this._port.setVitesse(9600) ;
      this._port.setByteSize(8);
      this._port.setStopBit(StopBit.ONESTOPBIT);
      this._port.setParite(Parity.NOPARITY);
      this._port.setControlFlux(FlowControl.XON_XOFF);
      this._port.setTimeOut(100) ;
      this._port.open(OPENING_DELAY);
    }
    catch(SerialComException | InterruptedException e)
    {
      String msg = String.format("error while initializing the port '%s': %s",
          this._port.getId(), e.getMessage());
      _LOG.fatal(msg, e);
      throw new InitializationException(msg, e);
    }
  }

  @Override
  public void close() throws IOException
  {
    this._port.close() ;
  }
  
  public void setThreadControl(ThreadControl threadCtrl)
  {
    this._threadCtrl = threadCtrl;
  }
  
  // traitement de la réponse de l'interface.
  // en cas d'erreur => exception
  private void traitementReponse(String reponse)
  {
    if(reponse.isEmpty())
    {
      String msg = "autosampler disconnected";
      _LOG.fatal(msg);
      throw new RuntimeException();
    }
    
    switch (reponse.charAt(0))
    {  
      case '0': { break ; } // commande bien exécutée par l'interface
      
      case '1': { break ; } // toujours en mouvement pour la fonction moving

      case 'E':
      { 
        String msg = String.format("autosampler failure: %s", reponse);
        _LOG.fatal(msg);
        throw new RuntimeException(msg);
      }

      // attention incompatibilité avec where et position négative !!!
      // case '-' : { throw EInterfaceMoteur (IM_ERREUR_RECEPTION_2) ; break ;}
      
      // XXX untranslated code.
      // case 0   : { throw EInterfaceMoteur ( IM_ERREUR_RECEPTION_3 ) ; break ; }
      
      default:
      {
        String msg = String.format("unsupported reponse: %s", reponse);
        _LOG.fatal(msg);
        throw new RuntimeException(msg);
      }

    }
  }
  
  private void checkThreadCtrl() throws InterruptedException
  {
    if(this._threadCtrl != null)
    {
      this._threadCtrl.checkInterruption();
      this._threadCtrl.checkPause();
    }
  }
  
  // renvoie la réponse de l'interface. Temporisation en milisecondes.
  private String traitementOrdre(String ordre, long temporisation)
                                 throws SerialComException, InterruptedException
  {  
    this.checkThreadCtrl();
    
    String reponse = "";
    
    this._port.ecrire(ordre) ;

    Thread.sleep(temporisation);

    reponse = this.lectureReponse() ;

    this.traitementReponse (reponse) ;
    
    return reponse  ;
  }
  
  private String traitementOrdre(String ordre)
      throws SerialComException, InterruptedException
  {
    return this.traitementOrdre (ordre, 0l) ;
  }
  
  private String lectureReponse() throws SerialComException
  {
    String message_brute = this._port.lire().strip() ;

    String message_renvoye = message_brute.replaceAll("#", "") ;
    
    return message_renvoye ;
  }  
  
  public void move(int nbPas1 , int nbPas2) throws SerialComException, InterruptedException
  { 
    String ordre = String.format("move (%s,%s)\r", nbPas1, nbPas2) ;
    this.traitementOrdre (ordre) ;
  }
  
  //détection fin de mouvement
  public boolean moving(TypeAxe axe) throws SerialComException, InterruptedException
  {  
    String ordre = String.format("moving (%s)\r", axe);
    // valeur 1 ==> en mouvement
    return  (this.traitementOrdre(ordre).charAt(0) == '1' ) ; 
  }

  // avance jusqu'à fin de butée
  // 0 :pas bougé, 1 : butée positive, -1 : butée négative
  public void movel(int axe1, int axe2) throws SerialComException, InterruptedException
  {  
    String ordre = String.format("movel (%s,%s)\r", axe1, axe2);
    this.traitementOrdre(ordre) ;
  }
  
  //réinitialisation de l'interface
  public void reset() throws SerialComException, InterruptedException

  {
    this._port.ecrire("new\r") ;

    // attente obligatoire à cause de la lenteur de l'interface
    //  pour cette fonction . 800 ms
    Thread.sleep(800); 

    this.lectureReponse ();

    // ATTENTION PAS DE VERIFICATION REPONSE !!! 
  }
  
  public void preSecale(int denominateur) 
      throws SerialComException, InterruptedException
  {  
    String ordre = String.format("prescale (%s)\r", denominateur);
    this.traitementOrdre (ordre) ;
  }
  
  public void param(TypeAxe axe, int base, int top, int accel)
                                                       throws SerialComException, InterruptedException
  {
    this.param(axe, base, top, accel, 0);
  }
  
  public void param(TypeAxe axe, int base, int top, int accel, int deaccel)
                                                       throws SerialComException, InterruptedException

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
    this.traitementOrdre(ordre, 2000l) ;
  }

  public void datum(TypeAxe axe) throws SerialComException, InterruptedException

  {  
    String ordre = String.format("datum (%s)\r", axe);
    this.traitementOrdre (ordre) ;
  }
  
  public void singleLine(boolean choix) throws SerialComException, InterruptedException
  { 
    // XXX convertion of boolean. Checked 04/18/2019
    char convertion = (choix) ? '1':'2';
    
    String ordre = String.format("singleline (%s)\r", convertion);
    this.traitementOrdre (ordre) ;
  }
  
  public void stop() throws SerialComException, InterruptedException
  { 
    this.traitementOrdre("stop ()\r") ;
  }
  
  public void manual() throws SerialComException, InterruptedException
  {  
    this.traitementOrdre("manual ()\r") ;
  }

  public void halt() throws SerialComException, InterruptedException
  { 
    // la temporisation dépend directement
    // de la vitesse de point et de la décéleration
    this.traitementOrdre ("halt()\r" , 1500l) ;
  } 
                                           
  public int where(TypeAxe axe) throws SerialComException, InterruptedException
  {  
    String ordre = String.format("where (%s)\r", axe);
    String response = this.traitementOrdre(ordre, 100l);
    return Integer.valueOf(response);
  }

  //voir manuel de l'interface
  // 0 <= octet <= 255
  public void out(int octet) throws SerialComException, InterruptedException
  { 
    if (octet <= 255 && octet <= 0)
    {
      String ordre = String.format("out(%s)\r", octet);
      this.traitementOrdre(ordre);
    }
    else
    {
      String msg = String.format("the state of optocoupled outputs '%s' must be greater or equal than 0 but less or equal than 255)",
          octet);
      _LOG.fatal(msg);
      throw new RuntimeException(msg);
    }
  }

  //voir manuel de l'interface
  public void out(int bitPosition, boolean isOn) throws SerialComException, InterruptedException
  { 
    if (bitPosition > NB_BITS )
    {
      String msg = String.format("the position of the bit '%s' cannot be greater than %s",
          bitPosition, NB_BITS);
      _LOG.fatal(msg);
      throw new RuntimeException(msg);
    }
    
    char value = (isOn) ? '1' : '0';
    
    String ordre = String.format("out(%s,%s)\r", bitPosition, value);
    this.traitementOrdre(ordre);
  }
}
