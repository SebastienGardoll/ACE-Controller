package fr.gardoll.ace.controller.tools.autosampler;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.ui.AbstractToolFrame ;

public class AutosamplerToolFrame extends AbstractToolFrame
{
  private static final Logger _LOG = LogManager.getLogger(AutosamplerToolFrame.class.getName());
  private static final long serialVersionUID = -6062838678688858409L ;

  private AutosamplerToolFrame(AutosamplerToolPanel toolPanel)
  {
    super(toolPanel) ;
  }
  
  public static AutosamplerToolFrame instantiate(ParametresSession parametresSession)
      throws InitializationException
  {
    AutosamplerToolControl ctrl = new AutosamplerToolControl(parametresSession);
    AutosamplerToolPanel toolPanel = new AutosamplerToolPanel(ctrl);
    ctrl.addControlPanel(toolPanel);
    AutosamplerToolFrame tool = new AutosamplerToolFrame(toolPanel);
    return tool;
  }

  // TODO: to be tested.
  public static void main(String[] args)
  {
    _LOG.info("start the tool");
    
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
        msg = "initialisation has crashed";
      }
      else
      {
        msg = "tool has crashed";
      }
      
      _LOG.fatal(msg, e);
      Utils.reportError(msg, e);
    }
    finally
    {
      _LOG.info("shutdown the tool");
    }
  }
}
