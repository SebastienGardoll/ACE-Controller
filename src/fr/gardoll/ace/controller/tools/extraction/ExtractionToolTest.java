package fr.gardoll.ace.controller.tools.extraction;

import java.nio.file.Path ;

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
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.Names ;
import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.pump.PumpControllerStub ;
import fr.gardoll.ace.controller.ui.PausableJPanelObserverStub ;

@TestMethodOrder(OrderAnnotation.class)
class ExtractionToolTest
{
  private static final Logger _LOG = LogManager.getLogger(ExtractionToolTest.class.getName());
  
  private static final long _PAUSE_DURATION = 500l;
  
  private static final long _TURN_DURATION  = 1000l;
  
  //Milliseconds before triggering pause or cancel.
  private static final long _TRIGGER_DELAY = 250l;
  
  private ExtractionToolControl _ctrl = null;
  private PausableJPanelObserverStub _toolPanel = null;

  @BeforeAll
  static void setUpBeforeClass() throws Exception
  {
    ParametresSession.isAutomatedTest = true;
    MotorControllerStub.ARM_TIME_INC = Passeur.convertBras(50.);
    MotorControllerStub.CAROUSEL_TIME_INC_FACTOR = 0.5;
    PumpControllerStub.TIME_FACTOR = 1.;
  }

