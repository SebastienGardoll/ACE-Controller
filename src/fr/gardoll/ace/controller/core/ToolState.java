package fr.gardoll.ace.controller.core;

import java.util.Collections ;
import java.util.Set ;

import org.apache.commons.lang3.NotImplementedException ;
import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

interface ToolState extends ControlPanelHandler
{
  public void pause() throws InterruptedException;
  public void resume() throws InterruptedException;
  public void cancel() throws InterruptedException;
  public void close() throws InterruptedException;
  public void reinit() throws InterruptedException;
  public void start(ThreadControl thread);
  public void done() ;
  public void crash();
}

abstract class AbstractState implements ToolState
{
  private static final Logger _LOG = LogManager.getLogger(AbstractState.class.getName());
  
  protected Set<ControlPanel> _panels = Collections.emptySet();
  protected AbstractToolControl _ctrl = null;
  protected ThreadControl _currentThread = null;
  
  public AbstractState(AbstractToolControl ctrl, Set<ControlPanel> panels)
  {
    this._ctrl = ctrl;
    
    if (panels != null)
    {
      this._panels = panels;
      for(ControlPanel panel: this._panels)
      {
        this._initPanels(panel);
      }
    }
  }
  
  protected boolean checkThread()
  {
    return this._currentThread != null && this._currentThread.isAlive();
  }
  
  protected abstract void _initPanels(ControlPanel panel) ;

  @Override
  public void addControlPanel(ControlPanel obs)
  {
    this._panels.add(obs);
    this._initPanels(obs);
  }

  @Override
  public void removeControlPanel(ControlPanel obs)
  {
    this._panels.remove(obs);
  }
  
  @Override
  public void pause() throws InterruptedException {} // Nothing to pause.

  @Override
  public void resume() throws InterruptedException {} // Nothing to resume.

  @Override
  public void cancel() throws InterruptedException {} // Nothing to cancel.

  @Override
  public void close() throws InterruptedException {} //Nothing to close.

  @Override
  public void reinit() throws InterruptedException {} // Nothing to reinit.

  @Override
  public void start(ThreadControl thread) {} // Nothing to start.
  
  @Override
  public void done() {} // Nothing is done.
  
  @Override
  public void crash()
  {
    this._ctrl.setState(new CrashedState(this._ctrl, this._panels));
  }
  
  protected void innerClose()
  {
    _LOG.debug("controller has nothing to do while closing the tool");
    this._ctrl.notifyAction(new Action(ActionType.CLOSING, null));
  }
  
  protected void innerCancel() throws InterruptedException
  {
    if (this.checkThread())
    {
      _LOG.info("waiting for cancellation");
      this._ctrl.notifyAction(new Action(ActionType.WAIT_CANCEL, null));
      this._currentThread.cancel();
    }
    else
    {
      String msg = "thread control is not alive or is null";
      _LOG.debug(msg);
    }
    
    _LOG.info("cancelling all operations");
    this._ctrl.notifyAction(new Action(ActionType.CANCEL, null));
    
    if(this._ctrl._hasAutosampler)
    {
      this._ctrl._passeur.cancel();
    }
    
    if(this._ctrl._hasPump)
    {
      this._ctrl._pousseSeringue.cancel();
    }
    
    this._ctrl.notifyAction(new Action(ActionType.CANCEL_DONE, null));
  }
}

class CrashedState extends AbstractState implements ToolState
{
  public CrashedState(AbstractToolControl ctrl, Set<ControlPanel> panels)
  {
    super(ctrl, panels);
  }

  @Override
  protected void _initPanels(ControlPanel panel)
  {
    panel.enablePause(false);
    panel.enableResume(false);
    panel.enableStart(false);
    panel.enableCancel(false);
    panel.enableReinit(false);
    panel.enableClose(true);
  }
  
  @Override
  public void close() throws InterruptedException
  {
    this.innerClose();
    this._ctrl.setState(new ClosedState(this._ctrl, this._panels));
  }
}

