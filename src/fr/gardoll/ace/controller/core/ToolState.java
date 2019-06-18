package fr.gardoll.ace.controller.core;

import java.util.Collections ;
import java.util.Set ;

public interface ToolState extends ControlPanelHandler
{
  public void pause() throws InterruptedException;
  public void resume() throws InterruptedException;
  public void cancel() throws InterruptedException;
  public void close() throws InterruptedException;
  public void reinit() throws InterruptedException;
  public void start() throws InterruptedException;
  public void done() ;
  public void crash();
}

abstract class AbstractState implements ToolState
{
  protected Set<ControlPanel> _panels = Collections.emptySet();
  protected ToolControl       _ctrl   = null; 
  
  public AbstractState(ToolControl ctrl, Set<ControlPanel> panels)
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
  public void close() throws InterruptedException {} // Nothing to close.

  @Override
  public void reinit() throws InterruptedException {} // Nothing to reinit.

  @Override
  public void start() throws InterruptedException {} // Nothing to start.
  
  @Override
  public void done() {} // Nothing is done.
  
  @Override
  public void crash()
  {
    this._ctrl.setState(new CrashedState(this._ctrl, this._panels));
  }
}

class CrashedState extends AbstractState implements ToolState
{
  public CrashedState(ToolControl ctrl, Set<ControlPanel> panels)
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
    this._ctrl.close();
  }
}

class InitialState extends AbstractState implements ToolState
{
  public InitialState(ToolControl ctrl, Set<ControlPanel> panels)
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

    this._ctrl.close();
    this._ctrl.setState(new ClosedState(this._ctrl, this._panels));
  }

  @Override
  public void start()
  {
    this._ctrl.setState(new RunningState(this._ctrl, this._panels));
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
  public ReadyState(ToolControl ctrl, Set<ControlPanel> panels)
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
    
    this._ctrl.cancel();
    this._ctrl.close();
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
      panel.enableClose(true);
    }
    
    this._ctrl.reinit();
    this._ctrl.setState(new ReadyState(this._ctrl, this._panels));
  }

  @Override
  public void start()
  {
    this._ctrl.setState(new RunningState(this._ctrl, this._panels));
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
  public RunningState(ToolControl ctrl, Set<ControlPanel> panels)
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
    
    this._ctrl.pause();
    this._ctrl.setState(new PausedState(this._ctrl, this._panels));
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
    
    this._ctrl.cancel();
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
  public PausedState(ToolControl ctrl, Set<ControlPanel> panels)
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
    
    this._ctrl.unPause();
    this._ctrl.setState(new RunningState(this._ctrl, this._panels));
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
    
    this._ctrl.cancelOnPause();
    this._ctrl.setState(new InitialState(this._ctrl, this._panels));
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
  public ClosedState(ToolControl ctrl, Set<ControlPanel> panels)
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
    panel.enableClose(false);
  }
}