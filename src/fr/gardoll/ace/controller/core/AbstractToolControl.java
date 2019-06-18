package fr.gardoll.ace.controller.core;

import java.util.HashSet ;
import java.util.Set ;

import org.apache.commons.lang3.NotImplementedException ;
import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.autosampler.Passeur ;
import fr.gardoll.ace.controller.pump.PousseSeringue ;

public abstract class AbstractToolControl implements ToolControl
{
  private static final Logger _LOG = LogManager.getLogger(AbstractToolControl.class.getName());
  
  private final Set<Observer> _observers     = new HashSet<>();
  private final Set<ControlPanel> _ctrlPanel = new HashSet<>();
  
  protected ThreadControl _currentThread = null;
  protected final PousseSeringue _pousseSeringue ;
  protected final Passeur _passeur ;
  
  private boolean _hasAutosampler = false;
  private boolean _hasPump        = false;

  private ToolState _state        = new InitialState(this, _ctrlPanel);
  
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

  protected void setThread(ThreadControl thread)
  {
    _LOG.debug(String.format("setting thread '%s'", thread)) ;
    this._currentThread = thread;
  }
  
  protected boolean checkThread()
  {
    return this._currentThread != null && this._currentThread.isAlive();
  }
  
  @Override
  public void cancel() throws InterruptedException
  {
    if (this.checkThread())
    {
      _LOG.info("waiting for cancellation");
      this.notifyAction(new Action(ActionType.WAIT_CANCEL, null));
      this._currentThread.cancel();
    }
    else
    {
      String msg = "thread control is not alive or is null";
      _LOG.debug(msg);
    }
    
    try
    {
      _LOG.info("cancelling all operations");
      this.notifyAction(new Action(ActionType.CANCEL, null));
      
      if(this._hasAutosampler)
      {
        this._passeur.cancel();
      }
      
      if(this._hasPump)
      {
        this._pousseSeringue.cancel();
      }
      
    }
    catch (InterruptedException e)
    {
      String msg = "cancellation has been interrupted";
      _LOG.fatal(msg);
      return ;
    }
  }
  
  @Override
  // Thread must be terminated.
  public void reinit() throws InterruptedException
  {
    try
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
      
    }
    catch (InterruptedException e)
    {
      String msg = "reinitializing has been interrupted";
      _LOG.fatal(msg);
      return ;
    }
  }
  
  @Override
  public void pause() throws InterruptedException
  {  
    if(this.checkThread())
    {
      _LOG.info("waiting for pause");
      this.notifyAction(new Action(ActionType.WAIT_PAUSE, null));
      
      this._currentThread.pause();
      
      _LOG.info("running pause operations");
      this.notifyAction(new Action(ActionType.PAUSE, null));
      
      if(this._hasPump)
      {
        this._pousseSeringue.pause();    
      }
      
      if(this._hasAutosampler)
      {
        this._passeur.pause();
      }
    }
    else
    {
      String msg = "thread control is not alive or is null";
      _LOG.debug(msg);
    }
  }
  
  @Override
  public void unPause() throws InterruptedException
  {  
    if(this.checkThread())
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
      
      this._currentThread.unPause();
    }
    else
    {
      String msg = "thread control is not alive or is null";
      _LOG.debug(msg);
    }
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
  
  @Override
  public void cancelOnPause() throws InterruptedException
  {
    throw new NotImplementedException("cancel on pause is not implemented yet");
  }
  
  @Override
  public void close() throws InterruptedException
  {
    _LOG.debug("controller has nothing to do while closing the tool");
  }
  
  @Override
  public void addControlPanel(ControlPanel obs)
  {
    this._ctrlPanel.add(obs);
    this._state.addControlPanel(obs);
  }

  @Override
  public void removeControlPanel(ControlPanel obs)
  {
    this._ctrlPanel.remove(obs);
    this._state.removeControlPanel(obs);
  }
  
  @Override
  public void addObserver(Observer obs)
  {
    this._observers.add(obs);
  }

  @Override
  public void removeObserver(Observer obs)
  {
    this._observers.remove(obs);
  }

  @Override
  public void notifyAction(Action action)
  {
    for(Observer panel: this._observers)
    {
      panel.majActionActuelle(action);
    }
  }
  
  @Override
  public void displayControlPanelModalMessage(String msg)
  {
    for(Observer panel: this._observers)
    {
      panel.displayModalMessage(msg);
    }
  }
  
  @Override
  public void notifyError(String msg, Throwable e)
  {
    for(Observer panel: this._observers)
    {
      panel.reportError(msg, e);
    }
  }
  
  @Override
  public void notifyError(String msg)
  {
    for(Observer panel: this._observers)
    {
      panel.reportError(msg);
    }
  }
}
