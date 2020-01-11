package fr.gardoll.ace.controller.tools.pump;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.settings.ConfigurationException ;
import fr.gardoll.ace.controller.settings.ParametresSession ;
import fr.gardoll.ace.controller.ui.AbstractToolFrame ;

public class PumpToolFrame extends AbstractToolFrame
{
  private static final long serialVersionUID = 1190173227128454971L ;
  private static final Logger _LOG = LogManager.getLogger(PumpToolFrame.class.getName());

  private PumpToolFrame(PumpToolPanel toolPanel)
  {
    super(toolPanel);
  }
  
  public static PumpToolFrame instantiate (ParametresSession parametresSession)
      throws InitializationException, ConfigurationException
  {
    PumpToolControl ctrl = new PumpToolControl(parametresSession);
    PumpToolPanel toolPanel = new PumpToolPanel(ctrl);
    ctrl.addControlPanel(toolPanel);
    PumpToolFrame tool = new PumpToolFrame(toolPanel);
    return tool;
  }
  
  public static void main(String[] args)
  {
    _LOG.info("starting the standalone pump tool");
    
    try(ParametresSession parametresSession = ParametresSession.getInstance())
    {
      PumpToolFrame tool = PumpToolFrame.instantiate(parametresSession);
      tool.setVisible(true);
    }
    catch (Exception e)
    {
      String msg = null;
      
      if(e.getCause() instanceof InitializationException)
      {
        msg = "initialisation of the standalone pump tool has crashed";
      }
      else
      {
        msg = "standalone pump tool has crashed";
      }
      
      _LOG.fatal(msg, e);
      Utils.reportError(msg, e);
    }
    finally
    {
      _LOG.info("shutdown the standalone pump tool");
    }
  }
}
