package fr.gardoll.ace.controller.tools.tests;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import javax.swing.JPanel ;

import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.AbstractThreadControl ;
import fr.gardoll.ace.controller.core.AbstractToolControl ;
import fr.gardoll.ace.controller.core.CancellationException ;
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.Log ;
import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.settings.ConfigurationException ;
import fr.gardoll.ace.controller.settings.ParametresSession ;
import fr.gardoll.ace.controller.ui.AbstractToolFrame ;

public class AutosamplerTest extends AbstractToolFrame
{
  private static final long serialVersionUID = -37627529259398662L ;
  
  public static final String TOOL_NAME = "autosampler test";

  public AutosamplerTest(JPanel mainPanel)
  {
    super(mainPanel) ;
  }

  private static final Logger _LOG = Log.HIGH_LEVEL;
  
  public static final List<String> OPERATIONS = new ArrayList<>();
  
  static
  {
    // TODO populate the OPERATIONS.
    OPERATIONS.add("coucou");
    OPERATIONS.add("hello");
  }
  
  static class AutosamplerControl extends AbstractTestControl
  {
    public AutosamplerControl(ParametresSession parametresSession)
        throws InitializationException, ConfigurationException
    {
      super(parametresSession, false, true, false) ;
    }

    @Override
    protected String getToolName()
    {
      return TOOL_NAME ;
    }
    
    @Override
    void start()
    {
      // TODO Auto-generated method stub
    }
    
    class AutosamplerTestThread extends AbstractThreadControl
    {
      public AutosamplerTestThread(AbstractToolControl toolCtrl)
      {
        super(toolCtrl, false) ;
      }

      @Override
      protected void threadLogic() throws CancellationException,
          InitializationException, ConfigurationException, Exception
      {
        // TODO
        Iterator<String> it = AutosamplerTest.OPERATIONS.iterator();
        int index = -1;
        
        // For every operation:
        String currentOperation = it.next(); index++;
        AutosamplerControl.this.updateCurrentOperation(index);
        _LOG.debug(String.format("executing %s", currentOperation));
        // Do something.
      }
    }
  }
  
  public static void main(String[] args)
  {
    _LOG.info("starting the autosampler test");
    
    try(ParametresSession parametresSession = ParametresSession.getInstance())
    {
      AutosamplerControl ctrl = new AutosamplerControl(parametresSession);
      TestPanel toolPanel = new TestPanel(ctrl, AutosamplerTest.OPERATIONS);
      ctrl.addControlPanel(toolPanel);
      AutosamplerTest tool = new AutosamplerTest(toolPanel);
      tool.setVisible(true);
    }
    catch (Exception e)
    {
      String msg = null;
      
      if(e.getCause() instanceof InitializationException)
      {
        msg = "initialisation of the autosampler test has crashed";
      }
      else
      {
        msg = "autosampler test has crashed";
      }
      
      _LOG.fatal(msg, e);
      Utils.reportError(msg, e);
    }
    finally
    {
      _LOG.info("shutdown the autosampler test");
    }
  }
}
