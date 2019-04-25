package fr.gardoll.ace.controller.core;

import java.util.concurrent.locks.Condition ;
import java.util.concurrent.locks.ReentrantLock ;

public class ThreadSession extends Thread
{
  // Fairness is on so as to yield the lock.
  private final ReentrantLock _sync = new ReentrantLock(true);
  private final Condition _sync_cond = _sync.newCondition();
  
  // Shared ressources. Must take the lock so as to read & write them.
  private boolean _is_paused       = false;
  private boolean _is_synchronized = false;
  
  public ThreadSession()
  {
    // JVM will not wait until this thread ends.
    // Very convenient for an emergency stop.
    this.setDaemon(true);
  }
  
  // Block until the sequence is paused.
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
         
        // The main thread wait for the sequence to pause.
        // await must be called in a loop so as to prevent spurious wakeup.
        while(false == this._is_synchronized)
        {
          // Release lock and make
          // the main thread to wait until the sequence is paused.
          this._sync_cond.await(); 
        }
        
        // When the main thread returns from the method await,
        // it blocks until it retakes the lock.
        // So it must unlock it (in the finally bloc).
        
        // At this point, the sequence is paused, so the main thread can
        // access to the sampler and the pump: they wont' be synchronized anymore.
        this._is_synchronized = false;
      }
    }
    finally
    {
      this._sync.unlock();
    }
  }
  
  // Resume the sequence.
  public void unPause() throws InterruptedException
  {
    try
    {
      // Must take the lock so as to read the shared ressources and
      // signalAll on the condition.
      this._sync.lockInterruptibly();
      
      if(this._is_paused)
      {
        // Makes the sequence to quit it's await loop.
        this._is_paused = false;
        // Wake up the sequence.
        this._sync_cond.signalAll();
      }
    }
    finally
    {
      this._sync.unlock();
    }
  }
  
  protected void checkPause() throws InterruptedException
  {
    try
    { 
      // Must take the lock so as to read the shared ressources and
      // signall on the condition.
      this._sync.lockInterruptibly();
      
      if(this._is_paused)
      {
        // Makes the main thread to quit it's await loop.
        this._is_synchronized = true;
        // Wake up the main thread that was waiting the sequence to pause.
        this._sync_cond.signalAll();
        
        // await must be called in a loop so as to prevent spurious wakeup.
        while(this._is_paused)
        {
          // The sequence is waiting the main thread to wake it up.
          // the method await makes the sequence to release the lock. 
          this._sync_cond.await();
        }
        
        // When the sequence returns from the method await,
        // it blocks until it retakes the lock.
        // So it must unlock it (in the finally bloc).
      }
    }
    finally
    {
      this._sync.unlock();
    }
  }
  
  public void run()
  {
    
  }
}
