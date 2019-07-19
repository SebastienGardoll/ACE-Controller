package fr.gardoll.ace.controller.core;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

public abstract class AbstractAdvancedToolControl extends AbstractBasicToolControl 
   implements ToolControl, ToolControlOperations
{
  private static final Logger _LOG = LogManager.getLogger(AbstractAdvancedToolControl.class.getName());
  
  public AbstractAdvancedToolControl(ParametresSession parametresSession,
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
          AbstractAdvancedToolControl.this.getState().askCancellation();
        }
        catch (Exception e)
        {
          String msg = "error while cancelling";
          _LOG.error(msg, e);
          // Don't change the state of the running thread
          // by calling AbstractAdvancedToolControl.this.handleException.
          AbstractAdvancedToolControl.this.notifyError(msg, e);
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
          AbstractAdvancedToolControl.this.getState().askPausing();
        }
        catch (Exception e)
        {
          String msg = "error while pausing";
          _LOG.fatal(msg, e);
          // Don't change the state of the running thread
          // by calling AbstractAdvancedToolControl.this.handleException.
          AbstractAdvancedToolControl.this.notifyError(msg, e);
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
          AbstractAdvancedToolControl.this.getState().askResuming() ;
        }
        catch (Exception e)
        {
          String msg = "error while resuming";
          _LOG.fatal(msg, e);
          // Resuming the operating thread is not possible, the operating thread
          // may never wake up from its pause.
          // So it is better to set the current state to crashed state.
          AbstractAdvancedToolControl.this.handleException(msg, e);
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
