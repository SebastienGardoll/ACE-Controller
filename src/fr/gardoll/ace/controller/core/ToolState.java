package fr.gardoll.ace.controller.core;

import java.util.Optional ;
import java.util.Set ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

enum StateLiteral
{
  INITIAL("initial"),
  READY("ready"),
  RUNNING("running"),
  PAUSED("paused"),
  CLOSED("closed"),
  CRASHED("crashed");

  private String _literal ;
  
  private StateLiteral(String literal)
  {
    this._literal = literal;
  }
  
  @Override
  public String toString()
  {
    return this._literal;
  }
}

interface ToolState extends ControlPanelHandler
{
  public void askPausing();
  public void pauseTransition();
  
  public void askResuming();
  public void resumeTransition();
  
  public void askCancellation();
  public void cancelTransition();
  
  public void close();
  public void reinit();
  public void start(ThreadControl thread);
  public void done() ;
  public void crash();
  public StateLiteral getLiteral();
}

abstract class AbstractState implements ToolState
{
  private static final Logger _LOG = LogManager.getLogger(AbstractState.class.getName());
  
  protected AbstractToolControl _ctrl = null;
  protected static ThreadControl _CURRENT_THREAD = null;
  
  public AbstractState(AbstractToolControl ctrl)
  {
    this._ctrl = ctrl;
    
    for(ControlPanel panel: this._ctrl.getCtrlPanels())
    {
      this.initPanel(panel);
    }
  }
  
  @Override
  public void addControlPanel(ControlPanel ctrlPanel)
  {
    // Just update the state of the new Panel.
    this.initPanel(ctrlPanel);
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
      panel.enableCarousel(false);
    }
  }
  
  protected boolean checkThread()
  {
    return _CURRENT_THREAD != null && _CURRENT_THREAD.isRunning();
  }
  
  protected abstract void initPanel(ControlPanel panel) ;

  @Override
  public void askPausing()
  {
    // Nothing to pause.
    String className = this.getClass().getName();
    String methodName = "askPausing";
    String msg = String.format("class %s doesn't implement method %s",
                               className, methodName);
    _LOG.debug(msg);
  } 

  @Override
  public void pauseTransition()
  {
    // No pause operations.
    String className = this.getClass().getName();
    String methodName = "pauseTransition";
    String msg = String.format("class %s doesn't implement method %s",
                               className, methodName);
    _LOG.debug(msg);
  }
  
  @Override
  public void askResuming()
  {
    // Nothing to resume.
    String className = this.getClass().getName();
    String methodName = "askResuming";
    String msg = String.format("class %s doesn't implement method %s",
                               className, methodName);
    _LOG.debug(msg);
  }

  @Override 
  public void resumeTransition()
  {
    // No resume operations.
    String className = this.getClass().getName();
    String methodName = "resumeTransition";
    String msg = String.format("class %s doesn't implement method %s",
                               className, methodName);
    _LOG.debug(msg);
  }
  
  @Override
  public void askCancellation()
  {
    // Nothing to cancel.
    String className = this.getClass().getName();
    String methodName = "askCancellation";
    String msg = String.format("class %s doesn't implement method %s",
                               className, methodName);
    _LOG.debug(msg);
  }
  
  @Override
  public void cancelTransition()
  {
    // No cancel operations.
    String className = this.getClass().getName();
    String methodName = "cancelTransition";
    String msg = String.format("class %s doesn't implement method %s",
                               className, methodName);
    _LOG.debug(msg);
  }

  @Override
  public void close()
  {
    // Nothing to close.
    String className = this.getClass().getName();
    String methodName = "close";
    String msg = String.format("class %s doesn't implement method %s",
                               className, methodName);
    _LOG.debug(msg);
  }

  @Override
  public void reinit()
  {
    // Nothing to reinit.
    String className = this.getClass().getName();
    String methodName = "reinit";
    String msg = String.format("class %s doesn't implement method %s",
                               className, methodName);
    _LOG.debug(msg);
  }

  @Override
  public void start(ThreadControl thread)
  {
    // Nothing to start.
    String className = this.getClass().getName();
    String methodName = "start";
    String msg = String.format("class %s doesn't implement method %s",
                               className, methodName);
    _LOG.debug(msg);
  }
  
  @Override
  public void done()
  {
    // Nothing is done.
    String className = this.getClass().getName();
    String methodName = "done";
    String msg = String.format("class %s doesn't implement method %s",
                               className, methodName);
    _LOG.debug(msg);
  } 
  
  @Override
  public void crash()
  {
    _LOG.debug("set crashed state");
    this._ctrl.setState(new CrashedState(this._ctrl));
    if(ParametresSession.isAutomatedTest)
    {
      throw new RuntimeException("stop test");
    }
  }
}

class CrashedState extends AbstractState implements ToolState
{
  private static final Logger _LOG = LogManager.getLogger(CrashedState.class.getName());
  
  public CrashedState(AbstractToolControl ctrl)
  {
    super(ctrl);
  }

  @Override
  protected void initPanel(ControlPanel panel)
  {
    panel.enablePause(false);
    panel.enableResume(false);
    panel.enableStart(false);
    panel.enableCancel(false);
    panel.enableReinit(false);
    panel.enableClose(true);
    panel.enableCarousel(false);
  }
  
  @Override
  public void close()
  {
    _LOG.debug("skip close operations");
    _LOG.debug("set closed state");
    this._ctrl.setState(new ClosedState(this._ctrl));
  }

  @Override
  public StateLiteral getLiteral()
  {
    return StateLiteral.CRASHED;
  }
}

class InitialState extends AbstractState implements ToolState
{
  private static final Logger _LOG = LogManager.getLogger(InitialState.class.getName());
  
