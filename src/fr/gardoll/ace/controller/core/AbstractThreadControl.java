package fr.gardoll.ace.controller.core;

import java.util.Collections ;
import java.util.Set ;
import java.util.concurrent.locks.Condition ;
import java.util.concurrent.locks.ReentrantLock ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

public abstract class AbstractThreadControl extends Thread
                                            implements ThreadControl
{
  private static final Logger _LOG = LogManager.getLogger(AbstractThreadControl.class.getName());
  
  // Fairness is on so as to yield the lock.
  private final ReentrantLock _sync = new ReentrantLock(true);
  private final Condition _sync_cond = _sync.newCondition();
  
  // Shared resources. Must take the lock so as to read & write them.
  private boolean _has_to_pause    = false;
  // Prevent recursive pause requests. Let the pause operations be executed.
  private boolean _is_pausing      = false;
  private boolean _has_to_cancel   = false;
  // Prevent recursive cancellation requests. Let the cancel operations be executed.
  private boolean _is_canceling    = false;
  // Thread.isAlive method is not quite clear for me. I prefer to handle the
  // liveliness of the thread.
  private boolean _is_running      = false;

  protected AbstractToolControl _toolCtrl ;

  public AbstractThreadControl(AbstractToolControl toolCtrl)
  {
    // JVM will not wait until this thread ends.
    // Very convenient for an emergency stop.
    this.setDaemon(true);
    this._toolCtrl = toolCtrl;
  }
  
  @Override
  public boolean isRunning()
  {
    this._sync.lock();
    boolean result = this._is_running;
    this._sync.unlock();
    return result;
  }
  
  private void setRunning(boolean isRunning)
  {
    this._sync.lock();
    this._is_running = isRunning;
    this._sync.unlock();
  }
  
  // Make the run method impossible to override as the thread must
  // perform some last computation before ending.
  // The only wait to give logic to this thread is to implement the threadLogic
  // method.
  @Override
  public final void run()
  {
    try
    {
      this.setRunning(true);
      this.threadLogic();
      this._toolCtrl.getState().done();
    }
    catch(CancellationException e)
    {
      try
      {
        this._toolCtrl.getState().cancelTransition();
        // At the point, cancel operations have been completed.
        this._is_canceling  = false;
        this._has_to_cancel = false;
        return; // Terminate the execution of the thread.
      }
      catch (Exception e1)
      {
        String msg = "cancel operations have crashed";
        _LOG.fatal(msg, e);
        this._toolCtrl.notifyError(msg, e);
        this._toolCtrl.getState().crash();
        return ; // Terminate the execution of the thread.
      }
    }
    catch(Exception e)
    {
      String msg = null;
      
      if(e instanceof InitializationException)
      {
        msg = "initialization has crashed";
      }
      else if(e instanceof InterruptedException)
      {
        msg = "operations have been interrupted";
      }
      else
      {
        msg = "operations have crashed";
      }
      
      _LOG.fatal(msg, e);
      this._toolCtrl.notifyError(msg, e);
      this._toolCtrl.getState().crash();
      return ; // Terminate the execution of the thread.
    }
    finally
    {
      this.setRunning(false);
    }
  }
  
  protected abstract void threadLogic() throws InterruptedException,
                                               CancellationException,
                                               InitializationException,
                                               Exception;

  @Override
  public boolean pause() throws InterruptedException
  {
    try
    {
      _LOG.debug("ask the thread to pause");
      
      // Must take the lock so as to read the shared resources.
      this._sync.lockInterruptibly();
      if(false == this._has_to_pause   &&
         false == this._has_to_cancel &&
         this._is_running           &&
         false == (Thread.currentThread() == this))
      {
        this._has_to_pause = true;

        // The caller may wake up a terminated thread.
        // So return the state of the thread to the caller so as to skip
        // any cancellation operations.
        return this._is_running;
      }
      else
      {
        _LOG.debug("nothing to do");
        return false;
      }
    }
    finally
    {
      this._sync.unlock();
    }
  }
  
  @Override
  public boolean unPause() throws InterruptedException
  {
    try
    {
      _LOG.debug("resuming the thread");
      
      // Must take the lock so as to read the shared resources and
      // signalAll on the condition.
      this._sync.lockInterruptibly();
      
      if(this._has_to_pause           &&
         false == this._has_to_cancel &&
         this._is_running             &&
         false == (Thread.currentThread() == this))
      {
        // Makes the thread to quit its await loop.
        this._has_to_pause = false;
        
        _LOG.debug("waking up the paused thread");
        
        // Wake up the thread.
        this._sync_cond.signalAll();
        return this._is_running;
      }
      else
      {
        _LOG.debug("nothing to do");
        return false;
      }
    }
    finally
    {
      this._sync.unlock();
    }
  }
  
  // Check point for the thread.
  // Make the instance of the AbstractThreadControl to pause if another thread
  // has called the pause method to do so.
  @Override
  public void checkPause() throws InterruptedException
  {
    try
    { 
      //_LOG.debug("checking the pause");
      
      // Must take the lock so as to read the shared resources.
      this._sync.lockInterruptibly();
      
      if(this._is_pausing == false &&
         this._has_to_pause        &&
         Thread.currentThread() == this)
      {
        this._toolCtrl.getState().pauseTransition();
        
        // At the point, pause operations have been completed.
        this._is_pausing = false;
        
        // The method await must be called in a loop so as to prevent spurious wakeup.
        _LOG.debug("the thead is paused");
        while(this._has_to_pause)
        {
          // The thread is waiting the caller to wake it up.
          // The method await makes the thread to release the lock. 
          this._sync_cond.await();
        }
        
        // When the thread returns from the method await,
        // it blocks until it re-takes the lock.
        // That why it must unlock it (in the finally bloc).
        _LOG.debug("the thread is resumed");
        
        this._toolCtrl.getState().resumeTransition();
      }
      else
      {
        //_LOG.debug("nothing to do");
      }
    }
    finally
    {
      this._sync.unlock();
    }
  }
  
  @Override
  public void checkInterruption() throws InterruptedException
  {
    _LOG.debug("checking interruption");
    
    // Don't call this.isInterrupted(), as this method takes the state of this
    // object that represents a thread rather than taking the
    // context of the current running thread.
    if(Thread.currentThread() == this && 
       Thread.currentThread().isInterrupted())
    {
      String msg = "the thread has been interrupted, throws InterruptedException";
      _LOG.debug(msg);
      throw new InterruptedException(msg);
    }
    else
    {
      _LOG.debug("nothing to do");
    }
  }
  
  @Override
  public boolean cancel() throws InterruptedException
  {
    try
    {
      _LOG.debug("ask the thread to cancel");
      
      // Must take the lock so as to read the shared resources.
      this._sync.lockInterruptibly();
      if(false == this._has_to_pause  &&
         false == this._has_to_cancel &&
         this._is_running             &&
         false == (Thread.currentThread() == this))
      {
        this._has_to_cancel = true;
         
        // The caller may wake up as the thread is terminated.
        // So return the state of the thread to the caller so as to skip
        // any cancellation operations.
        return this._is_running;
      }
      else
      {
        _LOG.debug("nothing to do");
        return false;
      }
    }
    finally
    {
      this._sync.unlock();
    }
  }
  
  @Override
  public void checkCancel() throws CancellationException, InterruptedException
  {
    try
    { 
      // Must take the lock so as to read the shared resources.
      this._sync.lockInterruptibly();
      
      if(this._is_canceling == false &&
         this._has_to_cancel         &&
         Thread.currentThread() == this)
      {
        this._is_canceling = true;
        _LOG.debug("cancelling the thread");
        throw new CancellationException();
      }
      else
      {
        //_LOG.debug("nothing to do");
      }
    }
    finally
    {
      this._sync.unlock();
    }
  }
  
  public static void main(String[] args)
  {
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
    };
    
    class TestAbstractThreadControl extends AbstractThreadControl
    {
      public TestAbstractThreadControl(AbstractToolControl toolCtrl)
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
          System.out.println(String.format("--- alive %s ---", i)) ;
          Thread.sleep(500);
          this.checkInterruption();
          this.checkCancel();
          this.checkPause();
        }
      }
    }
    
    try
    {
      for(int test = 0 ; test < 7 ; test++)
      {
        System.out.println() ;
        System.out.println(String.format("######### TEST %s #########", test)) ;
        System.out.println() ;
        
        TestToolControlOperations ctrlOp = new TestToolControlOperations();
        TestAbstractThreadControl thread = new TestAbstractThreadControl(ctrlOp);
        thread.start();
        
        switch(test)
        {
          case 0:
          {
            Thread.sleep(2000);
            
            System.out.println("*** calling pause on the running thread ****") ;
            thread.pause();
            
            Thread.sleep(500); // Give the thread the time to check.
            System.out.println(String.format("*** state is: %s ***", ctrlOp.getState().getLiteral())) ;
            
            System.out.println("*** calling pause on the thread, again ****") ;
            thread.pause();
            System.out.println("*** it should not do anything ****") ;
            
            
            System.out.println("*** calling check pause from main thread ****") ;
            thread.checkPause();
            System.out.println("*** main thread unchanged ***") ;
            
            System.out.println("*** calling check interruption from main thread ****") ;
            thread.checkInterruption();
            System.out.println("*** main thread unchanged ***") ;
            
            System.out.println("*** calling check cancel from main thread ****") ;
            thread.checkCancel();
            System.out.println("*** main thread unchanged ***") ;
            
            System.out.println("*** unpause thread ***") ;
            thread.unPause();
            
            Thread.sleep(2000);
            
            System.out.println(String.format("*** state is: %s ***", ctrlOp.getState().getLiteral())) ;
            System.out.println("*** calling interrupt on the running thread ****") ;
            thread.interrupt();
            Thread.sleep(500); // Give the thread the time to check.
            System.out.println(String.format("*** state is: %s ***", ctrlOp.getState().getLiteral())) ;
            
            break;
          }
          
          case 1:
          {
            Thread.sleep(2000);
            
            System.out.println("*** calling pause on the running thread ****") ;
            thread.pause();
            Thread.sleep(500); // Give the thread the time to check.
            System.out.println(String.format("*** state is: %s ***", ctrlOp.getState().getLiteral())) ;
            
            Thread.sleep(2000);
            System.out.println("*** calling interrupt on the paused thread ****") ;
            thread.interrupt();
            Thread.sleep(500); // Give the thread the process interruption.
            System.out.println(String.format("*** state is: %s ***", ctrlOp.getState().getLiteral())) ;
            
            break;
          }
          
          case 2:
          {
            Thread.sleep(2000);
            
            System.out.println("*** calling cancel on the running thread ****") ;
            thread.cancel();
            Thread.sleep(500); // Give the thread the time to check.
            System.out.println(String.format("*** state is: %s ***", ctrlOp.getState().getLiteral())) ;
            
            Thread.sleep(2000);
            
            System.out.println("*** calling cancel (again) on the cancelled thread ****") ;
            thread.cancel();
            System.out.println("*** it should not do anything ***") ;
            System.out.println(String.format("*** state is: %s ***", ctrlOp.getState().getLiteral())) ;
            
            break;
          }
          
          case 3:
          {
            Thread.sleep(2000);
            
            System.out.println("*** calling pause on the running thread ****") ;
            thread.pause();
            Thread.sleep(500); // Give the thread the time to check.
            System.out.println(String.format("*** state is: %s ***", ctrlOp.getState().getLiteral())) ;
            
            Thread.sleep(2000);
            System.out.println("*** calling cancel on the paused thread ****") ;
            thread.cancel();
            System.out.println("*** thread should not be cancelled ***") ;
            System.out.println(String.format("*** state is: %s ***", ctrlOp.getState().getLiteral())) ;
            
            Thread.sleep(2000);
            System.out.println("*** calling interrupt on the paused thread ****") ;
            thread.interrupt();
            Thread.sleep(500); // Give the thread the process interruption.
            System.out.println(String.format("*** state is: %s ***", ctrlOp.getState().getLiteral())) ;
            
            break;
          }
          
          case 4:
          {
            Thread.sleep(2000);
            
            System.out.println("*** calling cancel on the running thread ****") ;
            thread.cancel();
            Thread.sleep(500); // Give the thread the time to check.
            System.out.println(String.format("*** state is: %s ***", ctrlOp.getState().getLiteral())) ;
            
            Thread.sleep(2000);
            
            System.out.println("*** calling pause on the cancelled thread ****") ;
            thread.pause();
            System.out.println("*** it should not do anything ****") ;
            Thread.sleep(500); // Give the thread the time to check.
            System.out.println(String.format("*** state is: %s ***", ctrlOp.getState().getLiteral())) ;
            
            break;
          }
          
          case 5:
          {
            Thread.sleep(2000);
            
            System.out.println("*** calling interrupt on the running thread ****") ;
            thread.interrupt();
            Thread.sleep(500); // Give the thread the time to check.
            System.out.println(String.format("*** state is: %s ***", ctrlOp.getState().getLiteral())) ;
            
            Thread.sleep(2000);
            
            System.out.println("*** calling pause on the interrupted thread ****") ;
            thread.pause();
            System.out.println("*** it should not do anything ***") ;
            Thread.sleep(500); // Give the thread the time to check.
            System.out.println(String.format("*** state is: %s ***", ctrlOp.getState().getLiteral())) ;
            
            break;
          }
          
          case 6:
          {
            Thread.sleep(2000);
            
            System.out.println("*** calling interrupt on the running thread ****") ;
            thread.interrupt();
            Thread.sleep(500); // Give the thread the time to check.
            System.out.println(String.format("*** state is: %s ***", ctrlOp.getState().getLiteral())) ;
            
            Thread.sleep(2000);
            
            System.out.println("*** calling cancelled on the interrupted thread ****") ;
            thread.cancel();
            System.out.println("*** it should not do anything ***") ;
            Thread.sleep(500); // Give the thread the time to check.
            System.out.println(String.format("*** state is: %s ***", ctrlOp.getState().getLiteral())) ;
            
            break;
          }
        }
        
        thread.join();
        System.out.println(String.format("*** end of test %d ***", test)) ;
      }
    }
    catch (Exception e)
    {
      e.printStackTrace() ;
    }
  }
}
