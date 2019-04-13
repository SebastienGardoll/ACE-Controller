package fr.gardoll.ace.controller.pump;

import java.io.Closeable;

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
    
    // contient le code de vérification.
    this._debitMaxIntrinseque = debitMaxIntrinseque (diametreSeringue);
    
    dia(diametreSeringue);
  }
  
  private void traitementReponse(String message)
  {
    
  }
  
  private String traitementOrdre(String ordre)
  {
    return null;
  }
  
  private String lectureReponse()
  {
    return null;
  }
  
  private String formatage(double nombre)
  {
    return null;
  }
  
  public void run()
  {
    
  }
  
  public void stop()
  {
    
  }
  
  public void arretUrgence()
  {
    
  }
  
  public void dia(double diametre)
  {
    String ordre = String.format("dia %s\\r", formatage(diametre)) ;
    traitementOrdre ( ordre );
  }
  
  public boolean running()
  {
    return false;
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
