package fr.gardoll.ace.controller.tools.pump;

import java.util.SortedSet ;
import java.util.TreeSet ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;
import org.junit.jupiter.api.AfterEach ;
import org.junit.jupiter.api.BeforeAll ;
import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation ;
import org.junit.jupiter.api.Order ;
import org.junit.jupiter.api.Test ;
import org.junit.jupiter.api.TestMethodOrder ;

import fr.gardoll.ace.controller.autosampler.MotorControllerStub ;
import fr.gardoll.ace.controller.autosampler.Passeur ;
import fr.gardoll.ace.controller.core.Action ;
import fr.gardoll.ace.controller.core.ControlPanel ;
import fr.gardoll.ace.controller.core.ControlPanelAdapter ;
import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.pump.PumpControllerStub ;
import fr.gardoll.ace.controller.ui.PausableJPanelObserverStub ;

@TestMethodOrder(OrderAnnotation.class)
class PumpToolTest
{
  private static final Logger _LOG = LogManager.getLogger(PumpToolTest.class.getName());

  // Milliseconds before triggering pause or cancel.
  private static final long _TRIGGER_DELAY = 250l;
  
  // Duration of a pause in milliseconds.
  private static final long _PAUSE_DELAY = 500l;
  
  private PumpToolControl _ctrl = null;
  private PausableJPanelObserverStub _toolPanel = null;
  
  @BeforeAll
  static void setUpBeforeClass() throws Exception
  {
    ParametresSession.isAutomatedTest = true;
    MotorControllerStub.ARM_TIME_INC = Passeur.convertBras(10.) / 6;
    MotorControllerStub.CAROUSEL_TIME_INC_FACTOR = 5;
    PumpControllerStub.TIME_FACTOR = 35.;
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
  @Order(1)
  void test1n()
  {
    _LOG.info("******************** test1n clean line 1");
    SortedSet<Integer> lines = new TreeSet<Integer>();
    lines.add(1);
    this._ctrl.start(lines, 5);
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(2)
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
  @Order(3)
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
              Thread.sleep(PumpToolTest._TRIGGER_DELAY);
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
  @Order(4)
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
                Thread.sleep(PumpToolTest._TRIGGER_DELAY);
              }
              catch (InterruptedException e) {e.printStackTrace();}
              
              PumpToolTest.this._ctrl.pause();
              PumpToolTest.this._toolPanel.waitPause();
              try
              {
                Thread.sleep(_PAUSE_DELAY);
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
  @Order(5)
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
              Thread.sleep(PumpToolTest._TRIGGER_DELAY);
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
  @Order(6)
  void test2p()
  {
    _LOG.info("******************** test2p pause while withdrawing");
    ControlPanel ctrlPanel = new ControlPanelAdapter()
    {
      @Override
      public void majActionActuelle(Action action)
      {
        switch(action.type)
        {
          case WITHDRAWING:
          {
            // The current thread is pause when _ctrl.pause is called.
            // We must call resume method in another thread.
            Runnable threadLogic = () -> 
            {
              try
              {
                Thread.sleep(PumpToolTest._TRIGGER_DELAY);
              }
              catch (InterruptedException e) {e.printStackTrace();}
              
              PumpToolTest.this._ctrl.pause();
              PumpToolTest.this._toolPanel.waitPause();
              try
              {
                Thread.sleep(_PAUSE_DELAY);
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
}
