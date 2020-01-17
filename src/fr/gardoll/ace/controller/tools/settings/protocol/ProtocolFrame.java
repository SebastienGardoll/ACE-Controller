package fr.gardoll.ace.controller.tools.settings.protocol;

import javax.swing.JPanel ;

import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.Log ;
import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.ui.AbstractToolFrame ;

public class ProtocolFrame extends AbstractToolFrame
{
  private static final long serialVersionUID = -5251438372246500609L ;

  private static final Logger _LOG = Log.HIGH_LEVEL;
  
  public ProtocolFrame(JPanel mainPanel)
  {
    super(mainPanel) ;
  }
  
  public static void main(String[] args)
  {
    try
    {
      ProtocolFrame frame = ProtocolFrame.instantiate();
      frame.setVisible(true);
    }
    catch (Exception e)
    {
      String msg = null;
      
      if(e.getCause() instanceof InitializationException)
      {
        msg = "intialisation of the settings editor has crashed";
      }
      else
      {
        msg = "settings editor has crashed";
      }
      
      _LOG.fatal(msg, e);
      Utils.reportError(msg, e);
    }
    finally
    {
      _LOG.info("shutdown the settings");
    }
  }

  public static ProtocolFrame instantiate()
  {
    return new ProtocolFrame(new ProtocolMainPanel());
  }
}