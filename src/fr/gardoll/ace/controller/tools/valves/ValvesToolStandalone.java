package fr.gardoll.ace.controller.tools.valves;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.ui.AbstractToolFrame ;
import fr.gardoll.ace.controller.ui.AbstractJPanelObserver ;

public class ValvesToolStandalone extends AbstractToolFrame
{
  private static final long serialVersionUID = 9097813064524782984L ;
  
  private static final Logger _LOG = LogManager.getLogger(ValvesToolStandalone.class.getName());
  
  private ValvesToolStandalone(AbstractJPanelObserver mainPanel)
  {
    super(mainPanel) ;
  }
  
  public static ValvesToolStandalone instantiate(ParametresSession parametresSession) 
     throws InitializationException
  {
    ValvesToolControl ctrl = new ValvesToolControl(parametresSession);
    ValvesToolPanel toolPanel = new ValvesToolPanel(ctrl);
    ctrl.addControlPanel(toolPanel);
    ValvesToolStandalone tool = new ValvesToolStandalone(toolPanel);
    return tool;
  }
  
  public static void main(String[] args)
  {
    try(ParametresSession parametresSession = ParametresSession.getInstance())
    {
      ValvesToolStandalone tool = ValvesToolStandalone.instantiate(parametresSession);
      tool.setVisible(true);
    }
    catch (Exception e)
    {
      String msg = null;
      
      if(e.getCause() instanceof InitializationException)
      {
        msg = "intialisation has crashed";
      }
      else
      {
        msg = "tool has crashed";
      }
      
      _LOG.fatal(msg, e);
      Utils.reportError(msg, e);
    }
  }

}
