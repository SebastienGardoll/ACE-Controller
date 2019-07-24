package fr.gardoll.ace.controller.tools.valves;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.ui.AbstractJFrame ;
import fr.gardoll.ace.controller.ui.AbstractJPanelObserver ;

public class ValvesToolStandalone extends AbstractJFrame
{
  private static final long serialVersionUID = 9097813064524782984L ;
  
  private static final Logger _LOG = LogManager.getLogger(ValvesToolStandalone.class.getName());
  
  public ValvesToolStandalone(AbstractJPanelObserver mainPanel)
  {
    super(mainPanel) ;
  }
  
  // TODO: to be tested.
  public static void main(String[] args)
  {
    try(ParametresSession parametresSession = ParametresSession.getInstance())
    {
      ValvesToolControl ctrl = new ValvesToolControl(parametresSession);
      ValvesToolPanel toolPanel = new ValvesToolPanel(ctrl);
      ctrl.addControlPanel(toolPanel);
      ValvesToolStandalone tool = new ValvesToolStandalone(toolPanel);
      tool.setVisible(true);
    }
    catch (Exception e)
    {
      String msg = null;
      
      if(e.getCause() instanceof InitializationException)
      {
        msg = "error while initialisating the tool";
      }
      else
      {
        msg = "error while operating the tool";
      }
      
      _LOG.fatal(msg, e);
      Utils.reportError(msg, e);
    }
  }

}
