package fr.gardoll.ace.controller.tools.valves;

import java.util.Optional ;

import org.apache.commons.lang3.tuple.ImmutablePair ;
import org.apache.commons.lang3.tuple.Pair ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.AbstractCloseableToolControl ;
import fr.gardoll.ace.controller.core.Action ;
import fr.gardoll.ace.controller.core.ActionType ;
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.Log ;
import fr.gardoll.ace.controller.settings.ConfigurationException ;
import fr.gardoll.ace.controller.settings.ParametresSession ;

public class ValvesToolControl extends AbstractCloseableToolControl
{
  private static final Logger _LOG = Log.HIGH_LEVEL;

  private final static int _DEFAULT_LAST_VALVE = -1;
  
  // this._previousValveId: the id of the previous valve operated.
  private int _previousValveId = _DEFAULT_LAST_VALVE;
  
  // this._previousValveState: true  <=> the previous valve is opened
  //                           false <=> the previous valve is closed
  private boolean _previousValveState = false;
  
  
  public ValvesToolControl(ParametresSession parametresSession)
      throws InitializationException, ConfigurationException
  {
    super(parametresSession, false, false, true) ;
  }
  
  @Override
  protected void closeOperations()
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
    // this._previousValveId: the id of the previous valve operated. 
    // this._previousValveState: true  <=> the previous valve is opened
    //                           false <=> the previous valve is closed
    try
    {
      if(this._previousValveId == valveId ||
         this._previousValveId == _DEFAULT_LAST_VALVE) // The first time.
      {
        // The case of:
        // - the first valve operated.
        // - the current valve operated is the same as the previous valve.
        
        // Case of this._lastValve == _DEFAULT_LAST_VALVE
        this._previousValveId = valveId;
        
        if(this._previousValveState) 
        {
          // Meaning the valve was previously opened => close all valves.
          
          this._previousValveState = false;
          _LOG.info(String.format("closing valve %s", valveId));
          this._valves.toutFermer();
          
          Pair<Integer,Optional<Integer>> payload = ImmutablePair.of(valveId, Optional.empty());
          this.notifyAction(new Action(ActionType.CLOSE_VALVES, Optional.of(payload)));
        }
        else
        {
          // Meaning the valve was previously closed => open the given valve.
          
          this._previousValveState = true;
          _LOG.info(String.format("openning valve %s", valveId));
          this._valves.ouvrir(valveId);
          
          Pair<Integer,Optional<Integer>> payload = ImmutablePair.of(valveId, Optional.empty());
          this.notifyAction(new Action(ActionType.OPEN_VALVE, Optional.of(payload)));
        }
      }
      else
      {
        // The current valve operated (only open) is not the same as the previous valve.
        
        Pair<Integer,Optional<Integer>> payload = null;
        String msg = null;
        
        if(this._previousValveState)
        {
          // The previous valve is opened.
          
          msg = String.format("closing valve %s, openning valve %s", this._previousValveId, valveId);
          payload = ImmutablePair.of(valveId, Optional.of(this._previousValveId));
        }
        else
        {
          // The previous valve has already been closed.
          
          msg = String.format("openning valve %s", valveId);
          payload = ImmutablePair.of(valveId, Optional.empty());
        }
        
        _LOG.info(msg);

        this._valves.ouvrir(valveId);
        this._previousValveId = valveId;
        this._previousValveState = true;
        
        this.notifyAction(new Action(ActionType.OPEN_VALVE, Optional.of(payload)));
      }
    }
    catch (Exception e)
    {
      String msg = String.format("valve %s has crashed (last valve: , state: %s)", valveId,
          this._previousValveId, this._previousValveState);
      _LOG.fatal(msg, e);
      this.handleException(msg, e);
    }
  }
}