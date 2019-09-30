package fr.gardoll.ace.controller.pump;

import java.io.Closeable;
import java.math.RoundingMode ;
import java.nio.charset.Charset ;
import java.text.DecimalFormat ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.com.FlowControl ;
import fr.gardoll.ace.controller.com.JSerialComm ;
import fr.gardoll.ace.controller.com.Parity ;
import fr.gardoll.ace.controller.com.SerialCom ;
import fr.gardoll.ace.controller.com.SerialMode ;
import fr.gardoll.ace.controller.com.StopBit ;
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.SerialComException ;
import fr.gardoll.ace.controller.core.ThreadControl ;

//TODO: singleton.
public class InterfacePousseSeringue  implements Closeable, PumpController
{
  private static final int OPENING_DELAY = 2000;
  
  private static final Pattern _DELIVER_PATTERN = Pattern.compile("([0-9.]+)\\s+(u|m)l\\s+(:|>|<)");
  private static final Pattern _NA_PATTERN = Pattern.compile("NA\\s+(:|>|<)");
  
  private static final Logger _LOG = LogManager.getLogger(InterfacePousseSeringue.class.getName());
  
  private final static DecimalFormat[] _DOUBLE_FORMATTERS = new DecimalFormat[4];
  
  private final SerialCom _port;
  
  private ThreadControl _threadCtrl = null;
  
  // dépendant uniquement du diametre du type de seringue utilisé.
  private final int _debitMaxIntrinseque;
  
  static
  {
    String[] formats = {"#.###", "##.##", "###.#", "####"} ;
        
    for(int index=0 ; index < _DOUBLE_FORMATTERS.length ; index++)
    {
      _DOUBLE_FORMATTERS[index] = new DecimalFormat(formats[index]);
      _DOUBLE_FORMATTERS[index].setRoundingMode(RoundingMode.FLOOR);
      _DOUBLE_FORMATTERS[index].setDecimalSeparatorAlwaysShown(false);
      _DOUBLE_FORMATTERS[index].setDecimalFormatSymbols(DECIMAL_SYMBOLS);
    }
  }
  
  // requires 0 < diametreSeringue <= DIAMETRE_MAX
  public InterfacePousseSeringue(SerialCom port, double diametreSeringue)
      throws InitializationException
  {
    _LOG.debug(String.format("initializing the pump interface with the serial port %s and the syringe diameter %s",
        port.getId(), diametreSeringue));
    this._port = port ;
    
    // Initializing the serial port (already opened).
    try
    {
      _LOG.debug(String.format("setting the pump com port '%s'", this._port.getId()));
      this._port.setReadBufferSize(256);
      this._port.setMode(SerialMode.FULL_BLOCKING, SerialMode.FULL_BLOCKING);
      this._port.setCharset(Charset.forName("ASCII"));
      this._port.setVitesse(9600) ;
      this._port.setByteSize (8);
      this._port.setStopBit(StopBit.ONESTOPBIT);
      this._port.setParite(Parity.NOPARITY);
      this._port.setControlFlux(FlowControl.XON_XOFF);
      this._port.setTimeOut(300) ;
      this._port.open(OPENING_DELAY);
      
    }
    catch(SerialComException | InterruptedException e)
    {
      String msg = "error while initializing the pump serial port";
      _LOG.fatal(msg, e);
      throw new InitializationException(msg, e);
    }
 
    try
    {
      // contient le code de vérification.
      this._debitMaxIntrinseque = PumpController.debitMaxIntrinseque(diametreSeringue);
      _LOG.debug(String.format("computed rate max is '%s'", this._debitMaxIntrinseque));
      this.dia(diametreSeringue);
    }
    catch(SerialComException|InterruptedException e)
    {
      String msg = "error while initializing the pump";
      _LOG.fatal(msg, e);
      throw new InitializationException(msg, e);
    }
  }
  
  @Override
  public void setThreadControl(ThreadControl threadCtrl)
  {
    this._threadCtrl = threadCtrl;
  }
  
