package fr.gardoll.ace.controller.tools.extraction;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.ui.AbstractToolFrame ;

public class ExtractionToolStandalone extends AbstractToolFrame
{
  private static final long serialVersionUID = 1190173227128454971L ;
  private static final Logger _LOG = LogManager.getLogger(ExtractionToolStandalone.class.getName());

  private ExtractionToolStandalone(ExtractionToolPanel toolPanel)
  {
    super(toolPanel);
  }
  
  public static ExtractionToolStandalone instantiate(ParametresSession parametresSession)
      throws InitializationException
  {
    ExtractionToolControl ctrl = new ExtractionToolControl(parametresSession);
    ExtractionToolPanel toolPanel = new ExtractionToolPanel(ctrl);
    ctrl.addControlPanel(toolPanel);
    ExtractionToolStandalone tool = new ExtractionToolStandalone(toolPanel);
    return tool;
  }
  
  public static void main(String[] args)
  {
    try(ParametresSession parametresSession = ParametresSession.getInstance())
    {
      ExtractionToolStandalone tool = ExtractionToolStandalone.instantiate(parametresSession); 
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
