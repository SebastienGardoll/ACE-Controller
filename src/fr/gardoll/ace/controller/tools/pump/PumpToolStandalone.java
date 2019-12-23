package fr.gardoll.ace.controller.tools.pump;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.ui.AbstractJFrame ;

public class PumpToolStandalone extends AbstractJFrame
{
  private static final long serialVersionUID = 1190173227128454971L ;
  private static final Logger _LOG = LogManager.getLogger(PumpToolStandalone.class.getName());

  private PumpToolStandalone(PumpToolPanel toolPanel)
  {
    super(toolPanel);
  }
  
  public static PumpToolStandalone instantiate (ParametresSession parametresSession)
      throws InitializationException
  {
    PumpToolControl ctrl = new PumpToolControl(parametresSession);
    PumpToolPanel toolPanel = new PumpToolPanel(ctrl);
    ctrl.addControlPanel(toolPanel);
    PumpToolStandalone tool = new PumpToolStandalone(toolPanel);
    return tool;
  }
  
  public static void main(String[] args)
  {
    try(ParametresSession parametresSession = ParametresSession.getInstance())
    {
      PumpToolStandalone tool = PumpToolStandalone.instantiate(parametresSession);
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
