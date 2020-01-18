package fr.gardoll.ace.controller.tools.tests;

import java.util.List ;
import java.util.Optional ;

import javax.swing.JPanel ;

import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.AbstractCancelableToolControl ;
import fr.gardoll.ace.controller.core.AbstractThreadControl ;
import fr.gardoll.ace.controller.core.AbstractToolControl ;
import fr.gardoll.ace.controller.core.Action ;
import fr.gardoll.ace.controller.core.ActionType ;
import fr.gardoll.ace.controller.core.CancellationException ;
import fr.gardoll.ace.controller.core.ControlPanel ;
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.Log ;
import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.settings.ConfigurationException ;
import fr.gardoll.ace.controller.settings.ParametresSession ;
import fr.gardoll.ace.controller.ui.AbstractToolFrame ;

public class TestDriver 
{
  private static final Logger _LOG = Log.HIGH_LEVEL;
  
  private final List<Operation> _operations;
  public final String name;

  public TestDriver(String name, List<Operation> operations)
  {
    this.name = name;
    this._operations = operations;
  }
  
  class TestFrame extends AbstractToolFrame
  {
    private static final long serialVersionUID = 5648636412856199155L ;
    
    public TestFrame(JPanel mainPanel)
    {
      super(mainPanel) ;
    }
  }
  
  public void run(boolean hasPump, boolean hasAutosampler, boolean hasValves)
  {
    _LOG.info(String.format("starting the %s test", this.name));
    
    try(ParametresSession parametresSession = ParametresSession.getInstance())
    {
      TestControl ctrl = new TestControl(parametresSession, hasPump, hasAutosampler, hasValves) ;
      TestPanel toolPanel = new TestPanel(ctrl, this._operations);
      ctrl.addControlPanel(toolPanel);
      TestFrame tool = new TestFrame(toolPanel);
      tool.setVisible(true);
    }
    catch (Exception e)
    {
      String msg = null;
      
      if(e.getCause() instanceof InitializationException)
      {
        msg = String.format("initialisation of the %s test has crashed", this.name);
      }
      else
      {
        msg = String.format("%s test has crashed", this.name);
      }
      
      _LOG.fatal(msg, e);
      Utils.reportError(msg, e);
    }
    finally
    {
      _LOG.info(String.format("shutdown the %s test", this.name));
    }
  }
  
  class TestControl extends AbstractCancelableToolControl
  {
    public TestControl(ParametresSession parametresSession, boolean hasPump,
        boolean hasAutosampler, boolean hasValves)
        throws InitializationException, ConfigurationException
    {
      super(parametresSession, hasPump, hasAutosampler, hasValves) ;
    }

    @Override
    protected String getToolName()
    {
      return TestDriver.this.name ;
    }
    
    protected void updateCurrentOperation(int index)
    {
      for(ControlPanel panel: this.getCtrlPanels())
      {
        Action action = new Action(ActionType.TEST, Optional.of(index));
        panel.majActionActuelle(action);
      }
    }
    
    @Override
    protected void closeOperations()
    {
      _LOG.debug("controller has nothing to do while closing the tool");
    }
    
    void start()
    {
      try
      {
        String msg = String.format("starting %s test thread", TestDriver.this.name);
        _LOG.debug(msg);
        this.start(new TestThread(this));
      }
      catch(Exception e)
      {
        String msg = String.format("%s test thread start has crashed", TestDriver.this.name);
        _LOG.fatal(msg, e);
        this.handleException(msg, e);
      }
    }
    
    class TestThread extends AbstractThreadControl
    {
      public TestThread(AbstractToolControl toolCtrl)
      {
        super(toolCtrl, false) ;
      }

      @Override
      protected void threadLogic() throws CancellationException,
          InitializationException, ConfigurationException, Exception
      {
        {
          String msg = String.format("starting %s test", TestDriver.this.name);
          _LOG.info(msg);
        }
        
        int index = 0;
        
        for(Operation op: TestDriver.this._operations)
        {
          TestControl.this.updateCurrentOperation(index);
          _LOG.debug(String.format("executing %s", op.name));
          op.execute();
          index++;
        }
        
        {
          String msg = String.format("%s test is completed", TestDriver.this.name);
          _LOG.info(msg);
        }
        
        // Disable highlighting.
        TestControl.this.updateCurrentOperation(-1);
      }
    }
  }
}
