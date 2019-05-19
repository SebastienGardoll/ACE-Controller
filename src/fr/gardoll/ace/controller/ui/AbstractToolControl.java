package fr.gardoll.ace.controller.ui;

import java.util.HashSet ;
import java.util.Set ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.autosampler.Passeur ;
import fr.gardoll.ace.controller.common.InitializationException ;
import fr.gardoll.ace.controller.common.ParametresSession ;
import fr.gardoll.ace.controller.core.ThreadControl ;
import fr.gardoll.ace.controller.pump.PousseSeringue ;

public abstract class AbstractToolControl implements ToolControl
{
  private static final Logger _LOG = LogManager.getLogger(AbstractToolControl.class.getName());
  
  private final Set<Observer> _observers     = new HashSet<>();
  private final Set<ControlPanel> _ctrlPanel = new HashSet<>();
  
  protected ThreadControl _currentThread = null;
  protected final PousseSeringue _pousseSeringue ;
  protected final Passeur _passeur ;

  private boolean _hasAutosampler ;

  private boolean _hasPump ;
  
  public AbstractToolControl(boolean hasPump, boolean hasAutosampler)
      throws InitializationException
  {
    // Initialization checking.
    ParametresSession parametresSession = ParametresSession.getInstance();
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

  private boolean checkThread()
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
  public void pause() throws InterruptedException
  {  
    if(this.checkThread())
    {
      _LOG.info("waiting for pause");
      this.notifyAction(new Action(ActionType.WAIT_PAUSE, null));
      
      this._currentThread.pause();
      
      _LOG.info("pausing");
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
  public void close() throws InterruptedException
  {
    this.cancel();
  }
  
  @Override
  public void enableControlPanel(boolean isEnable)
  {
    for(ControlPanel ctrlPanel: this._ctrlPanel)
    {
      ctrlPanel.enableControl(isEnable);
    }
  }
  
  @Override
  public void addControlPanel(ControlPanel obs)
  {
    this._ctrlPanel.add(obs);
  }

  @Override
  public void removeControlPanel(ControlPanel obs)
  {
    this._ctrlPanel.remove(obs);
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
  public void notifyError(String msg, Throwable e)
  {
    for(Observer panel: this._observers)
    {
      panel.reportError(msg, e);
    }
  }
}
