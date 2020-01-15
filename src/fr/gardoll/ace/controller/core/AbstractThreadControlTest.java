package fr.gardoll.ace.controller.core;

import java.util.Collections ;
import java.util.Set ;

import org.apache.logging.log4j.Logger ;
import org.junit.jupiter.api.AfterEach ;
import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation ;
import org.junit.jupiter.api.Order ;
import org.junit.jupiter.api.Test ;
import org.junit.jupiter.api.TestMethodOrder ;

import fr.gardoll.ace.controller.settings.ConfigurationException ;

@TestMethodOrder(OrderAnnotation.class)
class AbstractThreadControlTest
{
  private TestToolControlOperations _ctrl = null;
  
  private static final Logger _LOG = Log.HIGH_LEVEL;

  @BeforeEach
  void setUp() throws Exception
  {
    this._ctrl = new TestToolControlOperations();
  }
  
  @AfterEach
  void teardown()
  {
    _LOG.debug("*** end of test ***") ;
  }

  @Test
  @Order(1)
  void test1() throws InterruptedException
  {
    _LOG.info("######### TEST 1 #########") ;
    
    SleepingThread thread = new SleepingThread(this._ctrl);
    thread.start();
    Thread.sleep(2000);
    
    _LOG.debug("*** calling pause on the running thread ****") ;
    thread.pause();
    
    Thread.sleep(500); // Give the thread the time to check.
    _LOG.debug(String.format("*** state is: %s ***", this._ctrl.getState().getLiteral())) ;
    
    _LOG.debug("*** calling pause on the thread, again ****") ;
    thread.pause();
    _LOG.debug("*** it should not do anything ****") ;
    
    
    _LOG.debug("*** calling check pause from main thread ****") ;
    thread.checkPause();
    _LOG.debug("*** main thread unchanged ***") ;
    
    _LOG.debug("*** calling check interruption from main thread ****") ;
    thread.checkInterruption();
    _LOG.debug("*** main thread unchanged ***") ;
    
    _LOG.debug("*** calling check cancel from main thread ****") ;
    thread.checkCancel();
    _LOG.debug("*** main thread unchanged ***") ;
    
    _LOG.debug("*** unpause thread ***") ;
    thread.unPause();
    
    Thread.sleep(2000);
    
    _LOG.debug(String.format("*** state is: %s ***", this._ctrl.getState().getLiteral())) ;
    _LOG.debug("*** calling interrupt on the running thread ****") ;
    thread.interrupt();
    Thread.sleep(500); // Give the thread the time to check.
    _LOG.debug(String.format("*** state is: %s ***", this._ctrl.getState().getLiteral())) ;
    
    thread.join();
  }
  
  @Test
  @Order(2)
  void test2() throws InterruptedException
  {
    _LOG.info("######### TEST 2 #########") ;
    
    SleepingThread thread = new SleepingThread(this._ctrl);
    thread.start();
    Thread.sleep(2000);

    _LOG.debug("*** calling pause on the running thread ****") ;
    thread.pause();
    Thread.sleep(500); // Give the thread the time to check.
    _LOG.debug(String.format("*** state is: %s ***", this._ctrl.getState().getLiteral())) ;
    
    Thread.sleep(2000);
    _LOG.debug("*** calling interrupt on the paused thread ****") ;
    thread.interrupt();
    Thread.sleep(500); // Give the thread the process interruption.
    _LOG.debug(String.format("*** state is: %s ***", this._ctrl.getState().getLiteral())) ;

    thread.join();
  }
  
  @Test
  @Order(3)
  void test3() throws InterruptedException
  {
    _LOG.info("######### TEST 3 #########") ;
    
    SleepingThread thread = new SleepingThread(this._ctrl);
    thread.start();
    Thread.sleep(2000);

    _LOG.debug("*** calling cancel on the running thread ****") ;
    thread.cancel();
    Thread.sleep(500); // Give the thread the time to check.
    _LOG.debug(String.format("*** state is: %s ***", this._ctrl.getState().getLiteral())) ;
    
    Thread.sleep(2000);
    
    _LOG.debug("*** calling cancel (again) on the cancelled thread ****") ;
    thread.cancel();
    _LOG.debug("*** it should not do anything ***") ;
    _LOG.debug(String.format("*** state is: %s ***", this._ctrl.getState().getLiteral())) ;

    thread.join();
  }

