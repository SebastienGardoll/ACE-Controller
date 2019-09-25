package fr.gardoll.ace.controller.tools.pump;

import java.util.List ;
import java.util.Optional ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.autosampler.Passeur ;
import fr.gardoll.ace.controller.core.AbstractStateFullToolControl ;
import fr.gardoll.ace.controller.core.AbstractStateToolControl ;
import fr.gardoll.ace.controller.core.AbstractThreadControl ;
import fr.gardoll.ace.controller.core.Action ;
import fr.gardoll.ace.controller.core.ActionType ;
import fr.gardoll.ace.controller.core.CancellationException ;
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.pump.PousseSeringue ;

public class PumpToolControl extends AbstractStateFullToolControl
{

  public PumpToolControl(ParametresSession parametresSession)
      throws InitializationException, InterruptedException
  {
    super(parametresSession, true, true, true) ;
  }

  @Override
  protected void closeOperations() throws InterruptedException
  {
    // TODO Auto-generated method stub
    // empty the syringe ?
  }
}

class pumpThread extends AbstractThreadControl
{
  private static final Logger _LOG = LogManager.getLogger(pumpThread.class.getName());
  
  private int _volume ;
  private List<Integer> _lines ;

  public pumpThread(AbstractStateToolControl toolCtrl,
                    List<Integer> lines,
                    int volume)
  {
    super(toolCtrl) ;
    this._lines = lines;
    this._volume = volume;
  }

  @Override
  protected void threadLogic() throws InterruptedException,
      CancellationException, InitializationException, Exception
  {
    _LOG.debug("starting pump thread");
    ParametresSession parametresSession = ParametresSession.getInstance() ;
    Passeur autosampler = parametresSession.getPasseur();
    PousseSeringue pump = parametresSession.getPousseSeringue();
    
    _LOG.debug("move arm upper limit");
    Action action = new Action(ActionType.ARM_MOVING, Optional.empty()) ;
    this._toolCtrl.notifyAction(action) ;
    autosampler.moveButeBras();//sans setOrigineBras() inclus !
    
    // XXX
    this.checkCancel();
    this.checkPause();
    
    autosampler.finMoveBras();
    autosampler.setOrigineBras(); //mettre le bras en fin de butée car attention débordement poubelle !!!
    
    // XXX
    this.checkCancel();
    this.checkPause();
    
    action = new Action(ActionType.USR_MSG, Optional.of("set maximum pump rate"));
    this._toolCtrl.notifyAction(action) ;
    pump.setDebitAspiration(parametresSession.debitMaxPousseSeringue());
    pump.setDebitRefoulement(parametresSession.debitMaxPousseSeringue());

    for (Integer line: this._lines)
    {  
      String msg = String.format("begin to clean line %s", line);
      _LOG.debug(msg);
      action = new Action(ActionType.USR_MSG, Optional.of(msg));
      this._toolCtrl.notifyAction(action);
      
      // XXX
      this.checkCancel();
      this.checkPause();
      
      action = new Action(ActionType.INFUSING, Optional.of(this._volume));
      this._toolCtrl.notifyAction(action);
      pump.rincageAspiration(this._volume, line.intValue());
      pump.finPompage();
      
      // XXX
      this.checkCancel();
      this.checkPause();
      
      action = new Action(ActionType.WITHDRAWING, Optional.of(this._volume));
      this._toolCtrl.notifyAction(action);
      pump.vidange();
      pump.finPompage();
    }

    String msg = "end";
    _LOG.debug(msg);
    action = new Action(ActionType.USR_MSG, Optional.of(msg));
    this._toolCtrl.notifyAction(action) ;
  }
}
