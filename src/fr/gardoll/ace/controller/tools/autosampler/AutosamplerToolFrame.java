package fr.gardoll.ace.controller.tools.autosampler;

import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.Log ;
import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.settings.ConfigurationException ;
import fr.gardoll.ace.controller.settings.ParametresSession ;
import fr.gardoll.ace.controller.ui.AbstractToolFrame ;

public class AutosamplerToolFrame extends AbstractToolFrame
{
  private static final Logger _LOG = Log.HIGH_LEVEL;
  private static final long serialVersionUID = -6062838678688858409L ;

  private AutosamplerToolFrame(AutosamplerToolPanel toolPanel)
  {
    super(toolPanel) ;
  }
  
  public static AutosamplerToolFrame instantiate(ParametresSession parametresSession)
      throws InitializationException, ConfigurationException
  {
    AutosamplerToolControl ctrl = new AutosamplerToolControl(parametresSession);
    AutosamplerToolPanel toolPanel = new AutosamplerToolPanel(ctrl);
    ctrl.addControlPanel(toolPanel);
    AutosamplerToolFrame tool = new AutosamplerToolFrame(toolPanel);
    return tool;
  }

  public static void main(String[] args)
  {
    _LOG.info("starting the standalone autosampler tool");
    
    try(ParametresSession parametresSession = ParametresSession.getInstance())
    {
      AutosamplerToolFrame tool = AutosamplerToolFrame.instantiate(parametresSession); 
      tool.setVisible(true);
    }
    catch (Exception e)
    {
      String msg = null;
      
      if(e.getCause() instanceof InitializationException)
      {
        msg = "initialisation of the standalone autosampler tool has crashed";
      }
      else
      {
        msg = "standalone autosampler tool has crashed";
      }
      
      _LOG.fatal(msg, e);
      Utils.reportError(msg, e);
    }
    finally
    {
      _LOG.info("shutdown the standalone autosampler tool");
    }
  }
}
