package fr.gardoll.ace.controller.autosampler;

import java.io.Closeable;
import java.io.IOException;

import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.com.FlowControl ;
import fr.gardoll.ace.controller.com.JSerialComm ;
import fr.gardoll.ace.controller.com.Parity ;
import fr.gardoll.ace.controller.com.SerialCom;
import fr.gardoll.ace.controller.com.SerialComException ;
import fr.gardoll.ace.controller.com.SerialMode ;
import fr.gardoll.ace.controller.com.StopBit ;
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.Log ;

public class InterfaceMoteur implements Closeable, MotorController
{
  private static final long _OPENING_DELAY = 2000l;
  
  // nombre de signaux en sortis de l'interface.
  private final static int _NB_BITS = 7 ;
  
  private static final Logger _LOG = Log.CONTROLLER;
  
  private final SerialCom _port;
  
  public InterfaceMoteur(SerialCom port) throws InitializationException
  {
    _LOG.debug(String.format("initializing the autosampler interface with the serial port %s",
        port.getId()));
    this._port = port ;
    try
    {
      _LOG.debug(String.format("setting the autosampler com port '%s'", this._port.getId()));
      this._port.setReadBufferSize(256);
      this._port.setMode(SerialMode.FULL_BLOCKING, SerialMode.FULL_BLOCKING);
      this._port.setCharset(JSerialComm.ASCII_CHARSET);
      this._port.setVitesse(9600) ;
      this._port.setByteSize(8);
      this._port.setStopBit(StopBit.ONESTOPBIT);
      this._port.setParite(Parity.NOPARITY);
      this._port.setControlFlux(FlowControl.XON_XOFF);
      this._port.setTimeOut(100) ;
      this._port.open(_OPENING_DELAY);
      
      this.singleLine(true); // ack processing supposes to get single line ack.
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while initializing the port '%s'",
          this._port.getId());
      throw new InitializationException(msg, e);
    }
  }

  @Override
  public void close() throws IOException
  {
    _LOG.debug(String.format("closing motor controller with port id '%s'", this._port.getId()));
    this._port.close() ;
  }
  
  // traitement de la réponse de l'interface.
  // en cas d'erreur => exception
  private void traitementReponse(String reponse) throws SerialComException
  {
    //_LOG.trace(String.format("ack received: '%s'", reponse));
    
    if(reponse.isEmpty())
    {
      String msg = "autosampler disconnected";
      throw new SerialComException(msg);
    }

    switch (reponse.charAt(0))
    {  
      // FYI
      //case '0': { break ; } // commande bien exécutée par l'interface
      //case '1': { break ; } // toujours en mouvement pour la fonction moving

      case 'E':
      { 
        String msg = String.format("autosampler failure: '%s'", reponse);
        throw new SerialComException(msg);
      }
    }
  }
  
  // renvoie la réponse de l'interface. Temporisation en milisecondes.
  private String traitementOrdre(String ordre, long temporisation)
                                 throws SerialComException
  {  
    String reponse = "";
    
    this._port.ecrire(ordre) ;

    try
    {
      Thread.sleep(temporisation);
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
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
    String message_brute = this._port.lire().strip() ;

    String message_renvoye = message_brute.replaceAll("#", "") ;
    
    return message_renvoye ;
  }  
  
  @Override
  public void move(int nbPas1 , int nbPas2) throws SerialComException
  { 
    String ordre = String.format("move (%s,%s)\r", nbPas1, nbPas2) ;
    _LOG.trace(String.format("command '%s'", ordre.substring(0, ordre.length()-1)));
    this.traitementOrdre (ordre) ;
  }
  
  //détection fin de mouvement
  @Override
  public boolean moving(TypeAxe axe) throws SerialComException
  {  
    String ordre = String.format("moving (%s)\r", axe);
    // valeur 1 ==> en mouvement
    return  (this.traitementOrdre(ordre).charAt(0) == '1' ) ; 
  }

  // avance jusqu'à fin de butée
  // 0 :pas bougé, 1 : butée positive, -1 : butée négative
  @Override
  public void movel(int axe1, int axe2) throws SerialComException
  {  
    String ordre = String.format("movel (%s,%s)\r", axe1, axe2);
    _LOG.trace(String.format("command '%s'", ordre.substring(0, ordre.length()-1)));
    this.traitementOrdre(ordre) ;
  }
  
  //réinitialisation de l'interface
  @Override
  public void reset() throws SerialComException

  {
    String order = "new\r";
    _LOG.trace("command 'new'");
    this._port.ecrire(order) ;

    // attente obligatoire à cause de la lenteur de l'interface
    //  pour cette fonction . 800 ms
    try
    {
      Thread.sleep(800);
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    } 

    this.lectureReponse ();

    // ATTENTION PAS DE VERIFICATION REPONSE !!! 
  }
  
  @Override
  public void preSecale(int denominateur) throws SerialComException
  {  
    String ordre = String.format("prescale (%s)\r", denominateur);
    _LOG.trace(String.format("command 'prescale (%s)'", denominateur));
    this.traitementOrdre (ordre) ;
  }
  
  @Override
  public void param(TypeAxe axe, int base, int top, int accel)
                                                       throws SerialComException
  {
    this.param(axe, base, top, accel, 0);
  }
  
  @Override
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
    
    _LOG.trace(String.format("command '%s'", ordre.substring(0, ordre.length()-1)));
    
    // long traitement par l'interface
    this.traitementOrdre(ordre, 2000l) ;
  }

  @Override
  public void datum(TypeAxe axe) throws SerialComException

  {  
    String ordre = String.format("datum (%s)\r", axe);
    _LOG.trace(String.format("command 'datum (%s)'", axe));
    this.traitementOrdre (ordre) ;
  }
  
  @Override
  public void singleLine(boolean choix) throws SerialComException
  { 
    char convertion = (choix) ? '1':'2';
    
    String ordre = String.format("singleline (%s)\r", convertion);
    _LOG.trace(String.format("command 'singleline (%s)'", convertion));
    this.traitementOrdre (ordre) ;
  }
  
  @Override
  public void stop() throws SerialComException
  { 
    String order = "stop ()\r";
    _LOG.trace("command 'stop ()'");
    this.traitementOrdre(order) ;
  }
  
  @Override
  public void manual() throws SerialComException
  {  
    String order = "manual ()\r";
    _LOG.trace("command 'manual ()'");
    this.traitementOrdre(order) ;
  }

  @Override
  public void halt() throws SerialComException
  { 
    // la temporisation dépend directement
    // de la vitesse de point et de la décéleration
    String order = "halt()\r";
    _LOG.trace("command 'halt()'");
    this.traitementOrdre (order , 1500l) ;
  } 
                                           
  @Override
  public int where(TypeAxe axe) throws SerialComException
  {  
    String ordre = String.format("where (%s)\r", axe);
    _LOG.trace(String.format("command 'where (%s)'", axe));
    String response = this.traitementOrdre(ordre, 100l);
    return Integer.valueOf(response);
  }

  //voir manuel de l'interface
  // 0 <= octet <= 255
  @Override
  public void out(int octet) throws SerialComException
  { 
    if (octet <= 255 && octet <= 0)
    {
      String ordre = String.format("out(%s)\r", octet);
      _LOG.trace(String.format("command 'out(%s)'", octet));
      this.traitementOrdre(ordre);
    }
    else
    {
      String msg = String.format("the state of optocoupled outputs '%s' must be greater or equal than 0 but less or equal than 255)",
          octet);
      throw new RuntimeException(msg);
    }
  }

  //voir manuel de l'interface
  @Override
  public void out(int bitPosition, boolean isOn) throws SerialComException
  { 
    if (bitPosition > _NB_BITS )
    {
      String msg = String.format("the position of the bit '%s' cannot be greater than %s",
          bitPosition, _NB_BITS);
      throw new RuntimeException(msg);
    }
    
    char value = (isOn) ? '1' : '0';
    
    String ordre = String.format("out(%s,%s)\r", bitPosition, value);
    _LOG.trace(String.format("command 'out(%s, %s)'", bitPosition, value));
    this.traitementOrdre(ordre);
  }
  
  @Override
  public String getPortPath()
  {
    return this._port.getPath();
  }
  
  public static void main(String[] args)
  {
    String portPath = "/dev/ttyUSB0"; // To be modified.
    JSerialComm port = new JSerialComm(portPath);
    
    try(InterfaceMoteur autoSamplerInt = new InterfaceMoteur(port))
    {
      boolean isArmMoving = autoSamplerInt.moving(TypeAxe.bras);
      _LOG.info(String.format("is arm moving: %s", isArmMoving));
      
      boolean isCarouselMoving = autoSamplerInt.moving(TypeAxe.carrousel);
      _LOG.info(String.format("is carousel moving: %s", isCarouselMoving));
      
      int whereIsArm = autoSamplerInt.where(TypeAxe.bras);
      _LOG.debug(String.format("the arm is at '%s'", whereIsArm));
      
      int whereIsCarousel = autoSamplerInt.where(TypeAxe.carrousel);
      _LOG.debug(String.format("the carousel is at '%s'", whereIsCarousel));
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
}
