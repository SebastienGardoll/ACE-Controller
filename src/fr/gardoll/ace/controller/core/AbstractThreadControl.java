package fr.gardoll.ace.controller.core;

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
  private boolean _is_paused       = false;
  private boolean _is_canceled     = false;
  private boolean _is_synchronized = false;

  protected AbstractToolControl _toolCtrl ;

  public AbstractThreadControl()
  {
    this.init(null);
  }
  
  private void init(AbstractToolControl toolCtrl)
  {
    // JVM will not wait until this thread ends.
    // Very convenient for an emergency stop.
    this.setDaemon(true);
    this._toolCtrl = toolCtrl;
  }

  public AbstractThreadControl(AbstractToolControl toolCtrl)
  {
    this.init(toolCtrl);
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
      this.threadLogic();
      if(this._toolCtrl != null) {this._toolCtrl.getState().done();}
    }
    catch(InterruptedException e)
    {
      String msg = "operations have been interrupted";
      _LOG.fatal(msg);
      this.interrupt(); // Reset the interruption state of this thread.
      if(this._toolCtrl != null) {this._toolCtrl.getState().crash();}
      return ; // Terminate the execution of the thread.
    }
    catch(CancellationException e)
    {
      _LOG.info("operations have been canceled");
      if(this._toolCtrl != null)
      {
        this._toolCtrl.notifyAction(new Action(ActionType.CANCEL, null));
      }
      
      return; // Terminate the execution of the thread.
    }
    catch(InitializationException e)
    {
      String msg = String.format("initialization has crashed: %s", e);
      _LOG.fatal(msg, e);
      if(this._toolCtrl != null)
      {
        this._toolCtrl.notifyError(msg, e);
      }
      
      if(this._toolCtrl != null) {this._toolCtrl.getState().crash();}
      return ; // Terminate the execution of the thread.
    }
    catch(Exception e)
    {
      String msg = String.format("operations have crashed: %s", e);
      _LOG.fatal(msg, e);
      if(this._toolCtrl != null)
      {
        this._toolCtrl.notifyError(msg, e);
      }
      
      if(this._toolCtrl != null) {this._toolCtrl.getState().crash();}
      return ; // Terminate the execution of the thread.
    }
    finally
    {
      // Someone may call cancel or pause at the end of the execution of the
      // thread and the thread may not check pause or cancel so the caller
      // will wait forever. Theses instructions release the pending callers.
      this._sync.lock();
      // Makes the caller to quit its await loop.
      this._is_synchronized = true;
      _LOG.debug("signaling to all callers that are waiting this thread to cancel or pause");
      // Wake up the caller that was waiting the thread to pause or cancel.
      this._sync_cond.signalAll();
      this._sync.unlock();
    }
  }
  
  protected abstract void threadLogic() throws InterruptedException,
                                               CancellationException,
                                               InitializationException,
                                               Exception;

  // Block the caller until the thread is paused.
  // Return false if the thread has terminated meanwhile. True otherwise.
  @Override
  public boolean pause() throws InterruptedException
  {
    try
    {
      _LOG.debug("pausing the thread");
      
      // Must take the lock so as to read the shared resources and
      // await on the condition.
      this._sync.lockInterruptibly();
      if(false == this._is_paused   &&
         false == this._is_canceled &&
         this.isAlive()             &&
         false == (Thread.currentThread() == this))
      {
        this._is_paused = true;
         
        _LOG.debug("waiting until the thread is paused");
        
        // The caller wait for the thread to pause.
        // The method await must be called in a loop so as to prevent spurious wakeup.
        while(false == this._is_synchronized)
        {
          // Release lock and make
          // the caller to wait until the thread is paused.
          // Note:
          // - the thread cannot terminate when the lock is taken. At the end,
          // the caller cannot hang around for a terminated thread.
          // See the finally block of the run method.
          // - the caller may wake up but the thread is terminated.
          this._sync_cond.await(); 
        }
        
        _LOG.debug("caller is synchronized");
        
        // When the caller returns from the method await,
        // it blocks until it re-takes the lock.
        // That why it must unlock it (in the finally bloc).
        
        // At this point, the thread is paused, so the caller can
        // access to the sampler and the pump: they wont' be synchronized anymore.
        this._is_synchronized = false;
        
        // The thread before terminating, signals all the hanging callers.
        // So the caller may wake up as the thread is terminated.
        // So return the state of the thread to the caller so as to skip
        // any cancellation operations.
        return this.isAlive();
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
  
  // Resume the thread from another thread.
  // Return false if the thread has terminated meanwhile. True otherwise.
  @Override
  public boolean unPause() throws InterruptedException
  {
    try
    {
      _LOG.debug("executing unpause");
      
      // Must take the lock so as to read the shared resources and
      // signalAll on the condition.
      this._sync.lockInterruptibly();
      
      if(this._is_paused            &&
         false == this._is_canceled &&
         this.isAlive()             &&
         false == (Thread.currentThread() == this))
      {
        // Makes the thread to quit its await loop.
        this._is_paused = false;
        
        _LOG.debug("waking up the paused thread");
        
        // Wake up the thread.
        this._sync_cond.signalAll();
        return this.isAlive();
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
      _LOG.debug("checking the pause");
      
      // Must take the lock so as to read the shared resources and
      // signall on the condition.
      this._sync.lockInterruptibly();
      
      if(this._is_paused &&
         Thread.currentThread() == this)
      {
        // Makes the caller to quit its await loop.
        this._is_synchronized = true;
        
        _LOG.debug("signaling to all callers that are waiting this thread to pause");
        // Wake up the caller that was waiting the thread to pause.
        this._sync_cond.signalAll();
        
        // The method await must be called in a loop so as to prevent spurious wakeup.
        _LOG.debug("begining to pause");
        while(this._is_paused)
        {
          // The thread is waiting the caller to wake it up.
          // the method await makes the thread to release the lock. 
          this._sync_cond.await();
        }
        
        // When the thread returns from the method await,
        // it blocks until it re-takes the lock.
        // That why it must unlock it (in the finally bloc).
        
        _LOG.debug("the thread is resumed");
      }
      else
      {
        _LOG.debug("nothing to do");
      }
    }
    finally
    {
      this._sync.unlock();
    }
  }
  
  // Check point for the thread.
  // Make the instance of the AbstractThreadControl to interrupt if another thread
  // call the interrupt method to do so.
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
  
  // Block the caller until the thread is canceled.
  // Return false if the thread has terminated meanwhile. True otherwise.
  @Override
  public boolean cancel() throws InterruptedException
  {
    try
    {
      _LOG.debug("cancelling the thread");
      
      // Must take the lock so as to read the shared resources and
      // await on the condition.
      this._sync.lockInterruptibly();
      if(false == this._is_paused   &&
         false == this._is_canceled &&
         this.isAlive()             &&
         false == (Thread.currentThread() == this))
      {
        this._is_canceled = true;
         
        _LOG.debug("waiting until the thread is cancelled");
        
        // The caller wait for the thread to pause.
        // The method await must be called in a loop so as to prevent spurious wakeup.
        while(false == this._is_synchronized)
        {
          // Release lock and make
          // the caller to wait until the thread is paused.
          // Note:
          // - the thread cannot terminate when the lock is taken. At the end,
          // the caller cannot hang around for a terminated thread.
          // See the finally block of the run method.
          // - the caller may wake up but the thread is terminated.
          this._sync_cond.await(); 
        }
        
        _LOG.debug("caller is synchronized");
        
        // When the caller returns from the method await,
        // it blocks until it re-takes the lock.
        // That why it must unlock it (in the finally bloc).
        
        // At this point, the thread is paused, so the caller can
        // access to the sampler and the pump: they wont' be synchronized anymore.
        this._is_synchronized = false;
        
        // The thread before terminating, signals all the hanging callers.
        // So the caller may wake up as the thread is terminated.
        // So return the state of the thread to the caller so as to skip
        // any cancellation operations.
        return this.isAlive();
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
  // Make the instance of the AbstractThreadControl to cancel if another thread
  // has called the cancel method to do so.
  @Override
  public void checkCancel() throws CancellationException, InterruptedException
  {
    try
    { 
      _LOG.debug("checking the cancellation");
      
      // Must take the lock so as to read the shared resources and
      // signall on the condition.
      this._sync.lockInterruptibly();
      
      if(this._is_canceled &&
         Thread.currentThread() == this)
      {
        // Makes the caller to quit its await loop.
        this._is_synchronized = true;
        
        _LOG.debug("signaling to all callers that are waiting this thread to cancel");
        // Wake up the caller that was waiting the thread to pause.
        this._sync_cond.signalAll();
        
        _LOG.debug("throwing CancellationException");
        throw new CancellationException();
      }
      else
      {
        _LOG.debug("nothing to do");
      }
    }
    finally
    {
      this._sync.unlock();
    }
  }
  
  public static void main(String[] args)
  {
    class TestAbstractThreadControl extends AbstractThreadControl
    {
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
        
        TestAbstractThreadControl thread = new TestAbstractThreadControl();
        thread.start();
        
        switch(test)
        {
          case 0:
          {
            Thread.sleep(2000);
            
            System.out.println("*** calling pause on the running thread ****") ;
            thread.pause();
            System.out.println("*** thread is paused ****") ;
            
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
            
            System.out.println("*** calling interrupt on the running thread ****") ;
            thread.interrupt();
            System.out.println("*** thread is interrupted ***") ;
            break;
          }
          
          case 1:
          {
            Thread.sleep(2000);
            
            System.out.println("*** calling pause on the running thread ****") ;
            thread.pause();
            System.out.println("*** thread is paused ****") ;
            
            Thread.sleep(2000);
            System.out.println("*** calling interrupt on the paused thread ****") ;
            thread.interrupt();
            System.out.println("*** thread is interrupted ***") ;
            
            break;
          }
          
          case 2:
          {
            Thread.sleep(2000);
            
            System.out.println("*** calling cancel on the running thread ****") ;
            thread.cancel();
            System.out.println("*** thread is cancelled ***") ;
            
            Thread.sleep(2000);
            
            System.out.println("*** calling cancel (again) on the cancelled thread ****") ;
            thread.cancel();
            System.out.println("*** it should not do anything ***") ;
            
            break;
          }
          
          case 3:
          {
            Thread.sleep(2000);
            
            System.out.println("*** calling pause on the running thread ****") ;
            thread.pause();
            System.out.println("*** thread is paused ****") ;
            
            Thread.sleep(2000);
            System.out.println("*** calling cancel on the paused thread ****") ;
            thread.cancel();
            System.out.println("*** thread should not be cancelled ***") ;
            
            Thread.sleep(2000);
            System.out.println("*** calling interrupt on the paused thread ****") ;
            thread.interrupt();
            System.out.println("*** thread is interrupted ***") ;
            
            break;
          }
          
          case 4:
          {
            Thread.sleep(2000);
            
            System.out.println("*** calling cancel on the running thread ****") ;
            thread.cancel();
            System.out.println("*** thread is cancelled ***") ;
            
            Thread.sleep(2000);
            
            System.out.println("*** calling pause on the cancelled thread ****") ;
            thread.pause();
            System.out.println("*** it should not do anything ****") ;
            
            break;
          }
          
          case 5:
          {
            Thread.sleep(2000);
            
            System.out.println("*** calling interrupt on the running thread ****") ;
            thread.interrupt();
            System.out.println("*** thread is interrupted ***") ;
            
            Thread.sleep(2000);
            
            System.out.println("*** calling pause on the interrupted thread ****") ;
            thread.pause();
            System.out.println("*** it should not do anything ***") ;
            break;
          }
          
          case 6:
          {
            Thread.sleep(2000);
            
            System.out.println("*** calling interrupt on the running thread ****") ;
            thread.interrupt();
            System.out.println("*** thread is interrupted ***") ;
            
            Thread.sleep(2000);
            
            System.out.println("*** calling cancelled on the interrupted thread ****") ;
            thread.cancel();
            System.out.println("*** it should not do anything ***") ;
            break;
          }
        }
        
        thread.join();
        System.out.println(String.format("*** end of test %d ***", test)) ;
      }
    }
    catch (InterruptedException e)
    {
      e.printStackTrace() ;
    }
  }
}