class InitialState extends AbstractState implements ToolState
{
  public InitialState(AbstractToolControl ctrl, Set<ControlPanel> panels)
  {
    super(ctrl, panels);
  }
  
  @Override
  public void close() throws InterruptedException
  {
    for(ControlPanel panel: this._panels)
    {
      panel.enablePause(false);
      panel.enableResume(false);
      panel.enableStart(false);
      panel.enableCancel(false);
      panel.enableReinit(false);
      //panel.enableClose(true);
    }

    this.innerClose();
    this._ctrl.setState(new ClosedState(this._ctrl, this._panels));
  }

  @Override
  public void start(ThreadControl thread)
  {
    this._currentThread = thread;
    this._ctrl.setState(new RunningState(this._ctrl, this._panels));
    thread.start();
  }

  @Override
  protected void _initPanels(ControlPanel panel)
  {
    panel.enablePause(false);
    panel.enableResume(false);
    panel.enableStart(true);
    panel.enableCancel(false);
    panel.enableReinit(false);
    panel.enableClose(true);
  }
}

class ReadyState extends AbstractState implements ToolState
{
  private static final Logger _LOG = LogManager.getLogger(ReadyState.class.getName());
  
  public ReadyState(AbstractToolControl ctrl, Set<ControlPanel> panels)
  {
    super(ctrl, panels);
  }

  @Override
  public void close() throws InterruptedException
  {
    for(ControlPanel panel: this._panels)
    {
      panel.enablePause(false);
      panel.enableResume(false);
      panel.enableStart(false);
      panel.enableCancel(false);
      panel.enableReinit(false);
      panel.enableClose(false);
    }
    
    this.innerReinit();
    this.innerClose();
    this._ctrl.setState(new ClosedState(this._ctrl, this._panels));
  }

  @Override
  public void reinit() throws InterruptedException
  {
    for(ControlPanel panel: this._panels)
    {
      panel.enablePause(false);
      panel.enableResume(false);
      panel.enableStart(false);
      panel.enableCancel(false);
      panel.enableReinit(false);
      panel.enableClose(false);
    }
    
    this.innerReinit();
    this._ctrl.setState(new InitialState(this._ctrl, this._panels));
  }

  private void innerReinit() throws InterruptedException
  {
    _LOG.info("reinitializing all operations");
    this._ctrl.notifyAction(new Action(ActionType.REINIT, null));
    
    if(this._ctrl._hasAutosampler)
    {
      this._ctrl._passeur.reinit();
    }
    
    if(this._ctrl._hasPump)
    {
      this._ctrl._pousseSeringue.reinit();
    }
    
    this._ctrl.notifyAction(new Action(ActionType.REINIT_DONE, null));
  }

  @Override
  public void start(ThreadControl thread)
  {
    this._currentThread = thread;
    this._ctrl.setState(new RunningState(this._ctrl, this._panels));
    thread.start();
  }

  @Override
  protected void _initPanels(ControlPanel panel)
  {
    panel.enablePause(false);
    panel.enableResume(false);
    panel.enableStart(true);
    panel.enableCancel(false);
    panel.enableReinit(true);
    panel.enableClose(true);
  }
}

class RunningState extends AbstractState implements ToolState
{
  private static final Logger _LOG = LogManager.getLogger(RunningState.class.getName());
  
  public RunningState(AbstractToolControl ctrl, Set<ControlPanel> panels)
  {
    super(ctrl, panels);
  }

  @Override
  public void pause() throws InterruptedException
  {
    for(ControlPanel panel: this._panels)
    {
      panel.enablePause(false);
      panel.enableResume(false);
      panel.enableStart(false);
      panel.enableCancel(false);
      panel.enableReinit(false);
      panel.enableClose(false);
    }
    
    this.innerPause();
    this._ctrl.setState(new PausedState(this._ctrl, this._panels));
  }

