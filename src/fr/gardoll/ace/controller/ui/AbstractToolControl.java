package fr.gardoll.ace.controller.ui;

import java.util.HashSet ;
import java.util.Set ;

public abstract class AbstractToolControl implements ToolControl
{
  private final Set<Observer> _observers     = new HashSet<>();
  private final Set<ControlPanel> _ctrlPanel = new HashSet<>();
  
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