  @Test
  @Order(4)
  void test4() throws InterruptedException
  {
    _LOG.info("######### TEST 4 #########") ;
    
    SleepingThread thread = new SleepingThread(this._ctrl);
    thread.start();
    Thread.sleep(2000);

    _LOG.debug("*** calling pause on the running thread ****") ;
    thread.pause();
    Thread.sleep(500); // Give the thread the time to check.
    _LOG.debug(String.format("*** state is: %s ***", this._ctrl.getState().getLiteral())) ;
    
    Thread.sleep(2000);
    _LOG.debug("*** calling cancel on the paused thread ****") ;
    thread.cancel();
    _LOG.debug("*** thread should not be cancelled ***") ;
    _LOG.debug(String.format("*** state is: %s ***", this._ctrl.getState().getLiteral())) ;
    
    Thread.sleep(2000);
    _LOG.debug("*** calling interrupt on the paused thread ****") ;
    thread.interrupt();
    Thread.sleep(500); // Give the thread the process interruption.
    _LOG.debug(String.format("*** state is: %s ***", this._ctrl.getState().getLiteral())) ;

    thread.join();
  }

  @Test
  @Order(5)
  void test5() throws InterruptedException
  {
    _LOG.info("######### TEST 5 #########") ;
    
    SleepingThread thread = new SleepingThread(this._ctrl);
    thread.start();
    Thread.sleep(2000);
    
    _LOG.debug("*** calling cancel on the running thread ****") ;
    thread.cancel();
    Thread.sleep(500); // Give the thread the time to check.
    _LOG.debug(String.format("*** state is: %s ***", this._ctrl.getState().getLiteral())) ;
    
    Thread.sleep(2000);
    
    _LOG.debug("*** calling pause on the cancelled thread ****") ;
    thread.pause();
    _LOG.debug("*** it should not do anything ****") ;
    Thread.sleep(500); // Give the thread the time to check.
    _LOG.debug(String.format("*** state is: %s ***", this._ctrl.getState().getLiteral())) ;

    thread.join();
  }

  @Test
  @Order(6)
  void test6() throws InterruptedException
  {
    _LOG.info("######### TEST 6 #########") ;
    
    SleepingThread thread = new SleepingThread(this._ctrl);
    thread.start();
    Thread.sleep(2000);

    _LOG.debug("*** calling interrupt on the running thread ****") ;
    thread.interrupt();
    Thread.sleep(500); // Give the thread the time to check.
    _LOG.debug(String.format("*** state is: %s ***", this._ctrl.getState().getLiteral())) ;
    
    Thread.sleep(2000);
    
    _LOG.debug("*** calling pause on the interrupted thread ****") ;
    thread.pause();
    _LOG.debug("*** it should not do anything ***") ;
    Thread.sleep(500); // Give the thread the time to check.
    _LOG.debug(String.format("*** state is: %s ***", this._ctrl.getState().getLiteral())) ;
    
    thread.join();
  }

  @Test
  @Order(7)
  void test7() throws InterruptedException
  {
    _LOG.info("######### TEST 7 #########") ;
    
    SleepingThread thread = new SleepingThread(this._ctrl);
    thread.start();
    Thread.sleep(2000);

    _LOG.debug("*** calling interrupt on the running thread ****") ;
    thread.interrupt();
    Thread.sleep(500); // Give the thread the time to check.
    _LOG.debug(String.format("*** state is: %s ***", this._ctrl.getState().getLiteral())) ;
    
    Thread.sleep(2000);
    
    _LOG.debug("*** calling cancelled on the interrupted thread ****") ;
    thread.cancel();
    _LOG.debug("*** it should not do anything ***") ;
    Thread.sleep(500); // Give the thread the time to check.
    _LOG.debug(String.format("*** state is: %s ***", this._ctrl.getState().getLiteral())) ;
    
    thread.join();
  }
  
  @Test
  @Order(8)
  void test8() throws InterruptedException
  {
    _LOG.info("######### TEST 8 #########") ;
    
    WaitingThread thread = new WaitingThread(this._ctrl, 5000);
    thread.start();
    
    _LOG.debug("*** just waiting for the thread ****") ;
    
    thread.join();
    _LOG.debug(String.format("*** state is: %s ***", this._ctrl.getState().getLiteral())) ;
  }

  @Test
  @Order(9)
  void test9() throws InterruptedException
  {
    _LOG.info("######### TEST 9 #########") ;
    
    WaitingThread thread = new WaitingThread(this._ctrl, 5000);
    thread.start();
    
    Thread.sleep(1000);
    _LOG.debug("*** calling cancel on the awaiting thread ****") ;
    thread.cancel();
    
    thread.join();
    _LOG.debug(String.format("*** state is: %s ***", this._ctrl.getState().getLiteral())) ;
  }
  
