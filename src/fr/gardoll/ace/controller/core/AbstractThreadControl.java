package fr.gardoll.ace.controller.core;

import java.util.concurrent.locks.Condition ;
import java.util.concurrent.locks.ReentrantLock ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.common.CancellationException ;

// TODO:
// - reimplement cancel, same mechanism as pause
// - don't pause or cancel when thread is terminated.
// - report msg.
public abstract class AbstractThreadControl extends Thread
                                            implements ThreadControl
{
  private static final Logger _LOG = LogManager.getLogger(AbstractThreadControl.class.getName());
  
  // Fairness is on so as to yield the lock.
  private final ReentrantLock _sync = new ReentrantLock(true);
  private final Condition _sync_cond = _sync.newCondition();
  
  // Shared ressources. Must take the lock so as to read & write them.
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
         this.isAlive())
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
      
      if(this._is_paused &&
         false == this._is_canceled &&
         this.isAlive())
      {
        // Makes the thread to quit its await loop.
        this._is_paused = false;
        
        _LOG.debug("waking up the paused thread");
        
        // Wake up the thread.
        this._sync_cond.signalAll();
      }
    }
    finally
    {
      this._sync.unlock();
    }
  }
  
  // Check point for the thread.
  // Make the instance of the AbstractThreadControl to pause if another thread
  // call the pause method to do so.
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
         Thread.currentThread() instanceof AbstractThreadControl)
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
    if(Thread.currentThread().isInterrupted())
    {
      String msg = "thread has been interrupted, throws InterruptedException";
      _LOG.debug(msg);
      throw new InterruptedException(msg);
    }
  }
  
  @Override
  public void cancel()
  {
    // TODO: impl.
  }
  
  @Override
  public void checkCancel() throws CancellationException
  {
    // TODO: impl.
  }
  
  public static void main(String[] args)
  {
    AbstractThreadControl thread = new AbstractThreadControl()
    {
      @Override
      public void run()
      {
        for(int i = 0 ; i < 20 ; i++)
        {
          try
          {
            System.out.println(String.format("alive %s", i)) ;
            Thread.sleep(500);
            this.checkPause();
          }
          catch (InterruptedException e)
          {
            System.err.println("interrupted") ;
            return;
          }
        }
      }
    };
    
    thread.start();
    
    try
    {
      Thread.sleep(2000);
      
      System.out.println("*** calling pause on the thread object ****") ;
      thread.pause();
      System.out.println("*** thread is paused ****") ;
      
      System.out.println("*** calling check pause on the thread object ****") ;
      thread.checkPause();
      System.out.println("*** check pause passed ***") ;
      
      System.out.println("*** calling check interruption on the thread object ****") ;
      thread.checkInterruption();
      System.out.println("*** check interruption passed ***") ;
      
      boolean interruptWhileInPause = false;
      
      if(interruptWhileInPause)
      {
        Thread.sleep(2000);
        System.out.println("*** calling interrupt on the thread object ****") ;
        thread.interrupt();
        System.out.println("*** thread is interrupted ***") ;
      }
      else
      {
        System.out.println("*** unpause thread ***") ;
        thread.unPause();
        
        Thread.sleep(2000);
        
        System.out.println("*** calling interrupt on the thread object ****") ;
        thread.interrupt();
        System.out.println("*** thread is interrupted ***") ;
      }
      
      thread.join();
      System.out.println("*** end ***") ;
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }
}
