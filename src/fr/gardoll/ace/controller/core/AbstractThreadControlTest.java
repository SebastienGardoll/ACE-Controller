package fr.gardoll.ace.controller.core;

import java.util.Collections ;
import java.util.Set ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;
import org.junit.jupiter.api.AfterEach ;
import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test ;

class AbstractThreadControlTest
{
  private TestToolControlOperations _ctrl = null;
  
  private static final Logger _LOG = LogManager.getLogger(AbstractThreadControlTest.class.getName());

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
  void test1() throws InterruptedException
  {
    System.out.println() ;
    System.out.println("######### TEST 1 #########") ;
    System.out.println() ;
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
  void test2() throws InterruptedException
  {
    System.out.println() ;
    System.out.println("######### TEST 2 #########") ;
    System.out.println() ;
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
  void test3() throws InterruptedException
  {
    System.out.println() ;
    System.out.println("######### TEST 3 #########") ;
    System.out.println() ;
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
  void test4() throws InterruptedException
  {
    System.out.println() ;
    System.out.println("######### TEST 4 #########") ;
    System.out.println() ;
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
  void test5() throws InterruptedException
  {
    System.out.println() ;
    System.out.println("######### TEST 5 #########") ;
    System.out.println() ;
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
  void test6() throws InterruptedException
  {
    System.out.println() ;
    System.out.println("######### TEST 6 #########") ;
    System.out.println() ;
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
  void test7() throws InterruptedException
  {
    System.out.println() ;
    System.out.println("######### TEST 7 #########") ;
    System.out.println() ;
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
  void test8() throws InterruptedException
  {
    System.out.println() ;
    System.out.println("######### TEST 8 #########") ;
    System.out.println() ;
    
    WaitingThread thread = new WaitingThread(this._ctrl, 5);
    thread.start();
    
    Thread.sleep(1000);
    _LOG.debug("*** calling cancel on the awaiting thread ****") ;
    thread.cancel();
    
    thread.join();
    _LOG.debug(String.format("*** state is: %s ***", this._ctrl.getState().getLiteral())) ;
  }
  
  @Test
  void test9() throws InterruptedException
  {
    System.out.println() ;
    System.out.println("######### TEST 9 #########") ;
    System.out.println() ;
    
    WaitingThread thread = new WaitingThread(this._ctrl, 5);
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
  void test10() throws InterruptedException
  {
    System.out.println() ;
    System.out.println("######### TEST 10 #########") ;
    System.out.println() ;
    
    WaitingThread thread = new WaitingThread(this._ctrl, 5);
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
  void test11() throws InterruptedException
  {
    System.out.println() ;
    System.out.println("######### TEST 11 #########") ;
    System.out.println() ;
    
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
  private static final Logger _LOG = LogManager.getLogger(SelfPausedThread.class.getName());
  
  public SelfPausedThread(AbstractToolControl toolCtrl)
  {
    super(toolCtrl) ;
  }

  @Override
  protected void threadLogic() throws InterruptedException,
                                      CancellationException,
                                      InitializationException,
                                      Exception
  {
    _LOG.debug("trigger self pause");
    this.selfTriggerPause();
  }
}

class SleepingThread extends AbstractThreadControl
{
  private static final Logger _LOG = LogManager.getLogger(SleepingThread.class.getName());
  
  public SleepingThread(AbstractToolControl toolCtrl)
  {
    super(toolCtrl) ;
  }

  @Override
  protected void threadLogic() throws InterruptedException,
                                      CancellationException,
                                      InitializationException,
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
  private int _seconds ;

  public WaitingThread(AbstractToolControl toolCtrl, int seconds)
  {
    super(toolCtrl) ;
    this._seconds = seconds;
  }

  @Override
  protected void threadLogic() throws InterruptedException,
                                      CancellationException,
                                      InitializationException,
                                      Exception
  {
    this.await(this._seconds);
  }
}

class TestToolControlOperations extends AbstractToolControl
{
  private final Logger _LOG = LogManager.getLogger(TestToolControlOperations.class.getName());
  
  private ToolState _state = new RunningState(this);
  
  public TestToolControlOperations() throws InitializationException,
                                            InterruptedException
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
  public void resumeOperations() throws InterruptedException
  {
    _LOG.info("execute resume operations");
  }
  
  @Override
  public void reinitOperations() throws InterruptedException {}
  
  @Override
  public void pauseOperations() throws InterruptedException
  {
    _LOG.info("execute pause operations");
  }
  
  @Override
  public ToolState getState()
  {
    return _state;
  }
  
  @Override
  public void closeOperations() throws InterruptedException {}
  
  @Override
  public void cancelOperations() throws InterruptedException
  {
    _LOG.info("execute cancel operations");
  }
}