  @BeforeEach
  void setUp() throws Exception
  {
    _LOG.info("******************** setup");
    ParametresSession parametresSession = ParametresSession.getInstance();
    this._ctrl = new ExtractionToolControl(parametresSession);
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

  private void autoResume(long pauseDuration)
  {
    // The current thread is pause when _ctrl.pause is called.
    // We must call resume method in another thread.
    Runnable threadLogic = () -> 
    {
      try
      {
        Thread.sleep(pauseDuration);
      }
      catch (InterruptedException e) {e.printStackTrace();}
      
      this._ctrl.resume();              
    };
    
    new Thread(threadLogic).start();
  }
  
  private void turnCarouselToRight()
  {
    this.turnCarousel(1);
  }
  
  private void turnCarouselToLeft()
  {
    this.turnCarousel(-1);
  }
  
  private void turnCarousel(int direction)
  {
    // The current thread is pause when _ctrl.pause is called.
    // We must call resume method in another thread.
    Runnable threadLogic = () -> 
    {
      try
      {
        Thread.sleep(ExtractionToolTest._PAUSE_DURATION);
      }
      catch (InterruptedException e) {e.printStackTrace();}
      
      if(direction >= 0)
      {
        ExtractionToolTest.this._ctrl.turnCarouselToRight();
      }
      else
      {
        ExtractionToolTest.this._ctrl.turnCarouselToLeft();
      }
    };
    
    new Thread(threadLogic).start();
  }
  
  private void triggerPause()
  {
    try
    {
      Thread.sleep(ExtractionToolTest._TRIGGER_DELAY);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    
    this._ctrl.pause();
  }
  
  @Test
  @Order(1)
  void test1n() throws InitializationException
  {
    _LOG.info("******************** test1n nominal sequence");
    int nbColumn = 3;
    int numColumn = 1;
    int numSequence = 1;
    String protocolFileName = "nominal_test.prt";
    Path protocolFilePath = Names.computeProtocolFilePath(protocolFileName);
    
    InitSession initSession = new InitSession(nbColumn, numColumn, numSequence,
                                              protocolFilePath);
    
    ControlPanel ctrlPanel = new ControlPanelAdapter()
    {
      @Override
      public void majActionActuelle(Action action)
      {
        switch(action.type)
        {
          case PAUSE_DONE:
          {
            ExtractionToolTest.this.autoResume(ExtractionToolTest._PAUSE_DURATION);
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
    
    
    this._ctrl.start(initSession);
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(2)
  void test2n()throws InitializationException
  {
    _LOG.info("******************** test2n rincing cases");
    int nbColumn = 1;
    int numColumn = 1;
    int numSequence = 1;
    String protocolFileName = "rincing_test.prt";
    Path protocolFilePath = Names.computeProtocolFilePath(protocolFileName);
   
    InitSession initSession = new InitSession(nbColumn, numColumn, numSequence,
                                              protocolFilePath);
    
    this._ctrl.start(initSession);
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(3)
  void test3n() throws InitializationException
  {
    _LOG.info("******************** test3n await tests");
    int nbColumn = 1;
    int numColumn = 1;
    int numSequence = 1;
    String protocolFileName = "await_test.prt";
    Path protocolFilePath = Names.computeProtocolFilePath(protocolFileName);
    
    InitSession initSession = new InitSession(nbColumn, numColumn, numSequence,
                                              protocolFilePath);
    
    ControlPanel ctrlPanel = new ControlPanelAdapter()
    {
      @Override
      public void majActionActuelle(Action action)
      {
        switch(action.type)
        {
          case PAUSE_DONE:
          {
            ExtractionToolTest.this.autoResume(ExtractionToolTest._PAUSE_DURATION);
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
    
    this._ctrl.start(initSession);
    this._toolPanel.waitMove();
  }
  
  @Order(4)
  @Test
  void test4n()throws InitializationException
  {
    _LOG.info("******************** test4n resuming to sequence 2/3");
    int nbColumn = 1;
    int numColumn = 1;
    int numSequence = 2;
    String protocolFileName = "resuming_test_seq.prt";
    Path protocolFilePath = Names.computeProtocolFilePath(protocolFileName);
   
    InitSession initSession = new InitSession(nbColumn, numColumn, numSequence,
                                              protocolFilePath);
    
    this._ctrl.start(initSession);
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(5)
  void test5n()throws InitializationException
  {
    _LOG.info("******************** test5n resuming to column 2/3");
    int nbColumn = 3;
    int numColumn = 2;
    int numSequence = 1;
    String protocolFileName = "resuming_test_col.prt";
    Path protocolFilePath = Names.computeProtocolFilePath(protocolFileName);
   
    InitSession initSession = new InitSession(nbColumn, numColumn, numSequence,
                                              protocolFilePath);
    
    this._ctrl.start(initSession);
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(6)
  void test6n()throws InitializationException
  {
    _LOG.info("******************** test6n resuming to column 2/3 & sequence 2/3");
    int nbColumn = 3;
    int numColumn = 2;
    int numSequence = 2;
    String protocolFileName = "resuming_test_seq.prt";
    Path protocolFilePath = Names.computeProtocolFilePath(protocolFileName);
   
    InitSession initSession = new InitSession(nbColumn, numColumn, numSequence,
                                              protocolFilePath);
    
    this._ctrl.start(initSession);
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(7)
  void test7n()throws InitializationException
  {
    _LOG.info("******************** test7n sequence pause and turn to the right");
    int nbColumn = 1;
    int numColumn = 1;
    int numSequence = 1;
    String protocolFileName = "solo_sequence_pause_test.prt";
    Path protocolFilePath = Names.computeProtocolFilePath(protocolFileName);
   
    InitSession initSession = new InitSession(nbColumn, numColumn, numSequence,
                                              protocolFilePath);

    ControlPanel ctrlPanel = new ControlPanelAdapter()
    {
      @Override
      public void majActionActuelle(Action action)
      {
        switch(action.type)
        {
          case PAUSE_DONE:
          {
            ExtractionToolTest.this.turnCarouselToRight();
            break;
          }
          
          case CAROUSEL_TURN_RIGHT:
          {
            ExtractionToolTest.this.autoResume(ExtractionToolTest._TURN_DURATION);
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
    
    this._ctrl.start(initSession);
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(8)
  void test8n()throws InitializationException
  {
    _LOG.info("******************** test8n sequence pause and turn to the left");
    int nbColumn = 1;
    int numColumn = 1;
    int numSequence = 1;
    String protocolFileName = "solo_sequence_pause_test.prt";
    Path protocolFilePath = Names.computeProtocolFilePath(protocolFileName);
   
    InitSession initSession = new InitSession(nbColumn, numColumn, numSequence,
                                              protocolFilePath);

    ControlPanel ctrlPanel = new ControlPanelAdapter()
    {
      @Override
      public void majActionActuelle(Action action)
      {
        switch(action.type)
        {
          case PAUSE_DONE:
          {
            ExtractionToolTest.this.turnCarouselToLeft();
            break;
          }
          
          case CAROUSEL_TURN_LEFT:
          {
            ExtractionToolTest.this.autoResume(ExtractionToolTest._TURN_DURATION);
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
    
    this._ctrl.start(initSession);
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(9)
  void test9n()throws InitializationException
  {
    _LOG.info("******************** test9n turn to the right while waiting");
    int nbColumn = 1;
    int numColumn = 1;
    int numSequence = 1;
    String protocolFileName = "solo_sequence_no_pause_test.prt";
    Path protocolFilePath = Names.computeProtocolFilePath(protocolFileName);
   
    InitSession initSession = new InitSession(nbColumn, numColumn, numSequence,
                                              protocolFilePath);

    ControlPanel ctrlPanel = new ControlPanelAdapter()
    {
      @Override
      public void majActionActuelle(Action action)
      {
        switch(action.type)
        {
          case SEQUENCE_AWAIT:
          {
            ExtractionToolTest.this.triggerPause();
            break;
          }
          
          case PAUSE_DONE:
          {
            ExtractionToolTest.this.turnCarouselToRight();
            break;
          }
          
          case CAROUSEL_TURN_RIGHT:
          {
            ExtractionToolTest.this.autoResume(ExtractionToolTest._TURN_DURATION);
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
    
    this._ctrl.start(initSession);
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(10)
  void test10n()throws InitializationException
  {
    _LOG.info("******************** test10n turn to the left while waiting");
    int nbColumn = 1;
    int numColumn = 1;
    int numSequence = 1;
    String protocolFileName = "solo_sequence_no_pause_test.prt";
    Path protocolFilePath = Names.computeProtocolFilePath(protocolFileName);
   
    InitSession initSession = new InitSession(nbColumn, numColumn, numSequence,
                                              protocolFilePath);

    ControlPanel ctrlPanel = new ControlPanelAdapter()
    {
      @Override
      public void majActionActuelle(Action action)
      {
        switch(action.type)
        {
          case SEQUENCE_AWAIT:
          {
            ExtractionToolTest.this.triggerPause();
            break;
          }
          
          case PAUSE_DONE:
          {
            ExtractionToolTest.this.turnCarouselToLeft();
            break;
          }
          
          case CAROUSEL_TURN_LEFT:
          {
            ExtractionToolTest.this.autoResume(ExtractionToolTest._TURN_DURATION);
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
    
    this._ctrl.start(initSession);
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(11)
  void test11n()throws InitializationException
  {
    _LOG.info("******************** test11n turn to the right while usr pause");
    int nbColumn = 1;
    int numColumn = 1;
    int numSequence = 1;
    String protocolFileName = "solo_sequence_no_pause_test.prt";
    Path protocolFilePath = Names.computeProtocolFilePath(protocolFileName);
   
    InitSession initSession = new InitSession(nbColumn, numColumn, numSequence,
                                              protocolFilePath);

    ControlPanel ctrlPanel = new ControlPanelAdapter()
    {
      @Override
      public void majActionActuelle(Action action)
      {
        switch(action.type)
        {
          case COLUMN_DIST_START:
          {
            ExtractionToolTest.this.triggerPause();
            break;
          }
          
          case PAUSE_DONE:
          {
            ExtractionToolTest.this.turnCarouselToRight();
            break;
          }
          
          case CAROUSEL_TURN_RIGHT:
          {
            ExtractionToolTest.this.autoResume(ExtractionToolTest._TURN_DURATION);
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
    
    this._ctrl.start(initSession);
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(12)
  void test12n()throws InitializationException
  {
    _LOG.info("******************** test12n turn to the left while usr pause");
    int nbColumn = 1;
    int numColumn = 1;
    int numSequence = 1;
    String protocolFileName = "solo_sequence_no_pause_test.prt";
    Path protocolFilePath = Names.computeProtocolFilePath(protocolFileName);
   
    InitSession initSession = new InitSession(nbColumn, numColumn, numSequence,
                                              protocolFilePath);

    ControlPanel ctrlPanel = new ControlPanelAdapter()
    {
      @Override
      public void majActionActuelle(Action action)
      {
        switch(action.type)
        {
          case COLUMN_DIST_START:
          {
            ExtractionToolTest.this.triggerPause();
            break;
          }
          
          case PAUSE_DONE:
          {
            ExtractionToolTest.this.turnCarouselToLeft();
            break;
          }
          
          case CAROUSEL_TURN_LEFT:
          {
            ExtractionToolTest.this.autoResume(ExtractionToolTest._TURN_DURATION);
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
    
    this._ctrl.start(initSession);
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(13)
  void test1c() throws InitializationException
  {
    _LOG.info("******************** test1c cancel while awaiting");
    int nbColumn = 1;
    int numColumn = 1;
    int numSequence = 1;
    String protocolFileName = "await_cancel_pause_test.prt";
    Path protocolFilePath = Names.computeProtocolFilePath(protocolFileName);
    
    InitSession initSession = new InitSession(nbColumn, numColumn, numSequence, protocolFilePath);
    
    
    ControlPanel ctrlPanel = new ControlPanelAdapter()
    {
      @Override
      public void majActionActuelle(Action action)
      {
        switch(action.type)
        {
          case PAUSE_DONE:
          {
            ExtractionToolTest.this.autoResume(ExtractionToolTest._PAUSE_DURATION);
            break;
          }
          
          case SEQUENCE_AWAIT:
          {
            try
            {
              Thread.sleep(ExtractionToolTest._TRIGGER_DELAY);
            }
            catch (InterruptedException e)
            {
              e.printStackTrace();
            }
            
            ExtractionToolTest.this._ctrl.cancel();
            
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
    
    
    this._ctrl.start(initSession);
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(14)
  void test1p() throws InitializationException
  {
    _LOG.info("******************** test1p pause while awaiting");
    int nbColumn = 1;
    int numColumn = 1;
    int numSequence = 1;
    String protocolFileName = "await_cancel_pause_test.prt";
    Path protocolFilePath = Names.computeProtocolFilePath(protocolFileName);
    
    InitSession initSession = new InitSession(nbColumn, numColumn, numSequence, protocolFilePath);
    
    
    ControlPanel ctrlPanel = new ControlPanelAdapter()
    {
      @Override
      public void majActionActuelle(Action action)
      {
        switch(action.type)
        {
          case PAUSE_DONE:
          {
            ExtractionToolTest.this.autoResume(ExtractionToolTest._PAUSE_DURATION);
            break;
          }
          
          case SEQUENCE_AWAIT:
          {
            ExtractionToolTest.this.triggerPause();
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
    
    
    this._ctrl.start(initSession);
    this._toolPanel.waitMove();
  }
}
