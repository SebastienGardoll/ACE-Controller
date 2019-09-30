package fr.gardoll.ace.controller.tools.valves;

import java.util.Optional ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.AbstractCloseableToolControl ;
import fr.gardoll.ace.controller.core.Action ;
import fr.gardoll.ace.controller.core.ActionType ;
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.ParametresSession ;

public class ValvesToolControl extends AbstractCloseableToolControl
{
  private static final Logger _LOG = LogManager.getLogger(ValvesToolControl.class.getName());

  private int     _lastValve = -1;
  private boolean _lastState = true; // true <=> button is unselected ; false <=> button is selected
  
  
  public ValvesToolControl(ParametresSession parametresSession)
      throws InitializationException, InterruptedException
  {
    super(parametresSession, false, false, true) ;
  }
  
  @Override
  protected void closeOperations() throws InterruptedException
  {
    _LOG.debug("closing all the valves");
    try
    {
      this._valves.toutFermer();
      this.notifyAction(new Action(ActionType.CLOSE_ALL_VALVES, Optional.empty()));
    }
    catch(Exception e)
    {
      String msg = "close operations have crashed";
      _LOG.fatal(msg, e);
      this.handleException(msg, e);
    }
  } 
  
  void handleValve(int valveId)
  {
    try
    {
      if(this._lastValve == valveId)
      {
        if(this._lastState)
        {
          this._lastState = false;
          _LOG.debug(String.format("openning valve %s (same valve)", valveId));
          this._valves.ouvrir(valveId);
          this.notifyAction(new Action(ActionType.OPEN_VALVE,
              Optional.of(Integer.valueOf(valveId))));
        }
        else
        {
          this._lastState = true;
          _LOG.debug(String.format("closing valve %s", valveId));
          this._valves.toutFermer();
          this.notifyAction(new Action(ActionType.CLOSE_VALVES, Optional.of(Integer.valueOf(valveId))));
        }
      }
      else
      {
        this._lastState = false;
        _LOG.debug(String.format("openning valve %s (new valve)", valveId));
        this._valves.ouvrir(valveId);
        this._lastValve = valveId;
        this.notifyAction(new Action(ActionType.OPEN_VALVE,
            Optional.of(Integer.valueOf(valveId))));
      }
    }
    catch (Exception e)
    {
      String msg = String.format("valve %s has crashed (last valve: , state: %s)", valveId,
          this._lastValve, this._lastState);
      _LOG.fatal(msg, e);
      this.handleException(msg, e);
    }
  }
}