  @Test
  @Order(10)
  void test10() throws InterruptedException
  {
    _LOG.info("######### TEST 10 #########") ;
    
    WaitingThread thread = new WaitingThread(this._ctrl, 5000);
    thread.start();
    
    Thread.sleep(1000);
    _LOG.debug("*** calling pause on the awaiting thread ****") ;
    thread.pause();
    
    Thread.sleep(2000);
    _LOG.debug("*** calling unpause before deadline meeting ****") ;
    thread.unPause();
    
    thread.join();
    _LOG.debug(String.format("*** state is: %s ***", this._ctrl.getState().getLiteral())) ;
  }

  @Test
  @Order(11)
  void test11() throws InterruptedException
  {
    _LOG.info("######### TEST 11 #########") ;
    
    WaitingThread thread = new WaitingThread(this._ctrl, 5000);
    thread.start();
    
    Thread.sleep(1000);
    _LOG.debug("*** calling pause on the awaiting thread ****") ;
    thread.pause();
    
    Thread.sleep(5000);
    _LOG.debug("*** calling unpause after deadline meeting ****") ;
    thread.unPause();
    
    thread.join();
    _LOG.debug(String.format("*** state is: %s ***", this._ctrl.getState().getLiteral())) ;
  }
  
  @Test
  @Order(12)
  void test12() throws InterruptedException
  {
    _LOG.info("######### TEST 12 #########") ;
    
    SelfPausedThread thread = new SelfPausedThread(this._ctrl);
    thread.start();
    
    Thread.sleep(5000);
    _LOG.debug("*** calling unpause on the selfpaused thread ****") ;
    thread.unPause();
    
    thread.join();
    _LOG.debug(String.format("*** state is: %s ***", this._ctrl.getState().getLiteral())) ;
  }
}

class SelfPausedThread extends AbstractThreadControl
{
  private static final Logger _LOG = Log.HIGH_LEVEL;
  
  public SelfPausedThread(AbstractToolControl toolCtrl)
  {
    super(toolCtrl) ;
  }

  @Override
  protected void threadLogic() throws CancellationException,
                                      InitializationException,
                                      ConfigurationException,
                                      Exception
  {
    _LOG.debug("trigger self pause");
    this.selfTriggerPause();
  }
}

class SleepingThread extends AbstractThreadControl
{
  private static final Logger _LOG = Log.HIGH_LEVEL;
  
  public SleepingThread(AbstractToolControl toolCtrl)
  {
    super(toolCtrl) ;
  }

  @Override
  protected void threadLogic() throws CancellationException,
                                      InitializationException,
                                      ConfigurationException,
                                      Exception
  {
    for(int i = 0 ; i < 20 ; i++)
    {
      _LOG.debug(String.format("--- alive %s ---", i)) ;
      Thread.sleep(500);
      this.checkInterruption();
      this.checkCancel();
      this.checkPause();
    }
  }
}

class WaitingThread extends AbstractThreadControl
{
  private int _duration ; // In milliseconds.

  public WaitingThread(AbstractToolControl toolCtrl, int duration)
  {
    super(toolCtrl) ;
    this._duration = duration;
  }

  @Override
  protected void threadLogic() throws CancellationException,
                                      InitializationException,
                                      ConfigurationException,
                                      Exception
  {
    this.await(this._duration);
  }
}

class TestToolControlOperations extends AbstractToolControl
{
  private final Logger _LOG = Log.HIGH_LEVEL;
  
  private ToolState _state = new RunningState(this);
  
  public TestToolControlOperations() throws InitializationException, ConfigurationException
  {
    super(null, false, false, false);
  }
  
  @Override
  public void removeControlPanel(ControlPanel ctrlPanel) {} 
  
  @Override
  public Set<ControlPanel> getCtrlPanels()
  {
    return Collections.emptySet() ;
  }
  
  @Override
  public void addControlPanel(ControlPanel ctrlPanel) {}
  
  @Override
  public void notifyError(String msg, Throwable e) {}
  
  @Override
  public void notifyError(String msg) {}
  
  @Override
  public void notifyAction(Action action) {}
  
  @Override
  public void setState(ToolState newState)
  {
    _state = newState;
  }
  
  @Override
  public void resumeOperations()
  {
    _LOG.info("execute resume operations");
  }
  
  @Override
  public void reinitOperations(){}
  
  @Override
  public void pauseOperations()
  {
    _LOG.info("execute pause operations");
  }
  
  @Override
  public ToolState getState()
  {
    return _state;
  }
  
  @Override
  public void closeOperations(){}
  
  @Override
  public void cancelOperations()
  {
    _LOG.info("execute cancel operations");
  }
}
