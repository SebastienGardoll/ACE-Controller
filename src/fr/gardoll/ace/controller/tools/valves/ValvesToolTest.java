package fr.gardoll.ace.controller.tools.valves;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;
import org.junit.jupiter.api.AfterEach ;
import org.junit.jupiter.api.BeforeAll ;
import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test ;

import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.ui.CloseableJPanelObserverStub ;

class ValvesToolTest
{
  private static final Logger _LOG = LogManager.getLogger(ValvesToolTest.class.getName());
  
  private ValvesToolControl _ctrl = null;
  private CloseableJPanelObserverStub _toolPanel = null;
  
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
    this._ctrl = new ValvesToolControl(parametresSession);
    this._toolPanel = new CloseableJPanelObserverStub(this._ctrl);
    this._ctrl.addControlPanel(this._toolPanel);
  }

  @AfterEach
  void tearDown() throws Exception
  {
    _LOG.info("******************** teardown");
    this._ctrl.close();
    ParametresSession.getInstance().close();
  }

  @Test
  void scriptedTest1() throws InterruptedException
  {
    _LOG.info("******************** scripted test1");
    _LOG.info("* open sequentially every valve *");
    for(int valveId=0 ; valveId < 8 ; valveId++)
    {
      this._ctrl.handleValve(valveId);
      Thread.sleep(1000);
    }
  }
  
  @Test
  void scriptedTest2() throws InterruptedException
  {
    _LOG.info("******************** scripted test2");
    _LOG.info("* open and shut sequentially every valve *");
    for(int valveId=1 ; valveId < 8 ; valveId++)
    {
      this._ctrl.handleValve(valveId);
      Thread.sleep(1000);
      this._ctrl.handleValve(valveId);
      Thread.sleep(1000);
    }
  }

}
