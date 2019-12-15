package fr.gardoll.ace.controller.tools.extraction;

import java.nio.file.Path ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;
import org.junit.jupiter.api.AfterEach ;
import org.junit.jupiter.api.BeforeAll ;
import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test ;

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

class ExtractionToolTest
{
  private static final Logger _LOG = LogManager.getLogger(ExtractionToolTest.class.getName());
  
  private static final long _PAUSE_DURATION = 500l;
  
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

  @Test
  void test1n() throws InitializationException
  {
    _LOG.info("******************** test1n");
    int nbColumn = 3;
    int numColumn = 1;
    int numSequence = 1;
    String protocolFileName = "sr-spec_test.prt";
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
            // The current thread is pause when _ctrl.pause is called.
            // We must call resume method in another thread.
            Runnable threadLogic = () -> 
            {
              try
              {
                Thread.sleep(ExtractionToolTest._PAUSE_DURATION);
              }
              catch (InterruptedException e) {e.printStackTrace();}
              
              ExtractionToolTest.this._ctrl.resume();              
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
    
    
    this._ctrl.start(initSession);
    this._toolPanel.waitMove();
  }
}
