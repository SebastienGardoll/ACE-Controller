package fr.gardoll.ace.controller.core;

import java.util.concurrent.locks.Condition ;
import java.util.concurrent.locks.ReentrantLock ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

// TODO: add logging
public abstract class AbstractThreadControl extends Thread implements ThreadControl
{
  private static final Logger _LOG = LogManager.getLogger(AbstractThreadControl.class.getName());
  
  // Fairness is on so as to yield the lock.
  private final ReentrantLock _sync = new ReentrantLock(true);
  private final Condition _sync_cond = _sync.newCondition();
  
  // Shared ressources. Must take the lock so as to read & write them.
  private boolean _is_paused       = false;
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
      // Must take the lock so as to read the shared ressources and
      // await on the condition.
      this._sync.lockInterruptibly();
      if(false == this._is_paused)
      {
        this._is_paused = true;
         
        // The caller wait for the thread to pause.
        // The method await must be called in a loop so as to prevent spurious wakeup.
        while(false == this._is_synchronized)
        {
          // Release lock and make
          // the caller to wait until the thread is paused.
          this._sync_cond.await(); 
        }
        
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
  
  // Resume the thread.
  @Override
  public void unPause() throws InterruptedException
  {
    try
    {
      // Must take the lock so as to read the shared ressources and
      // signalAll on the condition.
      this._sync.lockInterruptibly();
      
      if(this._is_paused)
      {
        // Makes the thread to quit its await loop.
        this._is_paused = false;
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
  @Override
  public void checkPause() throws InterruptedException
  {
    try
    { 
      // Must take the lock so as to read the shared ressources and
      // signall on the condition.
      this._sync.lockInterruptibly();
      
      if(this._is_paused)
      {
        // Makes the caller to quit its await loop.
        this._is_synchronized = true;
        // Wake up the caller that was waiting the thread to pause.
        this._sync_cond.signalAll();
        
        // The method await must be called in a loop so as to prevent spurious wakeup.
        while(this._is_paused)
        {
          // The thread is waiting the caller to wake it up.
          // the method await makes the thread to release the lock. 
          this._sync_cond.await();
        }
        
        // When the thread returns from the method await,
        // it blocks until it retakes the lock.
        // That why it must unlock it (in the finally bloc).
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
    if(this.isInterrupted())
    {
      String msg = "thread has been interrupted, throws InterruptedException";
      _LOG.debug(msg);
      throw new InterruptedException(msg);
    }
  }
}