  private void innerPause() throws InterruptedException
  {
    if(this.checkThread())
    {
      _LOG.info("waiting for pause");
      this._ctrl.notifyAction(new Action(ActionType.WAIT_PAUSE, null));
      
      this._currentThread.pause();
      
      _LOG.info("running pause operations");
      this._ctrl.notifyAction(new Action(ActionType.PAUSE, null));
      
      if(this._ctrl._hasPump)
      {
        this._ctrl._pousseSeringue.pause();    
      }
      
      if(this._ctrl._hasAutosampler)
      {
        this._ctrl._passeur.pause();
      }
    }
    else
    {
      String msg = "thread control is not alive or is null";
      _LOG.debug(msg);
    }
  }

  @Override
  public void cancel() throws InterruptedException
  {
    for(ControlPanel panel: this._panels)
    {
      panel.enablePause(false);
      panel.enableResume(false);
      panel.enableStart(false);
      panel.enableCancel(false);
      panel.enableReinit(false);
      panel.enableClose(false);
    }
    
    this.innerCancel();
    this._ctrl.setState(new InitialState(this._ctrl, this._panels));
  }
  
  @Override
  public void done()
  {
    this._ctrl.setState(new ReadyState(this._ctrl, this._panels));
  }

  @Override
  protected void _initPanels(ControlPanel panel)
  {
    panel.enablePause(true);
    panel.enableResume(false);
    panel.enableStart(false);
    panel.enableCancel(true);
    panel.enableReinit(false);
    panel.enableClose(false);
  }
}

class PausedState extends AbstractState implements ToolState
{
  private static final Logger _LOG = LogManager.getLogger(PausedState.class.getName());
  
  public PausedState(AbstractToolControl ctrl, Set<ControlPanel> panels)
  {
    super(ctrl, panels);
  }

  @Override
  public void resume() throws InterruptedException
  {
    for(ControlPanel panel: this._panels)
    {
      panel.enablePause(false);
      panel.enableResume(false);
      panel.enableStart(false);
      panel.enableCancel(false);
      panel.enableReinit(false);
      panel.enableClose(false);
    }
    
    this.unPause();
    this._ctrl.setState(new RunningState(this._ctrl, this._panels));
  }
  
  private void unPause() throws InterruptedException
  {
    if(this.checkThread())
    {
      _LOG.info("resuming from pause");
      this._ctrl.notifyAction(new Action(ActionType.RESUME, null));
      
      if(this._ctrl._hasAutosampler)
      {
        this._ctrl._passeur.reprise(false); 
      }

      //attention la reprise du passeur avant celle du pousse seringue à
      //cause de la manipulation eventuelle de celui ci
      if(this._ctrl._hasPump)
      {
        this._ctrl._pousseSeringue.reprise(); 
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
  public void cancel() throws InterruptedException
  {
    for(ControlPanel panel: this._panels)
    {
      panel.enablePause(false);
      panel.enableResume(false);
      panel.enableStart(false);
      panel.enableCancel(false);
      panel.enableReinit(false);
      panel.enableClose(false);
    }
    
    this.cancelOnPause();
    this._ctrl.setState(new InitialState(this._ctrl, this._panels));
  }
  
  private void cancelOnPause() throws InterruptedException
  {
    throw new NotImplementedException("cancel on pause is not implemented yet");
  }

  @Override
  protected void _initPanels(ControlPanel panel)
  {
    panel.enablePause(false);
    panel.enableResume(true);
    panel.enableStart(false);
    panel.enableCancel(true);
    panel.enableReinit(false);
    panel.enableClose(false);
  }
}

class ClosedState extends AbstractState implements ToolState
{
  public ClosedState(AbstractToolControl ctrl, Set<ControlPanel> panels)
  {
    super(ctrl, panels);
  }

  @Override
  protected void _initPanels(ControlPanel panel)
  {
    panel.dispose();
  }
}