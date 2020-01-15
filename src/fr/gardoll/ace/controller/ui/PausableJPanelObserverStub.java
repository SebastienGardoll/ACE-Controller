package fr.gardoll.ace.controller.ui;

import java.util.concurrent.Semaphore ;

import javax.swing.SwingUtilities ;

import fr.gardoll.ace.controller.core.Log ;
import fr.gardoll.ace.controller.core.ToolControl ;

public class PausableJPanelObserverStub extends AbstractPausableJPanelObserver
{
  private final Semaphore _syncMove = new Semaphore(1);
  private final Semaphore _syncPause = new Semaphore(1);
  
  public PausableJPanelObserverStub(ToolControl ctrl)
  {
    super(ctrl) ;
    this._syncMove.drainPermits();
    this._syncPause.drainPermits();
  }

  private static final long serialVersionUID = 4820020174928178132L ;
  
  public void waitPause()
  {
    try
    {
      this._syncPause.acquire();
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  public void waitMove()
  {
    try
    {
      this._syncMove.acquire();
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void enablePauseControl(boolean isEnable)
  {
    // Nothing to do.
  }

  @Override
  protected void enableResumeControl(boolean isEnable)
  {

    if(isEnable)
    {
      this._syncPause.release();
    }
    else
    {
      this._syncPause.drainPermits();
    }
  }

  @Override
  protected void enableReinitControl(boolean isEnable)
  {
    // Nothing to do.
  }

  @Override
  protected void enableStartControl(boolean isEnable)
  {
    // Nothing to do.
  }

  @Override
  protected void enableCancelControl(boolean isEnable)
  {
    // Nothing to do.
  }

  @Override
  protected void displayToUserLogSys(String msg)
  {
    Log.UI.trace(String.format("display '%s'", msg.strip()));
  }

  @Override
  protected void enableCloseControl(boolean isEnable)
  {
    if(isEnable)
    {
      this._syncMove.release();
    }
    else
    {
      this._syncMove.drainPermits();
    }
  }
  
  @Override
  protected void enableCarouselControl(boolean isEnable)
  {
    // Nothing to do.
  }
  
  @Override
  public void dispose()
  {
    // Call for enableStartControl is always executed by the thread AWT.
    // At the end of the teardown of a test, dispose and enableStartControl
    // are racing against each other for releasing/draining the permit of 
    // _syncMove. So running dispose by AWT makes sure that dispose is called
    // always after enableStartControl. 
    SwingUtilities.invokeLater(()->
    {
      this._syncMove.release();
    });
  }
}
