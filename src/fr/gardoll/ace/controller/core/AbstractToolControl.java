package fr.gardoll.ace.controller.core;

import java.util.Collections ;
import java.util.HashSet ;
import java.util.Set ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.autosampler.Passeur ;
import fr.gardoll.ace.controller.pump.PousseSeringue ;
import fr.gardoll.ace.controller.valves.Valves ;

public abstract class AbstractToolControl implements ToolControl, ToolControlOperations
{
  private static final Logger _LOG = LogManager.getLogger(AbstractToolControl.class.getName());
  
  final Set<ControlPanel> _ctrlPanels = new HashSet<>();
  
  protected final PousseSeringue _pousseSeringue ;
  protected final Passeur _passeur ;
  protected final Valves _valves;
  
  protected final boolean _hasAutosampler;
  protected final boolean _hasPump;
  private final boolean _hasValves ;

  private ToolState _state = new InitialState(this);
  
  public AbstractToolControl(ParametresSession parametresSession,
                             boolean hasPump, boolean hasAutosampler,
                             boolean hasValves)
      throws InitializationException, InterruptedException
  {
    this._hasAutosampler = hasAutosampler;
    this._hasPump = hasPump;
    this._hasValves = hasValves;
    
    if(hasPump)
    {
      this._pousseSeringue = parametresSession.getPousseSeringue();
    }
    else
    {
      this._pousseSeringue = null;
    }
    
    if(hasAutosampler)
    {
      this._passeur = parametresSession.getPasseur();
    }
    else
    {
      this._passeur = null;
    }
    
    if(hasValves)
    {
      this._valves = parametresSession.getValves();
    }
    else
    {
      this._valves = null;
    }
  }
  
  @Override
  public Set<ControlPanel> getCtrlPanels()
  {
    return Collections.unmodifiableSet(this._ctrlPanels);
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
          AbstractToolControl.this.getState().askCancellation();
        }
        catch (Exception e)
        {
          String msg = "error while cancelling";
          _LOG.error(msg, e);
          // Don't change the state of the running thread
          // by calling AbstractToolControl.this.handleException.
          AbstractToolControl.this.notifyError(msg, e);
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
          AbstractToolControl.this.getState().reinit();
        }
        catch (Exception e)
        {
          String msg = "error while reinitializing";
          _LOG.fatal(msg, e);
          // Reinit takes place in the main thread, there isn't any operating
          // thread that is running. So it is safe to change the state here.
          AbstractToolControl.this.handleException(msg, e);
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
  public void pause()
  {  
    Runnable r = new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          AbstractToolControl.this.getState().askPausing();
        }
        catch (Exception e)
        {
          String msg = "error while pausing";
          _LOG.fatal(msg, e);
          // Don't change the state of the running thread
          // by calling AbstractToolControl.this.handleException.
          AbstractToolControl.this.notifyError(msg, e);
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
          AbstractToolControl.this.getState().askResuming() ;
        }
        catch (Exception e)
        {
          String msg = "error while resuming";
          _LOG.fatal(msg, e);
          // Resuming the operating thread is not possible, the operating thread
          // may never wake up from its pause.
          // So it is better to set the current state to crashed state.
          AbstractToolControl.this.handleException(msg, e);
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
          AbstractToolControl.this.getState().close();
        }
        catch (Exception e)
        {
          String msg = "error while running close operations";
          _LOG.fatal(msg, e);
          // close takes place in the main thread, there isn't any operating
          // thread that is running. So it is safe to change the state here.
          AbstractToolControl.this.handleException(msg, e);
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
    this._ctrlPanels.add(obs);
    this.getState().addControlPanel(obs);
  }

  @Override
  public void removeControlPanel(ControlPanel obs)
  {
    this._ctrlPanels.remove(obs);
    this.getState().removeControlPanel(obs);
  }
  
  @Override
  public void notifyAction(Action action)
  {
    for(ControlPanel panel: this._ctrlPanels)
    {
      panel.majActionActuelle(action);
    }
  }
  
  @Override
  public void displayControlPanelModalMessage(String msg)
  {
    for(ControlPanel panel: this._ctrlPanels)
    {
      panel.displayModalMessage(msg);
    }
  }
  
  @Override
  public void notifyError(String msg, Throwable e)
  {
    for(ControlPanel panel: this._ctrlPanels)
    {
      panel.reportError(msg, e);
    }
  }
  
  @Override
  public void notifyError(String msg)
  {
    for(ControlPanel panel: this._ctrlPanels)
    {
      panel.reportError(msg);
    }
  }
  
  protected void handleException(String msg, Exception e)
  {
    this.notifyError(msg, e);
    this.getState().crash();
  }
}
