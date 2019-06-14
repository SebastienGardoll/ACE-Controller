package fr.gardoll.ace.controller.pump;

import java.io.Closeable ;
import java.io.IOException ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.SerialComException ;
import fr.gardoll.ace.controller.core.ThreadControl ;

public class PumpControllerStub implements Closeable, PumpController
{
  private static final Logger _LOG = LogManager.getLogger(PumpControllerStub.class.getName());

  private static final String _INFUSION    = "infusion" ;
  private static final String _WITHDRAWING = "withdrawing" ;
  
  // deliver 1 mL at the rate of 10 mL/min in 10 seconds for period of 0.1 seconds :
  // volume * _TIME_FACTOR = rate * elasting_time / period
  // _TIME_FACTOR = rate * elasting_time / (volume * period)
  // volume / period = rate / _TIME_FACTOR
  private static double _TIME_FACTOR = 1000. ;
  
  private boolean _isRunning ;
  
  private String _currentMode ;

  private double _currentVolW ;

  private double _currentVolI ;
  
  private double _deliveredVolI;
  
  private double _deliveredVolW;

  private double _currentRateI ;

  private double _currentRateW ;
  
  @Override
  public void setThreadControl(ThreadControl threadCtrl)
  {
    _LOG.debug("stubbing command setThreadControl");
  }

  @Override
  public void run() throws SerialComException, InterruptedException
  {
    _LOG.debug("stubbing command run");
    this._isRunning = true;
  }

  @Override
  public void stop() throws SerialComException, InterruptedException
  {
    _LOG.debug("stubbing command stop");
    this._isRunning = false;
  }

  @Override
  public void dia(double diametre)
      throws SerialComException, InterruptedException
  {
    _LOG.debug("stubbing command dia");
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
      if(this._currentMode.equals(_INFUSION))
      {
        double deliveredVolume = computeDeliveredVolume(this._currentVolI, this._currentRateI);
        this._deliveredVolI += deliveredVolume;
        
        if(this._deliveredVolI >= this._currentVolI)
        {
          this._deliveredVolI = 0.;
          this._isRunning = false;
        }
        else
        {
          this._isRunning = true;
        }
      }
      else
      {
        double deliveredVolume = computeDeliveredVolume(this._currentVolW, this._currentRateW);
        this._deliveredVolW += deliveredVolume;
        
        if(this._deliveredVolW >= this._currentVolW)
        {
          this._deliveredVolW = 0.;
          this._isRunning = false;
        }
        else
        {
          this._isRunning = true;
        }
      }
    }
    
    _LOG.debug(String.format("stubbing command running: %s", this._isRunning));
    
    return this._isRunning;
  }

  @Override
  public double deliver() throws SerialComException, InterruptedException
  {
    double result = 0.;
    
    if(this._currentMode.equals(_INFUSION))
    {
      result = this._deliveredVolI;
    }
    else
    {
      result = this._deliveredVolW;
    }
    
    _LOG.debug(String.format("stubbing command deliver: %s", result));
    
    return result;
  }

  @Override
  public void ratei(double debit)
      throws SerialComException, InterruptedException
  {
    _LOG.debug("stubbing command ratei");
    this._currentRateI = debit;
  }

  @Override
  public void ratew(double debit)
      throws SerialComException, InterruptedException
  {
    _LOG.debug("stubbing command ratew");
    this._currentRateW = debit;
  }

  @Override
  public void voli(double volume)
      throws SerialComException, InterruptedException
  {
    _LOG.debug("stubbing command volI");
    this._currentVolI = volume;
  }

  @Override
  public void volw(double volume)
      throws SerialComException, InterruptedException
  {
    _LOG.debug("stubbing command volw");
    this._currentVolW = volume;
  }

  @Override
  public void modeI() throws SerialComException, InterruptedException
  {
    _LOG.debug("stubbing command modeI");
    this._currentMode = _INFUSION;
  }

  @Override
  public void modeW() throws SerialComException, InterruptedException
  {
    _LOG.debug("stubbing command modeW");
    this._currentMode = _WITHDRAWING;
  }

  @Override
  public void close() throws IOException
  {
    _LOG.debug("stubbing command close");
  }
}
