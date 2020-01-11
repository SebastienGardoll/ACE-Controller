package fr.gardoll.ace.controller.tools.extraction;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.settings.ConfigurationException ;
import fr.gardoll.ace.controller.settings.ParametresSession ;
import fr.gardoll.ace.controller.ui.AbstractToolFrame ;

public class ExtractionToolFrame extends AbstractToolFrame
{
  private static final long serialVersionUID = 1190173227128454971L ;
  private static final Logger _LOG = LogManager.getLogger(ExtractionToolFrame.class.getName());

  private ExtractionToolFrame(ExtractionToolPanel toolPanel)
  {
    super(toolPanel);
  }
  
  public static ExtractionToolFrame instantiate(ParametresSession parametresSession)
      throws InitializationException, ConfigurationException
  {
    ExtractionToolControl ctrl = new ExtractionToolControl(parametresSession);
    ExtractionToolPanel toolPanel = new ExtractionToolPanel(ctrl);
    ctrl.addControlPanel(toolPanel);
    ExtractionToolFrame tool = new ExtractionToolFrame(toolPanel);
    return tool;
  }
  
  public static void main(String[] args)
  {
    _LOG.info("starting the standalone extraction tool");
    
    try(ParametresSession parametresSession = ParametresSession.getInstance())
    {
      ExtractionToolFrame tool = ExtractionToolFrame.instantiate(parametresSession); 
      tool.setVisible(true);
    }
    catch (Exception e)
    {
      String msg = null;
      
      if(e.getCause() instanceof InitializationException)
      {
        msg = "initialisation of the standalone extraction tool has crashed";
      }
      else
      {
        msg = "standalone extraction tool has crashed";
      }
      
      _LOG.fatal(msg, e);
      Utils.reportError(msg, e);
    }
    finally
    {
      _LOG.info("shutdown the standalone extraction tool");
    }
  }
}
