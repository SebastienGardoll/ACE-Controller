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

  public AutosamplerToolStandalone(AutosamplerToolPanel toolPanel)
  {
    super(toolPanel) ;
  }

  // TODO: to be tested.
  public static void main(String[] args)
  {
    try(ParametresSession parametresSession = ParametresSession.getInstance())
    {
      AutosamplerToolControl ctrl = new AutosamplerToolControl(parametresSession);
      AutosamplerToolPanel toolPanel = new AutosamplerToolPanel(ctrl);
      ctrl.addControlPanel(toolPanel);
      AutosamplerToolStandalone tool = new AutosamplerToolStandalone(toolPanel);
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
