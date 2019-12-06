package fr.gardoll.ace.controller.tools.autosampler;

import java.nio.file.Path ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;
import org.junit.jupiter.api.AfterAll ;
import org.junit.jupiter.api.AfterEach ;
import org.junit.jupiter.api.BeforeAll ;
import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test ;

import fr.gardoll.ace.controller.core.Names ;
import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.ui.PausableJPanelObserverStub ;

class AutosamplerToolTest
{
  private static final Logger _LOG = LogManager.getLogger(AutosamplerToolTest.class.getName());
  
  AutosamplerToolControl _ctrl = null;
  PausableJPanelObserverStub _toolPanel = null;

  @BeforeAll
  static void setUpBeforeClass() throws Exception
  {
    ParametresSession.isAutomatedTest = true;
  }

  @AfterAll
  static void tearDownAfterClass() throws Exception
  {
  }

  @BeforeEach
  void setUp() throws Exception
  {
    _LOG.debug("******************** setup");
    ParametresSession parametresSession = ParametresSession.getInstance();
    this._ctrl = new AutosamplerToolControl(parametresSession);
    this._toolPanel = new PausableJPanelObserverStub(this._ctrl);
    this._ctrl.addControlPanel(this._toolPanel);
    this._toolPanel.waitPanel();
  }

  @AfterEach
  void tearDown() throws Exception
  {
    _LOG.debug("******************** teardown");
    this._ctrl.close();
    this._toolPanel.waitPanel();
    ParametresSession.getInstance().close();
  }

  @Test
  void test1() throws InterruptedException
  {
    _LOG.debug("******************** test1 arm go to top");
    this._ctrl.armGoButee();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void test2()
  {
    _LOG.debug("******************** test2 arm go to trash");
    this._ctrl.armGoTrash();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void test3()
  {
    _LOG.debug("******************** test3 arm go to column");
    
    String columnFileName = "nouvelle colonne.cln";
    
    Path rootDir = Utils.getInstance().getRootDir();
    Path filePath = rootDir.resolve(Names.CONFIG_DIRNAME)
                           .resolve(Names.COLUMN_DIRNAME)
                           .resolve(columnFileName);
    
    this._ctrl.openColumn(filePath);
    this._ctrl.armGoColonne();
    this._toolPanel.waitPanel();
  }

}