  //traitement de la réponse de l'interface en cas d'erreur => exception.
  private void traitementReponse(String message) throws SerialComException
  {
    _LOG.trace(String.format("ack received: '%s'", message));
    
    if (message.matches("E+"))
    {
      String msg = "pump failure" ;
      _LOG.error(msg);
      throw new SerialComException(msg) ;
    }
    else if (_NA_PATTERN.matcher(message).matches())
    {
      String msg = "unknown pump order" ;
      _LOG.error(msg);
      throw new SerialComException(msg) ;
    }
    else if (message == "")
    {
      String msg = "pump disconnection" ;
      _LOG.error(msg);
      throw new SerialComException(msg) ;
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
  
  //renvoie la réponse de l'interface
  private String traitementOrdre(String ordre) throws SerialComException,
                                                      InterruptedException
  {
    this.checkThreadCtrl();
    this._port.ecrire(ordre) ;
    String reponse = this.lectureReponse() ;
    this.traitementReponse(reponse) ;
    return reponse  ;
  }
  
  private String lectureReponse() throws SerialComException
  {
    return this._port.lire().strip();
  }
  
  // transforme float en string mais
  // où , est transformée en .  (séparateur des réels)
  // et format le nombre pour qu'il n'y ait que 4 chiffres au plus.
  private String formatage(double nombre)
  {
    // le pousse seringue n'acceptant que des nombres à 4 chiffres au plus ,
    // sans compter le séparateur décimal
    // qui doit être un point, le paramètre nombre doit être formaté en conséquence.
    
    int truncatedValue = (int)nombre;
    int index = 0;
    
    if (truncatedValue < 10)
    {
      index = 0;
    }
    else if(truncatedValue < 100)
    {
      index = 1;
    }
    else if(truncatedValue < 1000)
    {
      index = 2;
    }
    else if(truncatedValue < 10000)
    {
      index = 3; 
    }
    else
    {
      String msg = String.format("unsupported format number '%s'", nombre);
      _LOG.fatal(msg);
      throw new RuntimeException(msg);
    }
    
    String result = _DOUBLE_FORMATTERS[index].format(nombre);
    _LOG.debug(String.format("formatting: from '%s' to '%s'", nombre, result));
    return result;
  }
  
  //reprise ou démarrage
  @Override
  public void run() throws SerialComException, InterruptedException
  {
    _LOG.debug("running the pump");
    this.traitementOrdre( "run\r" );
  }
  
  // pause or cancel
  @Override
  public void stop() throws SerialComException, InterruptedException
  {
    _LOG.debug("stopping the pump");
    this.traitementOrdre ("stop\r");
  }
  
  // en mm requires diametre > 0
  @Override
  public void dia(double diametre) throws SerialComException, InterruptedException
  {
    _LOG.debug(String.format("setting the diameter to '%s'", diametre));
    String ordre = String.format("dia %s\r", formatage(diametre)) ;
    this.traitementOrdre (ordre);
  }
  
  @Override
  public boolean running() throws SerialComException, InterruptedException
  {
    String ack = this.traitementOrdre("run?\r");
    boolean result = false == ack.equals(":") ;
    _LOG.trace(String.format("is pump running: %s", result));
    return result;
  }
  
  // en mL
  //Attention ne revoie un réel que si le volume à délivré en est un.
  // Ainsi : 1. ou 1.0 ne donnera pas de réponse en réel donc la réponse sera
  // 0 puis 1 à la fin !!!! Il n'y a donc aucun intérêt.
  // Parade : passer en micro litre quand < 10 mL.
  @Override
  public double deliver() throws SerialComException, InterruptedException
  {
    String rawMessage = this.traitementOrdre("del?\r");
    return innerDeliver(rawMessage);
  }
  
  private static double innerDeliver(String rawMessage) throws SerialComException
  {
    _LOG.debug(String.format("delivered brut msg: '%s'", rawMessage));
    
    Matcher m = _DELIVER_PATTERN.matcher(rawMessage);
    
    double result = 0.;
    
    if(m.matches())
    {
      result = Double.valueOf(m.group(1));
      
      if(m.group(2).equals("u"))
      {
        result /= 1000. ;
      }
    }
    else
    {
      String msg = String.format("cannot interpret delivered volume '%s'", rawMessage);
      _LOG.fatal(msg);
      throw new SerialComException(msg);
    }
    
    _LOG.debug(String.format("the delivered volume is %s", result));
    
    return result;
  }
  
  // en mL/min   requires 0 < debit <= _debitMaxIntrinseque
  @Override
  public void ratei(double debit) throws SerialComException, InterruptedException
  {
    _LOG.debug(String.format("setting the infusion rate to '%s'", debit));
    if (debit <= 0.)
    {
      String msg = String.format("the value of the rate '%s' cannot be negative or null",
                                 debit);
      _LOG.fatal(msg);
      throw new RuntimeException(msg) ;
    }
    else if (debit > this._debitMaxIntrinseque)
    {
      String msg = String.format("the value of the rate '%s' cannot be greater than %s mL/min",
                                 debit, this._debitMaxIntrinseque);
      _LOG.fatal(msg);
      throw new RuntimeException(msg) ;
    }
    
    String ordre = String.format("ratei %s ml/m\r", formatage(debit)) ;
    this.traitementOrdre(ordre) ;
  }
  
  //en mL/min   requires 0 < debit <= _debitMaxIntrinseque
  @Override
  public void ratew(double debit) throws SerialComException, InterruptedException
  {
    _LOG.debug(String.format("setting the withdrawing rate to '%s'", debit));
    if (debit <= 0.)
    {
      String msg = String.format(
          "the value of the rate '%s' cannot be negative or null", debit) ;
      _LOG.fatal(msg) ;
      throw new RuntimeException(msg) ;
    }
    else if (debit > this._debitMaxIntrinseque)
    {
      String msg = String.format(
          "the value of the rate '%s' cannot be greater than %s mL/min", debit,
          this._debitMaxIntrinseque) ;
      _LOG.fatal(msg) ;
      throw new RuntimeException(msg) ;
    }

    String ordre = String.format("ratew %s ml/m\r", formatage(debit));
    this.traitementOrdre(ordre) ;
  }
  
  // en mL seulement 4 caractères sans compter la virgule.
  // requires volume > 0
  @Override
  public void voli(double volume) throws SerialComException, InterruptedException
  {
    _LOG.debug(String.format("setting the infusion volume to '%s'", volume));
    if (volume <= 0.)
    {
      String msg = String.format("the value of the volume '%s' cannot be negative or null",
                                 volume);
      _LOG.fatal(msg);
      throw new RuntimeException(msg) ;
    }

    String ordre = null;

    if (volume < 10.)
    {
      // permet le suivie du volume délivré si <1 ml voir la fonction deliver.
      ordre = String.format("voli %s ul\r", formatage((volume * 1000))) ;
    }
    else
    {
      ordre = String.format("voli %s ml\r", formatage(volume));
    }

    this.traitementOrdre(ordre) ;
  }
  
  // en mL seulement 4 caractères sans compter la virgule.
  // requires volume > 0
  @Override
  public void volw(double volume) throws SerialComException, InterruptedException
  {
    _LOG.debug(String.format("setting the withdrawing volume to '%s'", volume));
    if (volume <= 0.)
    {
      String msg = String.format("the value of the volume '%s' cannot be negative or null",
                                 volume);
      _LOG.fatal(msg);
      throw new RuntimeException(msg) ;
    }

    String ordre = null;

    if (volume < 10.)
    {
      // permet le suivie du volume délivré si <1 ml voir la fonction deliver.
      ordre = String.format("volw %s ul\r", formatage(volume * 1000));
    }
    else
    {
      ordre = String.format("volw %s ml\r", formatage(volume));
    }

    this.traitementOrdre(ordre) ;
  }
  
  @Override
  public void modeI() throws SerialComException, InterruptedException
  {
    _LOG.debug("setting the infusion mode");
    this.traitementOrdre("mode i\r") ; 
  }
  
  @Override
  public void modeW() throws SerialComException, InterruptedException
  {
    _LOG.debug("setting the withdrawing mode");
    this.traitementOrdre("mode w\r") ;
  }
  
  @Override
  public void close()
  {
    _LOG.debug(String.format("closing pump controller with port id '%s'", this._port.getId()));
    this._port.close() ;
  }
  
  public static void main(String[] args)
  {
    // Windows 10: "COM5"
    // CentOS   7: "/dev/ttyUSB1"
    String portPath = "/dev/ttyUSB1"; // To be modified.
    JSerialComm port = new JSerialComm(portPath);
    
    try(InterfacePousseSeringue pumpInt = new InterfacePousseSeringue(port, 14.25))
    {
      boolean isRunning = pumpInt.running();
      _LOG.info(String.format("is runnning: %s", isRunning)) ;
      
      _LOG.info("set mode infusion") ;
      pumpInt.modeI();
      
      double ratei = 15. ;
      _LOG.debug(String.format("set ratei to %s", ratei));
      pumpInt.ratei(ratei);
      
      double voli = 11. ;
      _LOG.info(String.format("set the voli to %s", voli));
      pumpInt.voli(voli);
      
      _LOG.info("run");
      pumpInt.run();
      
      isRunning = pumpInt.running();
      _LOG.info(String.format("is runnning: %s", isRunning)) ;
      
      Thread.sleep(1000);
      
      double deliver = pumpInt.deliver();
      _LOG.info(String.format("deliver: %s", deliver));
      
      Thread.sleep(1000);
      
      _LOG.info("stopping pump");
      pumpInt.stop();
      
      isRunning = pumpInt.running();
      _LOG.info(String.format("is runnning: %s", isRunning)) ;
      
      _LOG.info("set mode withdrawal") ;
      pumpInt.modeW();
      
      double ratew = 1. ;
      _LOG.debug(String.format("set ratew to %s", ratew));
      pumpInt.ratew(ratew);
      
      double volw = 9. ;
      _LOG.info(String.format("set the volw to %s", volw));
      pumpInt.volw(volw);
      
      _LOG.info("run");
      pumpInt.run();
      
      isRunning = pumpInt.running();
      _LOG.info(String.format("is runnning: %s", isRunning)) ;
      
      Thread.sleep(1200);
      
      deliver = pumpInt.deliver();
      _LOG.info(String.format("deliver: %s", deliver));
      
      Thread.sleep(1000);
      
      _LOG.info("stopping pump");
      pumpInt.stop();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
}
