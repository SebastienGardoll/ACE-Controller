package fr.gardoll.ace.controller.pump;

import java.io.Closeable ;
import java.io.IOException ;

import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.com.SerialComException ;
import fr.gardoll.ace.controller.core.Log ;
import fr.gardoll.ace.controller.settings.ConfigurationException ;
import fr.gardoll.ace.controller.settings.GeneralSettings ;

public class PumpControllerStub implements Closeable, PumpController
{
  private static final Logger _LOG = Log.STUB;

  private static final String _INFUSION    = "infusion" ;
  private static final String _WITHDRAWING = "withdrawing" ;
  
  // deliver 1 mL at the rate of 10 mL/min in 1 second for period of 0.1 seconds :
  // volume * TIME_FACTOR = rate * elasting_time / period
  // TIME_FACTOR = rate * elasting_time / (volume * period)
  // volume / period = rate / TIME_FACTOR
  public static double TIME_FACTOR = 35. ;
  
  private boolean _isRunning ;
  
  private String _currentMode ;

  private double _currentVolW ;

  private double _currentVolI ;
  
  private double _delivered;
  
  private double _currentRateI ;

  private double _currentRateW ;
  
  public PumpControllerStub() throws ConfigurationException
  {
    _LOG.debug("instanciating pump controller stub");
    double diametreSeringue = GeneralSettings.instance().getDiametreSeringue();
    try
    {
      this.dia(diametreSeringue);
    }
    catch (SerialComException e)
    {
      // Cannot happen.
    }
  }
  
  @Override
  public void run() throws SerialComException
  {
    _LOG.trace("command 'run'");
    this._isRunning = true;
  }

  @Override
  public void stop() throws SerialComException
  {
    _LOG.trace("command 'stop'");
    this._isRunning = false;
  }

  @Override
  public void dia(double diametre)
      throws SerialComException
  {
    double debitMaxIntrinseque = PumpController.debitMaxIntrinseque(diametre);
    _LOG.trace(String.format("computed rate max is '%s'", debitMaxIntrinseque));
    
    String formattedDiameter = InterfacePousseSeringue.formatage(diametre);
    _LOG.trace(String.format("command 'dia %s'", formattedDiameter));
  }

  private double computeDeliveredVolume(double rate)
  {
    return (rate / TIME_FACTOR);
  }
  
  @Override
  public boolean running() throws SerialComException
  {
    if(this._isRunning)
    {
      double targetedVolume = 0.;
      double rate = 0.;
      
      if(this._currentMode.equals(_INFUSION))
      {
        targetedVolume = this._currentVolI;
        rate = this._currentRateI;
      }
      else
      {
        targetedVolume = this._currentVolW;
        rate = this._currentRateW;
      }
      
      double deliveredVolume = computeDeliveredVolume(rate);
      this._delivered += deliveredVolume;
      
      if(this._delivered >= targetedVolume)
      {
        this._isRunning = false;
        this._delivered = targetedVolume;
      }
      else
      {
        this._isRunning = true;
      }
    }

    return this._isRunning;
  }

  @Override
  public double deliver() throws SerialComException
  {
    this.running(); // Simulates the pump.
    return this._delivered;
  }

  @Override
  public void ratei(double debit)
      throws SerialComException
  {
    String formattedRate = InterfacePousseSeringue.formatage(debit);
    _LOG.trace(String.format("command 'ratei %s ml/m'", formattedRate));
    this._currentRateI = debit;
  }

  @Override
  public void ratew(double debit)
      throws SerialComException
  {
    String formattedRate = InterfacePousseSeringue.formatage(debit);
    _LOG.trace(String.format("command 'ratew %s ml/m'", formattedRate));
    this._currentRateW = debit;
  }

  @Override
  public void voli(double volume)
      throws SerialComException
  {
    String ordre = InterfacePousseSeringue.forgeVolOrder("voli", volume);
    String msg = String.format("command '%s'", ordre.substring(0, ordre.length()-1));
    _LOG.trace(msg);
    this._currentVolI = volume;
  }

  @Override
  public void volw(double volume)
      throws SerialComException
  {
    String ordre = InterfacePousseSeringue.forgeVolOrder("volw", volume);
    String msg = String.format("command '%s'", ordre.substring(0, ordre.length()-1));
    _LOG.trace(msg);
    this._currentVolW = volume;
  }

  @Override
  public void modeI() throws SerialComException
  {
    _LOG.trace("command 'mode i'");
    this._currentMode = _INFUSION;
    this._delivered = 0.;
  }

  @Override
  public void modeW() throws SerialComException
  {
    _LOG.trace("command 'mode w'");
    this._currentMode = _WITHDRAWING;
    this._delivered = 0.;
  }

  @Override
  public void close() throws IOException
  {
    _LOG.debug("stubbing close pump controller");
  }

  @Override
  public String getPortPath()
  {
    return "stub_port";
  }
}
