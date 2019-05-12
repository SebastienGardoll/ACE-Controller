package fr.gardoll.ace.controller.core;

import java.util.Date ;
import java.util.concurrent.locks.Condition ;
import java.util.concurrent.locks.ReentrantLock ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.common.CancellationException ;

// TODO:
// - unblock threads that are wating the thread to pause or cancel, near the end.
// - report msg.
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
  
  public AbstractThreadControl()
  {
    // JVM will not wait until this thread ends.
    // Very convenient for an emergency stop.
    this.setDaemon(true);
  }
  
  // Block the caller until the thread is paused.
  @Override
  public void pause() throws InterruptedException
  {
    try
    {
      _LOG.debug("pausing the thread");
      
      // Must take the lock so as to read the shared ressources and
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
          this._sync_cond.await(); 
        }
        
        _LOG.debug("thread is paused");
        
        // When the caller returns from the method await,
        // it blocks until it retakes the lock.
        // That why it must unlock it (in the finally bloc).
        
        // At this point, the thread is paused, so the caller can
        // access to the sampler and the pump: they wont' be synchronized anymore.
        this._is_synchronized = false;
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
  
  // Resume the thread from another thread.
  @Override
  public void unPause() throws InterruptedException
  {
    try
    {
      _LOG.debug("executing unpause");
      
      // Must take the lock so as to read the shared ressources and
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
  // Make the instance of the AbstractThreadControl to pause if another thread
  // has called the pause method to do so.
  @Override
  public void checkPause() throws InterruptedException
  {
    try
    { 
      _LOG.debug("checking the pause");
      
      // Must take the lock so as to read the shared ressources and
      // signall on the condition.
      this._sync.lockInterruptibly();
      
      if(this._is_paused &&
         Thread.currentThread() == this)
      {
        // Makes the caller to quit its await loop.
        this._is_synchronized = true;
        
        _LOG.debug("signaling to all threads that are waiting this thread to pause");
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
        // it blocks until it retakes the lock.
        // That why it must unlock it (in the finally bloc).
        
        _LOG.debug("thread is resumed");
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
  
  //Check point for the thread.
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
      String msg = "thread has been interrupted, throws InterruptedException";
      _LOG.debug(msg);
      throw new InterruptedException(msg);
    }
    else
    {
      _LOG.debug("nothing to do");
    }
  }
  
  // Block the caller until the thread is canceled.
  @Override
  public void cancel() throws InterruptedException
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
          this._sync_cond.await(); 
        }
        
        _LOG.debug("thread is cancelled");
        
        // When the caller returns from the method await,
        // it blocks until it retakes the lock.
        // That why it must unlock it (in the finally bloc).
        
        // At this point, the thread is paused, so the caller can
        // access to the sampler and the pump: they wont' be synchronized anymore.
        this._is_synchronized = false;
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
        
        _LOG.debug("signaling to all threads that are waiting this thread to cancel");
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
      public void run()
      {
        for(int i = 0 ; i < 20 ; i++)
        {
          try
          {
            System.out.println(String.format("--- alive %s ---", i)) ;
            Thread.sleep(500);
            this.checkInterruption();
            this.checkCancel();
            this.checkPause();
          }
          catch (InterruptedException e)
          {
            String msg = String.format("%s interrupted", new Date());
            System.err.println(msg) ;
            return;
          }
          catch(CancellationException e)
          {
            String msg = String.format("%s cancelled", new Date());
            System.err.println(msg) ;
            return;
          }
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
