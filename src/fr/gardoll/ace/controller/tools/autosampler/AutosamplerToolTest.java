package fr.gardoll.ace.controller.tools.autosampler;

import java.nio.file.Path ;

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
import fr.gardoll.ace.controller.column.Colonne ;
import fr.gardoll.ace.controller.core.Log ;
import fr.gardoll.ace.controller.settings.ParametresSession ;
import fr.gardoll.ace.controller.ui.PausableJPanelObserverStub ;

@TestMethodOrder(OrderAnnotation.class)
class AutosamplerToolTest
{
  private static final Logger _LOG = Log.HIGH_LEVEL;
  
  // Milliseconds before triggering pause or cancel.
  private static final long _TRIGGER_DELAY = 500l;
 
  // Duration of a pause in milliseconds.
  private static final long _PAUSE_DELAY = 500l;
  
  private AutosamplerToolControl _ctrl = null;
  private PausableJPanelObserverStub _toolPanel = null;

  @BeforeAll
  static void setUpBeforeClass() throws Exception
  {
    ParametresSession.isAutomatedTest = true;
    MotorControllerStub.ARM_TIME_INC = Passeur.convertBras(10.) / 6;
    MotorControllerStub.CAROUSEL_TIME_INC_FACTOR = 5;
  }

  @BeforeEach
  void setUp() throws Exception
  {
    _LOG.info("******************** setup");
    ParametresSession parametresSession = ParametresSession.getInstance();
    this._ctrl = new AutosamplerToolControl(parametresSession);
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

  private void openColumnFile()
  {
    String columnFileName = "nouvelle colonne.cln";
    
    Path columnFilePath = Colonne.computeColumnFilePath(columnFileName);
    
    this._ctrl.openColumn(columnFilePath);
  }
  
  @Test
  @Order(1)
  void testArm1n() throws InterruptedException
  {
    _LOG.info("******************** testArm1n arm go to top");
    this._ctrl.armGoButee();
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(2)
  void testArm2n()
  {
    _LOG.info("******************** testArm2n arm go to trash");
    this._ctrl.armGoTrash();
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(3)
  void testArm3n()
  {
    _LOG.info("******************** testArm3n arm go to column");
    
    this.openColumnFile();
    this._ctrl.armGoColonne();
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(4)
  void testArm4n()
  {
    _LOG.info("******************** testArm4n arm free move up");
    
    this._ctrl.armFreeMove(100);
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(5)
  void testArm5n()
  {
    _LOG.info("******************** testArm5n arm free move down");
    
    this._ctrl.armFreeMove(-100);
    this._toolPanel.waitMove();
  }

  @Test
  @Order(6)
  void testArm1c() throws InterruptedException
  {
    _LOG.info("******************** testArm1c cancel arm go to top");
    this._ctrl.armGoButee();
    this._ctrl.cancel();
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(7)
  void testArm2c() throws InterruptedException
  {
    _LOG.info("******************** testArm2c cancel arm go to trash");
    this._ctrl.armGoTrash();
    Thread.sleep(_TRIGGER_DELAY);
    this._ctrl.cancel();
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(8)
  void testArm3c() throws InterruptedException
  {
    _LOG.info("******************** testArm3c cancel arm go to column");
    
    this.openColumnFile();
    this._ctrl.armGoColonne();
    Thread.sleep(_TRIGGER_DELAY);
    this._ctrl.cancel();
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(9)
  void testArm4c() throws InterruptedException
  {
    _LOG.info("******************** testArm4c cancel arm free move up");
    
    this._ctrl.armFreeMove(100);
    Thread.sleep(_TRIGGER_DELAY);
    this._ctrl.cancel();
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(10)
  void testArm5c() throws InterruptedException
  {
    _LOG.info("******************** testArm5c cancel arm free move down");
    
    this._ctrl.armFreeMove(-100);
    Thread.sleep(_TRIGGER_DELAY);
    this._ctrl.cancel();
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(11)
  void testArm1p() throws InterruptedException
  {
    _LOG.info("******************** testArm1p pause arm go to top");
    this._ctrl.armGoButee();
    this._ctrl.pause();
    this._toolPanel.waitPause();
    Thread.sleep(_PAUSE_DELAY);
    this._ctrl.resume();
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(12)
  void testArm2p() throws InterruptedException
  {
    _LOG.info("******************** testArm2p pause arm go to trash");
    this._ctrl.armGoTrash();
    Thread.sleep(_TRIGGER_DELAY);
    this._ctrl.pause();
    this._toolPanel.waitPause();
    Thread.sleep(_PAUSE_DELAY);
    this._ctrl.resume();
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(13)
  void testArm3p() throws InterruptedException
  {
    _LOG.info("******************** testArm3p pause arm go to column");
    
    this.openColumnFile();
    this._ctrl.armGoColonne();
    Thread.sleep(_TRIGGER_DELAY);
    this._ctrl.pause();
    this._toolPanel.waitPause();
    Thread.sleep(_PAUSE_DELAY);
    this._ctrl.resume();
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(14)
  void testArm4p() throws InterruptedException
  {
    _LOG.info("******************** testArm4p pause arm free move up");
    
    this._ctrl.armFreeMove(100);
    Thread.sleep(_TRIGGER_DELAY);
    this._ctrl.pause();
    this._toolPanel.waitPause();
    Thread.sleep(_PAUSE_DELAY);
    this._ctrl.resume();
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(15)
  void testArm5p() throws InterruptedException
  {
    _LOG.info("******************** testArm5p pause arm free move down");
    
    this._ctrl.armFreeMove(-100);
    Thread.sleep(_TRIGGER_DELAY);
    this._ctrl.pause();
    this._toolPanel.waitPause();
    Thread.sleep(_PAUSE_DELAY);
    this._ctrl.resume();
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(16)
  void testCarousel1n()
  {
    _LOG.info("******************** testCarousel1n turn right");
    this._ctrl.carouselTurnRight();
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(17)
  void testCarousel2n()
  {
    _LOG.info("******************** testCarousel2n turn left");
    this._ctrl.carouselTurnLeft();
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(18)
  void testCarousel3n()
  {
    _LOG.info("******************** testCarousel3n go to position 10");
    this._ctrl.carouselGoPosition(10);
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(19)
  void testCarousel1c() throws InterruptedException
  {
    _LOG.info("******************** testCarousel1c cancel turn right");
    this._ctrl.carouselTurnRight();
    Thread.sleep(_TRIGGER_DELAY);
    this._ctrl.cancel();
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(20)
  void testCarousel2c() throws InterruptedException
  {
    _LOG.info("******************** testCarousel2c cancel turn left");
    this._ctrl.carouselTurnLeft();
    Thread.sleep(_TRIGGER_DELAY);
    this._ctrl.cancel();
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(21)
  void testCarousel3c() throws InterruptedException
  {
    _LOG.info("******************** testCarousel3c cancel go to position 10");
    this._ctrl.carouselGoPosition(10);
    Thread.sleep(_TRIGGER_DELAY);
    this._ctrl.cancel();
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(22)
  void testCarousel1p() throws InterruptedException
  {
    _LOG.info("******************** testCarousel1p pause turn right");
    this._ctrl.carouselTurnRight();
    Thread.sleep(_TRIGGER_DELAY);
    this._ctrl.pause();
    this._toolPanel.waitPause();
    Thread.sleep(_PAUSE_DELAY);
    this._ctrl.resume();
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(23)
  void testCarousel2p() throws InterruptedException
  {
    _LOG.info("******************** testCarousel2p pause turn left");
    this._ctrl.carouselTurnLeft();
    Thread.sleep(_TRIGGER_DELAY);
    this._ctrl.pause();
    this._toolPanel.waitPause();
    Thread.sleep(_PAUSE_DELAY);
    this._ctrl.resume();
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(24)
  void testCarousel3p() throws InterruptedException
  {
    _LOG.info("******************** testCarousel3p pause go to position 10");
    this._ctrl.carouselGoPosition(10);
    Thread.sleep(_TRIGGER_DELAY);
    this._ctrl.pause();
    this._toolPanel.waitPause();
    Thread.sleep(_PAUSE_DELAY);
    this._ctrl.resume();
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(25)
  void scriptedTest1() throws InterruptedException
  {
    _LOG.info("******************** scripted test 1");
    _LOG.info("* arm goes to trash ; carousel turns left ; pause ; resume ; carousel move to pos 14 ; reinit *");
    this._ctrl.armGoTrash();
    this._toolPanel.waitMove();
    
    this._ctrl.carouselTurnLeft();
    Thread.sleep(_TRIGGER_DELAY);
    this._ctrl.pause();
    this._toolPanel.waitPause();
    Thread.sleep(_PAUSE_DELAY);
    this._ctrl.resume();
    this._toolPanel.waitMove();
    
    this._ctrl.carouselGoPosition(14);
    this._toolPanel.waitMove();
    
    this._ctrl.reinit();
    this._toolPanel.waitMove();
  }
  
  @Test
  @Order(26)
  void scriptedTest2() throws InterruptedException
  {
    _LOG.info("******************** scripted test 2");
    _LOG.info("* carousel turns right ; pause ; resume ; arm to trash ; arm to trash ; arm to top ; cancel *");
    
    this._ctrl.carouselTurnRight();
    Thread.sleep(_TRIGGER_DELAY);
    this._ctrl.pause();
    this._toolPanel.waitPause();
    Thread.sleep(_PAUSE_DELAY);
    this._ctrl.resume();
    this._toolPanel.waitMove();
    
    this._ctrl.armGoTrash();
    this._toolPanel.waitMove();
    
    this._ctrl.armGoTrash();
    this._toolPanel.waitMove();
    
    this._ctrl.armGoButee();
    this._ctrl.cancel();
    this._toolPanel.waitMove();
  }
}
