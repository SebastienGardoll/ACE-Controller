package fr.gardoll.ace.controller.core;

import java.util.Optional ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

public abstract class AbstractCancelableToolControl extends AbstractCloseableToolControl implements ToolControl
{
  private static final Logger _LOG = LogManager.getLogger(AbstractCancelableToolControl.class.getName());
  
  public AbstractCancelableToolControl(ParametresSession parametresSession,
                                  boolean hasPump, boolean hasAutosampler,
                                  boolean hasValves)
      throws InitializationException, InterruptedException
  {
    super(parametresSession, hasPump, hasAutosampler, hasValves);
  }

  @Override
  public void cancel()
  {
    Runnable r = new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          AbstractCancelableToolControl.this.getState().askCancellation();
        }
        catch (Exception e)
        {
          String msg = "cancel has crashed";
          _LOG.error(msg, e);
          // Don't change the state of the running thread
          // by calling AbstractStateFullToolControl.this.handleException.
          AbstractCancelableToolControl.this.notifyError(msg, e);
        }
      }
    } ;
    
    new Thread(r).start();
  }
  
  @Override
  public void cancelOperations() throws InterruptedException
  {
    _LOG.info("cancelling all operations");
    this.notifyAction(new Action(ActionType.CANCELING, null));
    
    if(this._hasAutosampler)
    {
      this._passeur.cancelAndReinit();
    }
    
    if(this._hasPump)
    {
      this._pousseSeringue.cancelAndReinit();
    }
    
    this.notifyAction(new Action(ActionType.CANCEL_DONE, null));
  }
  
  @Override
  // Thread must be terminated.
  public void reinit()
  {
    Runnable r = new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          AbstractCancelableToolControl.this.getState().reinit();
        }
        catch (Exception e)
        {
          String msg = "reinitializing has crashed";
          _LOG.fatal(msg, e);
          // Reinit takes place in the main thread, there isn't any operating
          // thread control that is running. So it is safe to change the state here.
          AbstractCancelableToolControl.this.handleException(msg, e);
        }
      }
    } ;
    
    new Thread(r).start();
  }
  
  @Override
  void reinitOperations() throws InterruptedException
  {
    _LOG.info("reinitializing all operations");
    this.notifyAction(new Action(ActionType.REINIT, Optional.empty()));
    
    if(this._hasAutosampler)
    {
      this._passeur.reinit();
    }
    
    if(this._hasPump)
    {
      this._pousseSeringue.reinit();
    }
    
    if(this._hasValves)
    {
      // Nothing to do.
    }
    
    this.notifyAction(new Action(ActionType.REINIT_DONE, Optional.empty()));
  }
  
  @Override
  void pauseOperations() throws InterruptedException
  {
    throw new UnsupportedOperationException("pause operations is not implemented");
  }
  
  @Override
  void resumeOperations() throws InterruptedException
  {
    throw new UnsupportedOperationException("resume operations is not implemented");
  }
}
