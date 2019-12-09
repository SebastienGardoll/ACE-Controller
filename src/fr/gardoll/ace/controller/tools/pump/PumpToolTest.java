package fr.gardoll.ace.controller.tools.pump;

import java.util.SortedSet ;
import java.util.TreeSet ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;
import org.junit.jupiter.api.AfterEach ;
import org.junit.jupiter.api.BeforeAll ;
import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test ;

import fr.gardoll.ace.controller.core.Action ;
import fr.gardoll.ace.controller.core.ControlPanel ;
import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.ui.PausableJPanelObserverStub ;

class PumpToolTest
{
  private static final Logger _LOG = LogManager.getLogger(PumpToolTest.class.getName());
  
  PumpToolControl _ctrl = null;
  PausableJPanelObserverStub _toolPanel = null;
  
  @BeforeAll
  static void setUpBeforeClass() throws Exception
  {
    ParametresSession.isAutomatedTest = true;
  }

  @BeforeEach
  void setUp() throws Exception
  {
    _LOG.info("******************** setup");
    ParametresSession parametresSession = ParametresSession.getInstance();
    this._ctrl = new PumpToolControl(parametresSession);
    this._toolPanel = new PausableJPanelObserverStub(this._ctrl);
    this._ctrl.addControlPanel(this._toolPanel);
    this._toolPanel.waitMove();
  }

  @AfterEach
  void tearDown() throws Exception
  {
    _LOG.info("******************** teardown");
    this._ctrl.close();
    this._toolPanel.waitMove();
    ParametresSession.getInstance().close();
  }
  
  @Test
  void test1n()
  {
    _LOG.info("******************** test1n clean line 1");
    SortedSet<Integer> lines = new TreeSet<Integer>();
    lines.add(1);
    this._ctrl.start(lines, 5);
    this._toolPanel.waitMove();
  }
  
  @Test
  void test2n()
  {
    _LOG.info("******************** test2n clean line 1 to 3");
    SortedSet<Integer> lines = new TreeSet<Integer>();
    lines.add(1);
    lines.add(2);
    lines.add(3);
    this._ctrl.start(lines, 5);
    this._toolPanel.waitMove();
  }
  
  @Test
  void test1c()
  {
    _LOG.info("******************** test1c cancel while infusing");
    ControlPanel ctrlPanel = new ControlPanelAdapter()
    {
      @Override
      public void majActionActuelle(Action action)
      {
        switch(action.type)
        {
          case INFUSING:
          {
            try
            {
              Thread.sleep(250);
            }
            catch (InterruptedException e) {e.printStackTrace();}
            
            PumpToolTest.this._ctrl.cancel();
            break;
          }
          
          default:
          {
            break;
          }
        }
      }
    };
    this._ctrl.addControlPanel(ctrlPanel);
    
    SortedSet<Integer> lines = new TreeSet<Integer>();
    lines.add(1);
    this._ctrl.start(lines, 5);
    PumpToolTest.this._toolPanel.waitMove();
  }
  
  @Test
  void test1p()
  {
    _LOG.info("******************** test1p pause while infusing");
    ControlPanel ctrlPanel = new ControlPanelAdapter()
    {
      @Override
      public void majActionActuelle(Action action)
      {
        switch(action.type)
        {
          case INFUSING:
          {
            // The current thread is pause when _ctrl.pause is called.
            // We must call resume method in another thread.
            Runnable threadLogic = () -> 
            {
              try
              {
                Thread.sleep(250);
              }
              catch (InterruptedException e) {e.printStackTrace();}
              
              PumpToolTest.this._ctrl.pause();
              PumpToolTest.this._toolPanel.waitPause();
              try
              {
                Thread.sleep(500);
              }
              catch (InterruptedException e) {e.printStackTrace();}
              PumpToolTest.this._ctrl.resume();              
            };
            
            new Thread(threadLogic).start();
            
            break;
          }
          
          default:
          {
            break;
          }
        }
      }
    };
    this._ctrl.addControlPanel(ctrlPanel);
    
    SortedSet<Integer> lines = new TreeSet<Integer>();
    lines.add(1);
    this._ctrl.start(lines, 5);
    PumpToolTest.this._toolPanel.waitMove();
  }
  
  @Test
  void test2c()
  {
    _LOG.info("******************** test2c cancel while withdrawing");
    ControlPanel ctrlPanel = new ControlPanelAdapter()
    {
      @Override
      public void majActionActuelle(Action action)
      {
        switch(action.type)
        {
          case WITHDRAWING:
          {
            try
            {
              Thread.sleep(250);
            }
            catch (InterruptedException e) {e.printStackTrace();}
            
            PumpToolTest.this._ctrl.cancel();
            break;
          }
          
          default:
          {
            break;
          }
        }
      }
    };
    this._ctrl.addControlPanel(ctrlPanel);
    
    SortedSet<Integer> lines = new TreeSet<Integer>();
    lines.add(1);
    this._ctrl.start(lines, 5);
    PumpToolTest.this._toolPanel.waitMove();
  }
  
  abstract class ControlPanelAdapter implements ControlPanel
  {
    @Override
    public void reportError(String msg, Throwable e)
    {
      // Nothing to do.
    }

    @Override
    public void reportError(String msg)
    {
      // Nothing to do.  
    }

    @Override
    public void displayModalMessage(String msg)
    {
      // Nothing to do.
    }

    @Override
    public boolean close()
    {
      // Nothing to do.
      return true;
    }

    @Override
    public void dispose()
    {
      // Nothing to do.
    }

    @Override
    public void enableStart(boolean isEnable)
    {
      // Nothing to do.
    }

    @Override
    public void enableClose(boolean isEnable)
    {
      // Nothing to do.
    }

    @Override
    public void enablePause(boolean isEnable)
    {
      // Nothing to do.
    }

    @Override
    public void enableResume(boolean isEnable)
    {
      // Nothing to do.
    }

    @Override
    public void enableCancel(boolean isEnable)
    {
      // Nothing to do.
    }

    @Override
    public void enableReinit(boolean isEnable)
    {
      // Nothing to do.
    }
  }
}
