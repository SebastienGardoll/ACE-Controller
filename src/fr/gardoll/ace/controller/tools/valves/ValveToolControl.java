package fr.gardoll.ace.controller.tools.valves;

import java.util.Optional ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.AbstractToolControl ;
import fr.gardoll.ace.controller.core.Action ;
import fr.gardoll.ace.controller.core.ActionType ;
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.ParametresSession ;

public class ValveToolControl extends AbstractToolControl
{
  private static final Logger _LOG = LogManager.getLogger(ValveToolControl.class.getName());

  private int     _lastValve = -1;
  private boolean _lastState = true; // true <=> button is up ; false <=> button is down
  
  
  public ValveToolControl(ParametresSession parametresSession)
      throws InitializationException, InterruptedException
  {
    super(parametresSession, false, false, true) ;
  }
  
  public void handleValve(int valveId)
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
          _LOG.debug("closing all valves");
          this._valves.toutFermer();
          this.notifyAction(new Action(ActionType.CLOSE_ALL_VALVES, null));
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
      String msg = String.format("valve %s crashed (last valve: , state: %s)", valveId,
          this._lastValve, this._lastState);
      _LOG.fatal(msg, e);
      this.handleException(msg, e);
    }
  }

  @Override
  public void close()
  {
    _LOG.debug("controller has nothing to do while closing the tool");
  }
}