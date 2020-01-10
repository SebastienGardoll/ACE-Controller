package fr.gardoll.ace.controller.tools.pump;

import java.util.Optional ;
import java.util.SortedSet ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.autosampler.Passeur ;
import fr.gardoll.ace.controller.core.AbstractPausableToolControl ;
import fr.gardoll.ace.controller.core.AbstractThreadControl ;
import fr.gardoll.ace.controller.core.AbstractToolControl ;
import fr.gardoll.ace.controller.core.Action ;
import fr.gardoll.ace.controller.core.ActionType ;
import fr.gardoll.ace.controller.core.CancellationException ;
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.core.ThreadControl ;
import fr.gardoll.ace.controller.pump.PousseSeringue ;
import fr.gardoll.ace.controller.settings.GeneralSettings ;
import fr.gardoll.ace.controller.valves.Valves ;

public class PumpToolControl extends AbstractPausableToolControl
{
  private static final Logger _LOG = LogManager.getLogger(PumpToolControl.class.getName());
  
  public PumpToolControl(ParametresSession parametresSession)
      throws InitializationException
  {
    super(parametresSession, true, true, true) ;
  }
  
  void start(SortedSet<Integer> lines, int volume)
  {
    double volMax = GeneralSettings.instance().getVolumeMaxSeringue();
    
    if(volume > volMax ||
       volume <= 0)
    {
      String msg = String.format("volume must be greater than zero but less than %s, got '%s' mL", volMax, volume);
      _LOG.error(msg);
      this.notifyError(msg);
      return;
    }
    
    if(lines.isEmpty())
    {
      String msg = "select at least one line";
      _LOG.error(msg);
      this.notifyError(msg);
      return;
    }
    
    for(Integer line: lines)
    {
      if(line > Valves.NB_EV_MAX)
      {
        String msg = String.format("line number cannot be greater that %s, got '%s'",
            Valves.NB_EV_MAX, line);
        _LOG.error(msg);
        this.notifyError(msg);
        return;
      }
    }
    
    try
    {
      PumpThread thread = new PumpThread(this, lines, volume);
      _LOG.debug("starting pump thread");
      this.start(thread);
    }
    catch(Exception e)
    {
      String msg = "pump thread start has crashed";
      _LOG.fatal(msg, e);
      this.handleException(msg, e);
    }
  }

  @Override
  protected void closeOperations()
  {
    _LOG.debug("controller has nothing to do while closing the tool");
    this.notifyAction(new Action(ActionType.CLOSING, Optional.empty()));
  }
}

class PumpThread extends AbstractThreadControl
{
  private static final Logger _LOG = LogManager.getLogger(PumpThread.class.getName());
  
  private int _volume ;
  private SortedSet<Integer> _lines ;

  public PumpThread(AbstractToolControl toolCtrl,
                    SortedSet<Integer> lines,
                    int volume)
  {
    super(toolCtrl) ;
    this._lines = lines;
    this._volume = volume;
  }

  @Override
  protected void threadLogic() throws CancellationException,
                                      InitializationException, Exception
  {
    ParametresSession session = ParametresSession.getInstance() ;
    Passeur autosampler = session.getPasseur();
    PousseSeringue pump = session.getPousseSeringue();
    
    GeneralSettings settings = GeneralSettings.instance();
    
    ThreadControl.check();
    _LOG.debug("setup the arm");
    Action action = new Action(ActionType.ARM_MOVING, Optional.empty()) ;
    this._toolCtrl.notifyAction(action) ;
    
    autosampler.moveArmToTrash();
    autosampler.finMoveBras();
    
    action = new Action(ActionType.ARM_END_MOVING, Optional.empty()) ;
    this._toolCtrl.notifyAction(action) ;
    
    ThreadControl.check();
    
    action = new Action(ActionType.USR_MSG, Optional.of("set maximum pump rate"));
    this._toolCtrl.notifyAction(action) ;
    pump.setDebitAspiration(settings.getDebitMaxPousseSeringue());
    pump.setDebitRefoulement(settings.getDebitMaxPousseSeringue());

    for (Integer line: this._lines)
    {  
      ThreadControl.check();
      
      String msg = String.format("begin to clean line %s", line);
      _LOG.debug(msg);
      action = new Action(ActionType.USR_MSG, Optional.of(msg));
      this._toolCtrl.notifyAction(action);
      
      action = new Action(ActionType.WITHDRAWING, Optional.of(this._volume));
      this._toolCtrl.notifyAction(action);
      pump.rincageAspiration(this._volume, line.intValue());
      pump.finPompage();
      
      action = new Action(ActionType.INFUSING, Optional.of(this._volume));
      this._toolCtrl.notifyAction(action);
      pump.vidange();
      pump.finPompage();
      
      msg = String.format("line %s is clean", line);
      _LOG.debug(msg);
      action = new Action(ActionType.USR_MSG, Optional.of(msg));
      this._toolCtrl.notifyAction(action);
    }

    String msg = "end";
    _LOG.debug(msg);
    action = new Action(ActionType.USR_MSG, Optional.of(msg));
    this._toolCtrl.notifyAction(action) ;
  }
}
