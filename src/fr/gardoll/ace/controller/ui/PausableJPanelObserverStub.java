package fr.gardoll.ace.controller.ui;

import java.util.concurrent.Semaphore ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.ToolControl ;

public class PausableJPanelObserverStub extends AbstractPausableJPanelObserver
{
  private static final Logger _LOG = LogManager.getLogger(PausableJPanelObserverStub.class.getName());
  
  private final Semaphore _lock = new Semaphore(1);
  
  public PausableJPanelObserverStub(ToolControl ctrl)
  {
    super(ctrl) ;
    this._lock.drainPermits();
  }

  private static final long serialVersionUID = 4820020174928178132L ;
  
  public void waitPanel()
  {
    try
    {
      this._lock.acquire();
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }

  @Override
  protected void enablePauseControl(boolean isEnable)
  {
    _LOG.trace(String.format("pause control set to %s", isEnable));
  }

  @Override
  protected void enableResumeControl(boolean isEnable)
  {
    _LOG.trace(String.format("resume control set to %s", isEnable));
  }

  @Override
  protected void enableReinitControl(boolean isEnable)
  {
    _LOG.trace(String.format("reinit control set to %s", isEnable));
  }

  @Override
  protected void enableStartControl(boolean isEnable)
  {
    _LOG.trace(String.format("start control set to %s", isEnable));
    if(isEnable)
    {
      this._lock.release();
    }
    else
    {
      this._lock.drainPermits();
    }
  }

  @Override
  protected void enableCancelControl(boolean isEnable)
  {
    _LOG.trace(String.format("cancel control set to %s", isEnable));
  }

  @Override
  protected void displayToUserLogSys(String msg)
  {
    _LOG.debug(String.format("display '%s'", msg.strip()));
  }

  @Override
  protected void enableCloseControl(boolean isEnable)
  {
    _LOG.trace(String.format("close control set to %s", isEnable));
  }
  
  @Override
  public void dispose()
  {
    this._lock.release();
  }
}
