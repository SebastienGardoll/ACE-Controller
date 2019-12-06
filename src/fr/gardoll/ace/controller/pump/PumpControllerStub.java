package fr.gardoll.ace.controller.pump;

import java.io.Closeable ;
import java.io.IOException ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.SerialComException ;

public class PumpControllerStub implements Closeable, PumpController
{
  private static final Logger _LOG = LogManager.getLogger(PumpControllerStub.class.getName());

  private static final String _INFUSION    = "infusion" ;
  private static final String _WITHDRAWING = "withdrawing" ;
  
  // deliver 1 mL at the rate of 10 mL/min in 1 second for period of 0.1 seconds :
  // volume * _TIME_FACTOR = rate * elasting_time / period
  // _TIME_FACTOR = rate * elasting_time / (volume * period)
  // volume / period = rate / _TIME_FACTOR
  private static double _TIME_FACTOR = 100. ;
  
  private boolean _isRunning ;
  
  private String _currentMode ;

  private double _currentVolW ;

  private double _currentVolI ;
  
  private double _delivered;
  
  private double _currentRateI ;

  private double _currentRateW ;
  
  public PumpControllerStub()
  {
    _LOG.debug("instanciating pump controller stub");
  }
  
  @Override
  public void run() throws SerialComException, InterruptedException
  {
    _LOG.trace("stubbing command run");
    this._isRunning = true;
  }

  @Override
  public void stop() throws SerialComException, InterruptedException
  {
    _LOG.trace("stubbing command stop");
    this._isRunning = false;
  }

  @Override
  public void dia(double diametre)
      throws SerialComException, InterruptedException
  {
    _LOG.trace(String.format("stubbing command dia %s", diametre));
  }

  private double computeDeliveredVolume(double volume, double rate)
  {
    return (rate / _TIME_FACTOR);
  }
  
  @Override
  public boolean running() throws SerialComException, InterruptedException
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
      
      double deliveredVolume = computeDeliveredVolume(targetedVolume, rate);
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
    
    //_LOG.trace(String.format("stubbing command running: %s", this._isRunning));
    
    return this._isRunning;
  }

  @Override
  public double deliver() throws SerialComException, InterruptedException
  {
    _LOG.trace(String.format("stubbing command deliver %s", this._delivered));
    return this._delivered;
  }

  @Override
  public void ratei(double debit)
      throws SerialComException, InterruptedException
  {
    _LOG.trace(String.format("stubbing command ratei %s", debit));
    this._currentRateI = debit;
  }

  @Override
  public void ratew(double debit)
      throws SerialComException, InterruptedException
  {
    _LOG.trace(String.format("stubbing command ratew %s", debit));
    this._currentRateW = debit;
  }

  @Override
  public void voli(double volume)
      throws SerialComException, InterruptedException
  {
    _LOG.trace(String.format("stubbing command volI %s", volume));
    this._currentVolI = volume;
  }

  @Override
  public void volw(double volume)
      throws SerialComException, InterruptedException
  {
    _LOG.trace(String.format("stubbing command volw %s", volume));
    this._currentVolW = volume;
  }

  @Override
  public void modeI() throws SerialComException, InterruptedException
  {
    _LOG.trace("stubbing command modeI");
    this._currentMode = _INFUSION;
    this._delivered = 0.;
  }

  @Override
  public void modeW() throws SerialComException, InterruptedException
  {
    _LOG.trace("stubbing command modeW");
    this._currentMode = _WITHDRAWING;
    this._delivered = 0.;
  }

  @Override
  public void close() throws IOException
  {
    _LOG.debug("stubbing command close");
  }
}
