package fr.gardoll.ace.controller.tools.valves;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.settings.ConfigurationException ;
import fr.gardoll.ace.controller.settings.ParametresSession ;
import fr.gardoll.ace.controller.ui.AbstractJPanelObserver ;
import fr.gardoll.ace.controller.ui.AbstractToolFrame ;

public class ValvesToolFrame extends AbstractToolFrame
{
  private static final long serialVersionUID = 9097813064524782984L ;
  
  private static final Logger _LOG = LogManager.getLogger(ValvesToolFrame.class.getName());
  
  private ValvesToolFrame(AbstractJPanelObserver mainPanel)
  {
    super(mainPanel) ;
  }
  
  public static ValvesToolFrame instantiate(ParametresSession parametresSession) 
     throws InitializationException, ConfigurationException
  {
    ValvesToolControl ctrl = new ValvesToolControl(parametresSession);
    ValvesToolPanel toolPanel = new ValvesToolPanel(ctrl);
    ctrl.addControlPanel(toolPanel);
    ValvesToolFrame tool = new ValvesToolFrame(toolPanel);
    return tool;
  }
  
  public static void main(String[] args)
  {
    _LOG.info("starting the standalone valves tool");
    
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
        msg = "intialization of the standalone valves tool has crashed";
      }
      else
      {
        msg = "standalone valves tool has crashed";
      }
      
      _LOG.fatal(msg, e);
      Utils.reportError(msg, e);
    }
    finally
    {
      _LOG.info("shutdown the standalone valves tool");
    }
  }

}
