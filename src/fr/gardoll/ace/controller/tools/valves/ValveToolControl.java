package fr.gardoll.ace.controller.tools.valves;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.AbstractBasicToolControl ;
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.ParametresSession ;

public class ValveToolControl extends AbstractBasicToolControl
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
          this._valves.ouvrir(valveId);
        }
        else
        {
          this._lastState = true;
          this._valves.toutFermer();
        }
      }
      else
      {
        this._lastState = false;
        this._valves.ouvrir(valveId);
        this._lastValve = valveId;
      }
    }
    catch (Exception e)
    {
      String msg = String.format("valve %s crashed", valveId);
      _LOG.fatal(msg, e);
      this.handleException(msg, e);
    }
  }
}