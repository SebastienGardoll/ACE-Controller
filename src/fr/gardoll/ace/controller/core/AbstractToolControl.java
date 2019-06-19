package fr.gardoll.ace.controller.core;

import java.util.HashSet ;
import java.util.Set ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.autosampler.Passeur ;
import fr.gardoll.ace.controller.pump.PousseSeringue ;

public abstract class AbstractToolControl implements ToolControl
{
  private static final Logger _LOG = LogManager.getLogger(AbstractToolControl.class.getName());
  
  private final Set<ControlPanel> _ctrlPanels = new HashSet<>();
  
  protected final PousseSeringue _pousseSeringue ;
  protected final Passeur _passeur ;
  
  final boolean _hasAutosampler;
  final boolean _hasPump;

  private ToolState _state        = new InitialState(this, _ctrlPanels);
  
  public AbstractToolControl(ParametresSession parametresSession,
                             boolean hasPump, boolean hasAutosampler)
      throws InitializationException, InterruptedException
  {
    this._hasAutosampler = hasAutosampler;
    this._hasPump = hasPump;
    
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
          AbstractToolControl.this.getState().cancel();
        }
        catch (Exception e)
        {
          String msg = String.format("error while cancelling: %s", e.getMessage());
          _LOG.error(msg, e);
          AbstractToolControl.this.notifyError("error while cancelling", e);
        }
      }
    } ;
    
    new Thread(r).start();
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
          String msg = String.format("error while reinitializing: %s", e.getMessage());
          _LOG.fatal(msg, e);
          AbstractToolControl.this.notifyError("error while reinitializing", e);
        }
      }
    } ;
    
    new Thread(r).start();
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
          AbstractToolControl.this.getState().pause();
        }
        catch (Exception e)
        {
          String msg = String.format("error while pausing: %s", e.getMessage());
          _LOG.fatal(msg, e);
          AbstractToolControl.this.notifyError("error while pausing", e);
        }
      }
    } ;
    
    new Thread(r).start();
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
          AbstractToolControl.this.getState().resume() ;
        }
        catch (Exception e)
        {
          String msg = String.format("error while resuming operations: %s", e.getMessage());
          _LOG.fatal(msg, e);
          AbstractToolControl.this.notifyError("error while resuming", e);
        }
      }
    } ;
    
    new Thread(r).start();
  }

  // XXX Concurrent racing between AbstractThreadControl and user actions ?
  void setState(ToolState state)
  {
    this._state = state;
  }
  
  ToolState getState()
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
          String msg = String.format("error while running close operations: %s", e.getMessage());
          _LOG.fatal(msg, e);
          AbstractToolControl.this.notifyError("error while running close operations", e);
        }
      }
    } ;
    
    new Thread(r).start();
  }
  
  @Override
  public void addControlPanel(ControlPanel obs)
  {
    this._ctrlPanels.add(obs);
    this._state.addControlPanel(obs);
  }

  @Override
  public void removeControlPanel(ControlPanel obs)
  {
    this._ctrlPanels.remove(obs);
    this._state.removeControlPanel(obs);
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
}
