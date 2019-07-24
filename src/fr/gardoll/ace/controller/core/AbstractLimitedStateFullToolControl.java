package fr.gardoll.ace.controller.core;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

public class AbstractLimitedStateFullToolControl extends AbstractToolControl implements ToolControl, ToolControlOperations
{
  private static final Logger _LOG = LogManager.getLogger(AbstractLimitedStateFullToolControl.class.getName());
  
  private ToolState _state = new InitialState(this);
  
  public AbstractLimitedStateFullToolControl(ParametresSession parametresSession,
                                  boolean hasPump, boolean hasAutosampler,
                                  boolean hasValves)
      throws InitializationException, InterruptedException
  {
    super(parametresSession, hasPump, hasAutosampler, hasValves);
  }

  @Override
  public void cancelOperations() throws InterruptedException
  {
    throw new UnsupportedOperationException();
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
          AbstractLimitedStateFullToolControl.this.getState().reinit();
        }
        catch (Exception e)
        {
          String msg = "error while reinitializing";
          _LOG.fatal(msg, e);
          // Reinit takes place in the main thread, there isn't any operating
          // thread that is running. So it is safe to change the state here.
          AbstractLimitedStateFullToolControl.this.handleException(msg, e);
        }
      }
    } ;
    
    new Thread(r).start();
  }
  
  @Override
  public void reinitOperations() throws InterruptedException
  {
    _LOG.info("reinitializing all operations");
    this.notifyAction(new Action(ActionType.REINIT, null));
    
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
    
    this.notifyAction(new Action(ActionType.REINIT_DONE, null));
  }
  
  @Override
  public void pauseOperations() throws InterruptedException
  {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public void resumeOperations() throws InterruptedException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setState(ToolState state)
  {
    this._state = state;
  }
  
  @Override
  public ToolState getState()
  {
    return this._state;
  }
  
  protected void start(ThreadControl thread)
  {
    this.getState().start(thread);
  }
  
  @Override
  public void close()
  {
    Runnable r = new Runnable()
    {
      @Override
      public void run()
      {
        _LOG.debug("running the close operations");
        try
        {
          AbstractLimitedStateFullToolControl.this.getState().close();
        }
        catch (Exception e)
        {
          String msg = "error while running close operations";
          _LOG.fatal(msg, e);
          // close takes place in the main thread, there isn't any operating
          // thread that is running. So it is safe to change the state here.
          AbstractLimitedStateFullToolControl.this.handleException(msg, e);
        }
      }
    } ;
    
    new Thread(r).start();
  }
  
  @Override
  public void closeOperations()
  {
    _LOG.debug("controller has nothing to do while closing the tool");
    this.notifyAction(new Action(ActionType.CLOSING, null));
  }
  
  @Override
  public void addControlPanel(ControlPanel obs)
  {
    super.addControlPanel(obs);
    this.getState().addControlPanel(obs);
  }

  @Override
  public void removeControlPanel(ControlPanel obs)
  {
    super.removeControlPanel(obs);
    this.getState().removeControlPanel(obs);
  }
  
  @Override
  protected void handleException(String msg, Exception e)
  {
    super.handleException(msg, e);
    this.getState().crash();
  }
}
