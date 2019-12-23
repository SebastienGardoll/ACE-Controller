package fr.gardoll.ace.controller.tools.valves;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.ui.AbstractToolFrame ;
import fr.gardoll.ace.controller.ui.AbstractJPanelObserver ;

public class ValvesToolFrame extends AbstractToolFrame
{
  private static final long serialVersionUID = 9097813064524782984L ;
  
  private static final Logger _LOG = LogManager.getLogger(ValvesToolFrame.class.getName());
  
  private ValvesToolFrame(AbstractJPanelObserver mainPanel)
  {
    super(mainPanel) ;
  }
  
  public static ValvesToolFrame instantiate(ParametresSession parametresSession) 
     throws InitializationException
  {
    ValvesToolControl ctrl = new ValvesToolControl(parametresSession);
    ValvesToolPanel toolPanel = new ValvesToolPanel(ctrl);
    ctrl.addControlPanel(toolPanel);
    ValvesToolFrame tool = new ValvesToolFrame(toolPanel);
    return tool;
  }
  
  public static void main(String[] args)
  {
    try(ParametresSession parametresSession = ParametresSession.getInstance())
    {
      ValvesToolFrame tool = ValvesToolFrame.instantiate(parametresSession);
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
