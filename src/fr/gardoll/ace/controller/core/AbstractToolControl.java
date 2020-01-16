package fr.gardoll.ace.controller.core;

import java.util.Collections ;
import java.util.HashSet ;
import java.util.Set ;

import fr.gardoll.ace.controller.autosampler.Passeur ;
import fr.gardoll.ace.controller.pump.PousseSeringue ;
import fr.gardoll.ace.controller.settings.ConfigurationException ;
import fr.gardoll.ace.controller.settings.GeneralSettings ;
import fr.gardoll.ace.controller.settings.ParametresSession ;
import fr.gardoll.ace.controller.valves.Valves ;

public abstract class AbstractToolControl implements ToolControl, Observable
{
  final private Set<ControlPanel> _ctrlPanels = new HashSet<>();
  
  protected final PousseSeringue _pousseSeringue ;
  protected final Passeur _passeur ;
  protected final Valves _valves;
  
  protected final boolean _hasAutosampler;
  protected final boolean _hasPump;
  protected final boolean _hasValves ;
  
  private ToolState _state = new InitialState(this);
 
  abstract protected void closeOperations();

  abstract void cancelOperations();
  
  abstract void reinitOperations();
  
  abstract void pauseOperations();
  
  abstract void resumeOperations();
  
  protected abstract String getToolName();
  
  public AbstractToolControl(ParametresSession parametresSession,
                             boolean hasPump, boolean hasAutosampler,
                             boolean hasValves)
                         throws InitializationException, ConfigurationException
  {
    if(false == ParametresSession.isAutomatedTest)
    {
      String msg = String.format("starting %s tool with general settings:\n\n%s\n",
          this.getToolName(), GeneralSettings.instance().toString());
      Log.HIGH_LEVEL.info(msg);
    }
    
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
  
  protected void start(ThreadControl thread)
  {
    this.getState().start(thread);
  }
  
  @Override
  public void close()
  {
    throw new UnsupportedOperationException("close  is not implemented");
  }
  
  @Override
  public void cancel()
  {
    throw new UnsupportedOperationException("cancel is not implemented");
  }
  
  @Override
  // Thread must be terminated.
  public void reinit()
  {
    throw new UnsupportedOperationException("reinit is not implemented");
  }
  
  @Override
  public void pause()
  {  
    throw new UnsupportedOperationException("pause is not implemented");
  }
  
  @Override
  public void resume()
  {  
    throw new UnsupportedOperationException("resume is not implemented");
  }
  
  @Override
  public Set<ControlPanel> getCtrlPanels()
  {
    return Collections.unmodifiableSet(this._ctrlPanels);
  }
  
  void setState(ToolState state)
  {
    this._state = state;
  }
  
  ToolState getState()
  {
    return this._state;
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
