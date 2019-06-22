package fr.gardoll.ace.controller.core;

import java.util.Set ;

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
  
  protected ToolControlOperations _ctrl = null;
  protected ThreadControl _currentThread = null;
  
  public AbstractState(ToolControlOperations ctrl)
  {
    this._ctrl = ctrl;
    
    for(ControlPanel panel: this._ctrl.getCtrlPanels())
    {
      this.initPanels(panel);
    }
  }
  
  @Override
  public void addControlPanel(ControlPanel ctrlPanel)
  {
    // Just update the state of the new Panel.
    this.initPanels(ctrlPanel);
  }
  
  @Override
  public void removeControlPanel(ControlPanel ctrlPanel)
  {
    // Nothing to do.
  }
  
  @Override
  public Set<ControlPanel> getCtrlPanels()
  {
    return this._ctrl.getCtrlPanels();
  }
  
  protected void disableAllControl()
  {
    for(ControlPanel panel: this._ctrl.getCtrlPanels())
    {
      panel.enablePause(false);
      panel.enableResume(false);
      panel.enableStart(false);
      panel.enableCancel(false);
      panel.enableReinit(false);
      panel.enableClose(false);
    }
  }
  
  protected boolean checkThread()
  {
    return this._currentThread != null && this._currentThread.isRunning();
  }
  
  protected abstract void initPanels(ControlPanel panel) ;

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
    _LOG.debug("set the crashed state");
    this._ctrl.setState(new CrashedState(this._ctrl));
  }
}

class CrashedState extends AbstractState implements ToolState
{
  public CrashedState(ToolControlOperations ctrl)
  {
    super(ctrl);
  }

  @Override
  protected void initPanels(ControlPanel panel)
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
    this._ctrl.closeOperations();
    this._ctrl.setState(new ClosedState(this._ctrl));
  }
}

class InitialState extends AbstractState implements ToolState
{
  public InitialState(ToolControlOperations ctrl)
  {
    super(ctrl);
  }
  
  @Override
  protected void initPanels(ControlPanel panel)
  {
    panel.enablePause(false);
    panel.enableResume(false);
    panel.enableStart(true);
    panel.enableCancel(false);
    panel.enableReinit(false);
    panel.enableClose(true);
  }
  
  @Override
  public void close() throws InterruptedException
  {
    this.disableAllControl();
    this._ctrl.closeOperations();
    this._ctrl.setState(new ClosedState(this._ctrl));
  }

  @Override
  public void start(ThreadControl thread)
  {
    this._currentThread = thread;
    this._ctrl.setState(new RunningState(this._ctrl));
    thread.start();
  }
}

class ReadyState extends AbstractState implements ToolState
{
  public ReadyState(ToolControlOperations ctrl)
  {
    super(ctrl);
  }
  
  @Override
  protected void initPanels(ControlPanel panel)
  {
    panel.enablePause(false);
    panel.enableResume(false);
    panel.enableStart(true);
    panel.enableCancel(false);
    panel.enableReinit(true);
    panel.enableClose(true);
  }

  @Override
  public void close() throws InterruptedException
  {
    this.disableAllControl();
    this._ctrl.reinitOperations();
    this._ctrl.closeOperations();
    this._ctrl.setState(new ClosedState(this._ctrl));
  }

  @Override
  public void reinit() throws InterruptedException
  {
    this.disableAllControl();
    this._ctrl.reinitOperations();
    this._ctrl.setState(new InitialState(this._ctrl));
  }

  @Override
  public void start(ThreadControl thread)
  {
    this._currentThread = thread;
    this._ctrl.setState(new RunningState(this._ctrl));
    thread.start();
  }
}

class RunningState extends AbstractState implements ToolState
{
  private static final Logger _LOG = LogManager.getLogger(RunningState.class.getName());
  
  public RunningState(ToolControlOperations ctrl)
  {
    super(ctrl);
  }
  
  @Override
  protected void initPanels(ControlPanel panel)
  {
    panel.enablePause(true);
    panel.enableResume(false);
    panel.enableStart(false);
    panel.enableCancel(true);
    panel.enableReinit(false);
    panel.enableClose(false);
  }

  @Override
  public void pause() throws InterruptedException
  {
    this.disableAllControl();
    
    if(this.checkThread())
    {
      _LOG.info("waiting for pause");
      this._ctrl.notifyAction(new Action(ActionType.WAIT_PAUSE, null));
      this._currentThread.pause();
    }
    else
    {
      _LOG.debug("thread control is not alive or is null");
    }
  }

  @Override
  public void cancel() throws InterruptedException
  {
    this.disableAllControl();
    
    if (this.checkThread())
    {
      _LOG.info("waiting for cancellation");
      this._ctrl.notifyAction(new Action(ActionType.WAIT_CANCEL, null));
      this._currentThread.cancel();
    }
    else
    {
      _LOG.debug("thread control is not alive or is null");
    }
  }
  
  @Override
  public void done()
  {
    this._ctrl.setState(new ReadyState(this._ctrl));
  }
}

// Never get to this state if the thread is not really paused.
class PausedState extends AbstractState implements ToolState
{
  private static final Logger _LOG = LogManager.getLogger(PausedState.class.getName());
  
  public PausedState(ToolControlOperations ctrl)
  {
    super(ctrl);
  }
  
  @Override
  protected void initPanels(ControlPanel panel)
  {
    panel.enablePause(false);
    panel.enableResume(true);
    panel.enableStart(false);
    panel.enableCancel(true);
    panel.enableReinit(false);
    panel.enableClose(false);
  }

  @Override
  public void resume() throws InterruptedException
  {
    this.disableAllControl();

    if(this.checkThread())
    {
      this._currentThread.unPause();
    }
    else // Should never happen.
    {
      String msg = "thread control is not alive or is null";
      _LOG.debug(msg);
    }
  }
}

class ClosedState extends AbstractState implements ToolState
{
  public ClosedState(ToolControlOperations ctrl)
  {
    super(ctrl);
  }

  @Override
  protected void initPanels(ControlPanel panel)
  {
    panel.dispose();
  }
}