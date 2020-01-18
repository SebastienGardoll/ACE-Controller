package fr.gardoll.ace.controller.tools.tests;

import java.util.Optional ;

import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.AbstractCancelableToolControl ;
import fr.gardoll.ace.controller.core.Action ;
import fr.gardoll.ace.controller.core.ActionType ;
import fr.gardoll.ace.controller.core.ControlPanel ;
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.Log ;
import fr.gardoll.ace.controller.settings.ConfigurationException ;
import fr.gardoll.ace.controller.settings.ParametresSession ;

public abstract class AbstractTestControl extends AbstractCancelableToolControl
{
  private static final Logger _LOG = Log.HIGH_LEVEL;
  
  public AbstractTestControl(ParametresSession parametresSession,
      boolean hasPump, boolean hasAutosampler, boolean hasValves)
      throws InitializationException, ConfigurationException
  {
    super(parametresSession, hasPump, hasAutosampler, hasValves) ;
  }
  
  abstract void start();
  
  protected void updateCurrentOperation(int index)
  {
    for(ControlPanel panel: this.getCtrlPanels())
    {
      Action action = new Action(ActionType.TEST, Optional.of(index));
      panel.majActionActuelle(action);
    }
  }
  
  @Override
  protected void closeOperations()
  {
    _LOG.debug("controller has nothing to do while closing the tool");
  }
}
