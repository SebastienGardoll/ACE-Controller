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
    _LOG.info("******************** setup");
    ParametresSession parametresSession = ParametresSession.getInstance();
    this._ctrl = new AutosamplerToolControl(parametresSession);
    this._toolPanel = new PausableJPanelObserverStub(this._ctrl);
    this._ctrl.addControlPanel(this._toolPanel);
    this._toolPanel.waitPanel();
  }

  @AfterEach
  void tearDown() throws Exception
  {
    _LOG.info("******************** teardown");
    this._ctrl.close();
    this._toolPanel.waitPanel();
    ParametresSession.getInstance().close();
  }

  private void openColumnFile()
  {
    String columnFileName = "nouvelle colonne.cln";
    
    Path rootDir = Utils.getInstance().getRootDir();
    Path filePath = rootDir.resolve(Names.CONFIG_DIRNAME)
                           .resolve(Names.COLUMN_DIRNAME)
                           .resolve(columnFileName);
    
    this._ctrl.openColumn(filePath);
  }
  
  @Test
  void testArm1n() throws InterruptedException
  {
    _LOG.info("******************** testArm1n arm go to top");
    this._ctrl.armGoButee();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void testArm2n()
  {
    _LOG.info("******************** testArm2n arm go to trash");
    this._ctrl.armGoTrash();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void testArm3n()
  {
    _LOG.info("******************** testArm3n arm go to column");
    
    this.openColumnFile();
    this._ctrl.armGoColonne();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void testArm4n()
  {
    _LOG.info("******************** testArm4n arm free move up");
    
    this._ctrl.armFreeMove(100);
    this._toolPanel.waitPanel();
  }
  
  @Test
  void testArm5n()
  {
    _LOG.info("******************** testArm5n arm free move down");
    
    this._ctrl.armFreeMove(-100);
    this._toolPanel.waitPanel();
  }

  @Test
  void testArm1c() throws InterruptedException
  {
    _LOG.info("******************** testArm1c cancel arm go to top");
    this._ctrl.armGoButee();
    this._ctrl.cancel();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void testArm2c() throws InterruptedException
  {
    _LOG.info("******************** testArm2c cancel arm go to trash");
    this._ctrl.armGoTrash();
    Thread.sleep(500);
    this._ctrl.cancel();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void testArm3c() throws InterruptedException
  {
    _LOG.info("******************** testArm3c cancel arm go to column");
    
    this.openColumnFile();
    this._ctrl.armGoColonne();
    Thread.sleep(500);
    this._ctrl.cancel();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void testArm4c() throws InterruptedException
  {
    _LOG.info("******************** testArm4c cancel arm free move up");
    
    this._ctrl.armFreeMove(100);
    Thread.sleep(500);
    this._ctrl.cancel();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void testArm5c() throws InterruptedException
  {
    _LOG.info("******************** testArm5c cancel arm free move down");
    
    this._ctrl.armFreeMove(-100);
    Thread.sleep(500);
    this._ctrl.cancel();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void testArm1p() throws InterruptedException
  {
    _LOG.info("******************** testArm1p pause arm go to top");
    this._ctrl.armGoButee();
    this._ctrl.pause();
    Thread.sleep(500);
    this._ctrl.resume();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void testArm2p() throws InterruptedException
  {
    _LOG.info("******************** testArm2p pause arm go to trash");
    this._ctrl.armGoTrash();
    Thread.sleep(500);
    this._ctrl.pause();
    Thread.sleep(500);
    this._ctrl.resume();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void testArm3p() throws InterruptedException
  {
    _LOG.info("******************** testArm3p pause arm go to column");
    
    this.openColumnFile();
    this._ctrl.armGoColonne();
    Thread.sleep(500);
    this._ctrl.pause();
    Thread.sleep(500);
    this._ctrl.resume();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void testArm4p() throws InterruptedException
  {
    _LOG.info("******************** testArm4p pause arm free move up");
    
    this._ctrl.armFreeMove(100);
    Thread.sleep(500);
    this._ctrl.pause();
    Thread.sleep(500);
    this._ctrl.resume();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void testArm5p() throws InterruptedException
  {
    _LOG.info("******************** testArm5p pause arm free move down");
    
    this._ctrl.armFreeMove(-100);
    Thread.sleep(500);
    this._ctrl.pause();
    Thread.sleep(500);
    this._ctrl.resume();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void testCarousel1n()
  {
    _LOG.info("******************** testCarousel1n turn right");
    this._ctrl.carouselTurnRight();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void testCarousel2n()
  {
    _LOG.info("******************** testCarousel2n turn left");
    this._ctrl.carouselTurnLeft();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void testCarousel3n()
  {
    _LOG.info("******************** testCarousel3n go to position 10");
    this._ctrl.carouselGoPosition(10);
    this._toolPanel.waitPanel();
  }
  
  @Test
  void testCarousel1c() throws InterruptedException
  {
    _LOG.info("******************** testCarousel1c cancel turn right");
    this._ctrl.carouselTurnRight();
    Thread.sleep(500);
    this._ctrl.cancel();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void testCarousel2c() throws InterruptedException
  {
    _LOG.info("******************** testCarousel2c cancel turn left");
    this._ctrl.carouselTurnLeft();
    Thread.sleep(500);
    this._ctrl.cancel();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void testCarousel3c() throws InterruptedException
  {
    _LOG.info("******************** testCarousel3c cancel go to position 10");
    this._ctrl.carouselGoPosition(10);
    Thread.sleep(500);
    this._ctrl.cancel();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void testCarousel1p() throws InterruptedException
  {
    _LOG.info("******************** testCarousel1p pause turn right");
    this._ctrl.carouselTurnRight();
    Thread.sleep(500);
    this._ctrl.pause();
    Thread.sleep(500);
    this._ctrl.resume();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void testCarousel2p() throws InterruptedException
  {
    _LOG.info("******************** testCarousel2p pause turn left");
    this._ctrl.carouselTurnLeft();
    Thread.sleep(500);
    this._ctrl.pause();
    Thread.sleep(500);
    this._ctrl.resume();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void testCarousel3p() throws InterruptedException
  {
    _LOG.info("******************** testCarousel3p pause go to position 10");
    this._ctrl.carouselGoPosition(10);
    Thread.sleep(500);
    this._ctrl.pause();
    Thread.sleep(500);
    this._ctrl.resume();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void scriptedTest1() throws InterruptedException
  {
    _LOG.info("******************** scripted test 1");
    _LOG.info("* arm goes to trash ; carousel turns left ; pause ; resume ; carousel move to pos 14 ; reinit *");
    this._ctrl.armGoTrash();
    this._toolPanel.waitPanel();
    
    this._ctrl.carouselTurnLeft();
    Thread.sleep(500);
    this._ctrl.pause();
    Thread.sleep(500);
    this._ctrl.resume();
    this._toolPanel.waitPanel();
    
    this._ctrl.carouselGoPosition(14);
    this._toolPanel.waitPanel();
    
    this._ctrl.reinit();
    this._toolPanel.waitPanel();
  }
  
  @Test
  void scriptedTest2() throws InterruptedException
  {
    _LOG.info("******************** scripted test 2");
    _LOG.info("* carousel turns right ; pause ; resume ; arm to trash ; arm to trash ; arm to top ; cancel *");
    
    this._ctrl.carouselTurnRight();
    Thread.sleep(500);
    this._ctrl.pause();
    Thread.sleep(500);
    this._ctrl.resume();
    this._toolPanel.waitPanel();
    
    this._ctrl.armGoTrash();
    this._toolPanel.waitPanel();
    
    this._ctrl.armGoTrash();
    this._toolPanel.waitPanel();
    
    this._ctrl.armGoButee();
    this._ctrl.cancel();
    this._toolPanel.waitPanel();
  }
}
