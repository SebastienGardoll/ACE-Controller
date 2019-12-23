package fr.gardoll.ace.controller.tools.autosampler;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.ui.AbstractJFrame ;

public class AutosamplerToolStandalone extends AbstractJFrame
{
  private static final Logger _LOG = LogManager.getLogger(AutosamplerToolStandalone.class.getName());
  private static final long serialVersionUID = -6062838678688858409L ;

  private AutosamplerToolStandalone(AutosamplerToolPanel toolPanel)
  {
    super(toolPanel) ;
  }
  
  public static AutosamplerToolStandalone instantiate(ParametresSession parametresSession)
      throws InitializationException
  {
    AutosamplerToolControl ctrl = new AutosamplerToolControl(parametresSession);
    AutosamplerToolPanel toolPanel = new AutosamplerToolPanel(ctrl);
    ctrl.addControlPanel(toolPanel);
    AutosamplerToolStandalone tool = new AutosamplerToolStandalone(toolPanel);
    return tool;
  }

  // TODO: to be tested.
  public static void main(String[] args)
  {
    try(ParametresSession parametresSession = ParametresSession.getInstance())
    {
      AutosamplerToolStandalone tool = AutosamplerToolStandalone.instantiate(parametresSession); 
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
  }
}