  public InitialState(AbstractToolControl ctrl)
  {
    super(ctrl);
  }
  
  @Override
  protected void initPanel(ControlPanel panel)
  {
    panel.enablePause(false);
    panel.enableResume(false);
    panel.enableStart(true);
    panel.enableCancel(false);
    panel.enableReinit(false);
    panel.enableClose(true);
    panel.enableCarousel(false);
  }
  
  @Override
  public void close()
  {
    this.disableAllControl();
    this._ctrl.closeOperations();
    _LOG.debug("set closed state");
    this._ctrl.setState(new ClosedState(this._ctrl));
  }

  @Override
  public void start(ThreadControl thread)
  {
    _CURRENT_THREAD = thread;
    _LOG.debug("set running state");
    this._ctrl.setState(new RunningState(this._ctrl));
    thread.start();
  }

  @Override
  public StateLiteral getLiteral()
  {
    return StateLiteral.INITIAL;
  }
}

class ReadyState extends AbstractState implements ToolState
{
  private static final Logger _LOG = LogManager.getLogger(ReadyState.class.getName());
  
  public ReadyState(AbstractToolControl ctrl)
  {
    super(ctrl);
  }
  
  @Override
  protected void initPanel(ControlPanel panel)
  {
    panel.enablePause(false);
    panel.enableResume(false);
    panel.enableStart(true);
    panel.enableCancel(false);
    panel.enableReinit(true);
    panel.enableClose(true);
    panel.enableCarousel(false);
  }

  @Override
  public void close()
  {
    this.disableAllControl();
    _LOG.debug("reinit before closing");
    this.reinit();
    _LOG.debug("closing after reinit");
    this._ctrl.getState().close();
  }

  @Override
  public void reinit()
  {
    this.disableAllControl();
    this._ctrl.reinitOperations();
    _LOG.debug("set initial state");
    this._ctrl.setState(new InitialState(this._ctrl));
  }

  @Override
  public void start(ThreadControl thread)
  {
    _CURRENT_THREAD = thread;
    _LOG.debug("set running state");
    this._ctrl.setState(new RunningState(this._ctrl));
    thread.start();
  }

  @Override
  public StateLiteral getLiteral()
  {
    return StateLiteral.READY;
  }
}

class RunningState extends AbstractState implements ToolState
{
  private static final Logger _LOG = LogManager.getLogger(RunningState.class.getName());
  
  public RunningState(AbstractToolControl ctrl)
  {
    super(ctrl);
  }
  
  @Override
  protected void initPanel(ControlPanel panel)
  {
    panel.enablePause(true);
    panel.enableResume(false);
    panel.enableStart(false);
    panel.enableCancel(true);
    panel.enableReinit(false);
    panel.enableClose(false);
    panel.enableCarousel(false);
  }

  @Override
  public void askPausing()
  {
    this.disableAllControl();
    
    if(this.checkThread())
    {
      _LOG.info("waiting for pause");
      this._ctrl.notifyAction(new Action(ActionType.WAIT_PAUSE, Optional.empty()));
      _CURRENT_THREAD.pause();
    }
    else
    {
      _LOG.debug("thread control is not alive or is null");
    }
  }
  
  @Override
  public void pauseTransition()
  {
    this._ctrl.pauseOperations();
    _LOG.debug("set paused state");
    this._ctrl.setState(new PausedState(this._ctrl));
  }

  @Override
  public void askCancellation()
  {
    this.disableAllControl();
    
    if (this.checkThread())
    {
      _LOG.info("waiting for cancellation");
      this._ctrl.notifyAction(new Action(ActionType.WAIT_CANCEL, Optional.empty()));
      _CURRENT_THREAD.cancel();
    }
    else
    {
      _LOG.debug("thread control is not alive or is null");
    }
  }
  
  @Override
  public void cancelTransition()
  {
    this._ctrl.cancelOperations();
    _LOG.debug("set initial state");
    this._ctrl.setState(new InitialState(this._ctrl));
  }
  
  @Override
  public void done()
  {
    _LOG.debug("set ready state");
    this._ctrl.setState(new ReadyState(this._ctrl));
  }

  @Override
  public StateLiteral getLiteral()
  {
    return StateLiteral.RUNNING;
  }
}

// Never get to this state if the thread is not really paused.
class PausedState extends AbstractState implements ToolState
{
  private static final Logger _LOG = LogManager.getLogger(PausedState.class.getName());
  
  public PausedState(AbstractToolControl ctrl)
  {
    super(ctrl);
  }
  
  @Override
  protected void initPanel(ControlPanel panel)
  {
    panel.enablePause(false);
    panel.enableResume(true);
    panel.enableStart(false);
    panel.enableCancel(false);
    panel.enableReinit(false);
    panel.enableClose(false);
    panel.enableCarousel(true);
  }

  @Override
  public void askResuming()
  {
    this.disableAllControl();

    if(this.checkThread())
    {
      _CURRENT_THREAD.unPause();
    }
    else // Should never happen.
    {
      String msg = "thread control is not alive or is null";
      _LOG.debug(msg);
    }
  }
  
  @Override
  public void resumeTransition()
  {
    this._ctrl.resumeOperations();
    _LOG.debug("set running state");
    this._ctrl.setState(new RunningState(this._ctrl));
  }

  @Override
  public StateLiteral getLiteral()
  {
    return StateLiteral.PAUSED;
  }
}

class ClosedState extends AbstractState implements ToolState
{
  public ClosedState(AbstractToolControl ctrl)
  {
    super(ctrl);
  }

  @Override
  protected void initPanel(ControlPanel panel)
  {
    panel.dispose();
  }

  @Override
  public StateLiteral getLiteral()
  {
    return StateLiteral.CLOSED;
  }
}