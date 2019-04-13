package fr.gardoll.ace.controller.pump;

import java.io.Closeable;
import java.math.RoundingMode ;
import java.text.DecimalFormat ;
import java.text.DecimalFormatSymbols ;
import java.util.Locale ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.comm.FlowControl ;
import fr.gardoll.ace.controller.comm.Parity ;
import fr.gardoll.ace.controller.comm.SerialCom;
import fr.gardoll.ace.controller.comm.StopBit;
import fr.gardoll.ace.controller.common.ConfigurationException ;
import fr.gardoll.ace.controller.common.InitializationException ;
import fr.gardoll.ace.controller.common.SerialComException ;

public class InterfacePousseSeringue  implements Closeable
{
  private final SerialCom _port;
  private final int _debitMaxIntrinseque;

  public final static char DecimalSeparator = '.' ;
  //caractéristique du pousse seringue en m/min
  public final static double COURCE_LINEAIRE_MAX = 0.1269 ;
  //diamètre de seringue maximum pour le pousse seringue en mm
  public final static int DIAMETRE_MAX = 140 ;
  
  private static final Logger _LOG = LogManager.getLogger(InterfacePousseSeringue.class.getName());
  
  private final static DecimalFormat[] _DOUBLE_FORMATTERS = new DecimalFormat[4];
  
  static
  {
    String[] formats = {"#.###", "##.##", "###.#", "####"} ;
    DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);
    
    for(int index=0 ; index < _DOUBLE_FORMATTERS.length ; index++)
    {
      _DOUBLE_FORMATTERS[index] = new DecimalFormat(formats[index]);
      _DOUBLE_FORMATTERS[index].setRoundingMode(RoundingMode.FLOOR);
      _DOUBLE_FORMATTERS[index].setDecimalSeparatorAlwaysShown(false);
      _DOUBLE_FORMATTERS[index].setDecimalFormatSymbols(dfs);
    }
  }
  
  public InterfacePousseSeringue(double diametreSeringue, SerialCom port)
      throws InitializationException
  {
    this._port = port ;
    
    // Initializing the serial port.
    try
    {
      this._port.setVitesse(9600) ;
      this._port.setByteSize (8);
      this._port.setStopBit(StopBit.ONESTOPBIT);
      this._port.setParite(Parity.NOPARITY);
      this._port.setControlFlux(FlowControl.XON_XOFF);
      this._port.setTimeOut(300) ;
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while initializing the pump serial port: %s",
          e.getMessage());
      _LOG.fatal(msg);
      throw new InitializationException(msg, e);
    }
 
    try
    {
      // contient le code de vérification.
      this._debitMaxIntrinseque = InterfacePousseSeringue.debitMaxIntrinseque (diametreSeringue);

      this.dia(diametreSeringue);
    }
    catch(SerialComException e)
    {
      String msg = "error while initializing the pumpe";
      _LOG.fatal(msg);
      throw new InitializationException(msg, e);
    }
  }
  
  private void traitementReponse(String message) throws SerialComException
  {
    if (message == "EE")
    {
      String msg = "pump serial communication failure" ;
      _LOG.error(msg);
      throw new SerialComException(msg) ;
    }

    else if (message == "NA::")
    {
      String msg = "unknown pump order" ;
      _LOG.error(msg);
      throw new SerialComException(msg) ;
    }

    else if (message == "")
    {
      String msg = "the pump did not acknowledgement the order" ;
      _LOG.error(msg);
      throw new SerialComException(msg) ;
    }
  }
  
  private String traitementOrdre(String ordre) throws SerialComException
  {
    this._port.ecrire(ordre) ;

    String reponse = this.lectureReponse() ;

    this.traitementReponse(reponse) ;

    return reponse  ;
  }
  
  private String lectureReponse() throws SerialComException
  {
    return this._port.lire();
  }
  
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
    
    return _DOUBLE_FORMATTERS[index].format(nombre);
  }
  
  public void run() throws SerialComException
  {
    this.traitementOrdre( "run\r" );
  }
  
  public void stop() throws SerialComException
  {
    this.traitementOrdre ("stop\r");
  }
  
  // Don't throw any exception as it is an emergency method.
  public void arretUrgence()
  {
    //précondition : le threadSequence doit être détruit ( pthread_cancel ) ou inexistant
    try
    {
      this._port.ecrire("stop\r") ;
      String reponse = this.lectureReponse() ;
      this.traitementReponse(reponse) ;
    }
    catch (SerialComException e)
    {
      String msg = "error while emergency stopping" ;
      _LOG.error(msg);
    }
  }
  
  public void dia(double diametre) throws SerialComException
  {
    String ordre = String.format("dia %s\r", formatage(diametre)) ;
    this.traitementOrdre (ordre);
  }
  
  public boolean running() throws SerialComException
  {
    boolean result = this.traitementOrdre("run?\r") != "::" ;
    return result;
  }
  
  public double deliver()
  {
    return 0;
  }
  
  public void ratei(double debit)
  {
    
  }
  
  public void ratew(double debit)
  {
    
  }
  
  public void voli(double volume)
  {
    
  }
  
  public void volw(double volume)
  {
    
  }
  
  public void modeI()
  {
    
  }
  
  public void modeW()
  {
    
  }
  
  public static int debitMaxIntrinseque(double diametreSeringue)
  {
    if (diametreSeringue <= 0)
    {
      String msg = String.format("the value of the syringe diameter (%s) cannot be negative or null",
                                 diametreSeringue) ;
      _LOG.fatal(msg);
      throw new ConfigurationException(msg);
    }
    else if (diametreSeringue > DIAMETRE_MAX)
    {
      String msg = String.format("the value of the syringe diameter (%s) cannot be superior than %s",
                                 diametreSeringue, DIAMETRE_MAX);
      _LOG.fatal(msg);
      throw new ConfigurationException (msg);
    }
    
    double result = Math.pow(diametreSeringue/2. , 2.) * Math.PI * COURCE_LINEAIRE_MAX  ;
    // arrondi par à l'entier inférieur à cause spec du pousse seringue .
    return (int) (result) ;
  }
  
  @Override
  public void close()
  {
    this._port.close() ;
  }
  
  public static void main(String[] args)
  {
    // TODO Auto-generated method stub

  }
}
