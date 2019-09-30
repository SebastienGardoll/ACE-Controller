package fr.gardoll.ace.controller.core;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

public abstract class AbstractPausableToolControl extends AbstractCancelableToolControl 
                                                          implements ToolControl
{
  private static final Logger _LOG = LogManager.getLogger(AbstractPausableToolControl.class.getName());
  
  public AbstractPausableToolControl(ParametresSession parametresSession,
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
          AbstractPausableToolControl.this.getState().askCancellation();
        }
        catch (Exception e)
        {
          String msg = "cancel has crashed";
          _LOG.error(msg, e);
          // Don't change the state of the running thread
          // by calling AbstractStateFullToolControl.this.handleException.
          AbstractPausableToolControl.this.notifyError(msg, e);
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
      this._passeur.cancel();
    }
    
    if(this._hasPump)
    {
      this._pousseSeringue.cancel();
    }
    
    this.notifyAction(new Action(ActionType.CANCEL_DONE, null));
  }
  
  @Override
  public void pause()
  {  
    Runnable r = new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          AbstractPausableToolControl.this.getState().askPausing();
        }
        catch (Exception e)
        {
          String msg = "pause has crashed";
          _LOG.fatal(msg, e);
          // Don't change the state of the running thread
          // by calling AbstractStateFullToolControl.this.handleException.
          AbstractPausableToolControl.this.notifyError(msg, e);
        }
      }
    } ;
    
    new Thread(r).start();
  }
  
  @Override
  public void pauseOperations() throws InterruptedException
  {
    _LOG.info("running pause operations");
    this.notifyAction(new Action(ActionType.PAUSING, null));
    
    if(this._hasPump)
    {
      this._pousseSeringue.pause();    
    }
    
    if(this._hasAutosampler)
    {
      this._passeur.pause();
    }
    
    this.notifyAction(new Action(ActionType.PAUSE_DONE, null));
  }
  
  @Override
  public void resume()
  {  
    Runnable r = new Runnable()
    {
      @Override
      public void run()
      {
        _LOG.debug("running the resume operations");
        try
        {
          AbstractPausableToolControl.this.getState().askResuming() ;
        }
        catch (Exception e)
        {
          String msg = "resuming has crashed";
          _LOG.fatal(msg, e);
          // Resuming the operating thread is not possible, the operating thread
          // may never wake up from its pause.
          // So it is better to set the current state to crashed state.
          AbstractPausableToolControl.this.handleException(msg, e);
        }
      }
    } ;
    
    new Thread(r).start();
  }
  
  @Override
  public void resumeOperations() throws InterruptedException
  {
    _LOG.info("resuming from pause");
    this.notifyAction(new Action(ActionType.RESUME, null));
    
    if(this._hasAutosampler)
    {
      this._passeur.reprise(false); 
    }

    //attention la reprise du passeur avant celle du pousse seringue à
    //cause de la manipulation eventuelle de celui ci
    if(this._hasPump)
    {
      this._pousseSeringue.reprise(); 
    }
    
    this.notifyAction(new Action(ActionType.RESUME_DONE, null));
  }
}
