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
  
  private ThreadControl _threadCtrl = null;

  private boolean _isRunning ;
  
  private String _currentMode ;

  private double _currentVolW ;

  private double _currentVolI ;

  private double _currentRateI ;

  private double _currentRateW ;
  
  @Override
  public void setThreadControl(ThreadControl threadCtrl)
  {
    _LOG.debug("stubbing command setThreadControl");
    this._threadCtrl = threadCtrl;
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
  }

  @Override
  public void dia(double diametre)
      throws SerialComException, InterruptedException
  {
    _LOG.debug("stubbing command dia");
  }

  private double innerRunning(double volume, double rate)
  {
    return (volume - (rate / _TIME_FACTOR));
  }
  
  @Override
  public boolean running() throws SerialComException, InterruptedException
  {
    boolean result = false;
    if(false == this._isRunning)
    {
      result = false ;
    }
    else
    {
      double volume = 0.;
      
      if(this._currentMode.equals(_INFUSION))
      {
        this._currentVolI = innerRunning(this._currentVolI, this._currentRateI);
        volume = this._currentVolI;
      }
      else
      {
        this._currentVolW = innerRunning(this._currentVolW, this._currentRateW);
        volume = this._currentVolW;
      }
      
      result = volume <= 0. ;
    }
    
    _LOG.debug(String.format("stubbing command running: %s", result));
    
    return result;
  }

  @Override
  public double deliver() throws SerialComException, InterruptedException
  {
    // TODO Auto-generated method stub
    return 0 ;
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
