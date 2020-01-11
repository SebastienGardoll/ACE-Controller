package fr.gardoll.ace.controller.core;

import java.time.Duration ;
import java.time.Instant ;
import java.util.concurrent.TimeUnit ;
import java.util.concurrent.locks.Condition ;
import java.util.concurrent.locks.ReentrantLock ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.settings.ConfigurationException ;

public abstract class AbstractThreadControl extends Thread
                                            implements ThreadControl
{
  private static final Logger _LOG = LogManager.getLogger(AbstractThreadControl.class.getName());
  
  // Fairness is on so as to yield the lock.
  private final ReentrantLock _sync = new ReentrantLock(true);
  private final Condition _sync_cond = _sync.newCondition();
  private final Condition _sync_await_cond = _sync.newCondition();
  
  // Flag that tells when the thread is waiting for an amount of time
  // (see await method).
  private boolean _is_awaiting = false;
  
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

  private boolean _canRerun = true;

  public AbstractThreadControl(AbstractToolControl toolCtrl)
  {
    init(toolCtrl, true);
  }
  
  public AbstractThreadControl(AbstractToolControl toolCtrl, boolean canRerun)
  {
    init(toolCtrl, canRerun);
  }
  
  private void init(AbstractToolControl toolCtrl, boolean canRerun)
  {
    // JVM will not wait until this thread ends.
    // Very convenient for an emergency stop.
    this.setDaemon(true);
    this._toolCtrl = toolCtrl;
    this._canRerun = canRerun;
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
      this._toolCtrl.getState().done(this._canRerun);
    }
    catch(CancellationException e)
    {
      try
      {
        this._toolCtrl.getState().cancelTransition(this._canRerun);
        // At the point, cancel operations have been completed.
        this._is_canceling  = false;
        this._has_to_cancel = false;
        return; // Terminate the execution of the thread.
      }
      catch (Exception e1)
      {
        String msg = "cancel operations have crashed";
        _LOG.fatal(msg, e1);
        this._toolCtrl.notifyError(msg, e1);
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
      else if(e instanceof ConfigurationException)
      {
        msg = "operations have been interrupted";
      }
      else if(e instanceof InterruptedException)
      {
        msg = "operations have been interrupted";
      }
      else if(e instanceof RuntimeException &&
              e.getCause() instanceof InterruptedException)
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
  
  protected abstract void threadLogic() throws CancellationException,
                                               InitializationException,
                                               ConfigurationException,
                                               Exception;

  @Override
  public boolean pause()
  {
    if(false == (Thread.currentThread() == this))
    {
      return this.askPause();
    }
    else
    {
      _LOG.debug("pause: nothing to do");
      return false;
    }
  }
  
  protected void selfTriggerPause()
  {
    if(Thread.currentThread() == this)
    {
      this.askPause();
      this.checkCancel();
      this.checkPause();
    }
    else
    {
      _LOG.debug("selfTriggerPause: nothing to do");
    }
  }
  
  private boolean askPause()
  {
    try
    {
      _LOG.debug("ask the thread to pause");
      
      // Must take the lock so as to read the shared resources.
      try
      {
        this._sync.lockInterruptibly();
      }
      catch (InterruptedException e)
      {
        throw new RuntimeException(e);
      }
      
      if(false == this._has_to_pause   &&
         false == this._has_to_cancel  &&
         this._is_running)
      {
        this._has_to_pause = true;
        
        this.handleAwaitingThread();

        // The caller may wake up a terminated thread.
        // So return the state of the thread to the caller so as to skip
        // any cancellation operations.
        return this._is_running;
      }
      else
      {
        _LOG.debug("askPause: nothing to do");
        return false;
      }
    }
    finally
    {
      this._sync.unlock();
    }
  }
  
  // _sync must be lock before calling this method !
  private void handleAwaitingThread()
  {
    // Wake up the thread if it is awaiting (see await method).
    this._is_awaiting = false;
    this._sync_await_cond.signalAll();
  }

  @Override
  public boolean unPause()
  {
    try
    {
      _LOG.debug("resuming the thread");
      
      // Must take the lock so as to read the shared resources and
      // signalAll on the condition.
      try
      {
        this._sync.lockInterruptibly();
      }
      catch (InterruptedException e)
      {
        throw new RuntimeException(e);
      }
      
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
  public void checkPause()
  {
    try
    { 
      //_LOG.debug("checking the pause");
      
      // Must take the lock so as to read the shared resources.
      try
      {
        this._sync.lockInterruptibly();
      }
      catch (InterruptedException e)
      {
        throw new RuntimeException(e);
      }
      
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
          try
          {
            this._sync_cond.await();
          }
          catch (InterruptedException e)
          {
            throw new RuntimeException(e);
          }
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
  
  public void await(long milliseconds)
  {
    final Instant deadline = Instant.now().plusMillis(milliseconds);
    _LOG.debug(String.format("wait until %s", deadline)) ;
    
    // Whatever the thread is pause, the thread must wait until the deadline.
    while(Instant.now().isBefore(deadline))
    {
      // Must take the lock so as to read the shared resources.
      try
      {
        this._sync.lockInterruptibly();
      }
      catch (InterruptedException e)
      {
        throw new RuntimeException(e);
      }
      
      try
      {
        // Shared resource.
        // Initialize the wait flag.
        // It can be set to false by pause and cancel methods so as the thread
        // to react to the pause/cancel triggers.
        this._is_awaiting = true;
        
        // The method await must be called in a loop so as to prevent
        // spurious wake up.
        while(this._is_awaiting)
        {
          // Now must be computed each time the thread is waked up.
          Instant now = Instant.now(); 
          
          if(now.isAfter(deadline))
          {
            this._is_awaiting = false;
            break; // Leave the inner while but not the outer most while.
          }
          
          // +1 because between definition.
          long timeToWait = Duration.between(now,deadline).toMillis() + 1;
          
          _LOG.debug(String.format("waiting %s ms", timeToWait));
          
          // The thread is waiting nanosTimeout of time but it can be wake up
          // by pause/cancel triggers.
          // The method await makes the thread to release the lock. 
          try
          {
            this._sync_await_cond.await(timeToWait, TimeUnit.MILLISECONDS);
          }
          catch (InterruptedException e)
          {
            throw new RuntimeException(e);
          }
        }
      }
      finally
      {
        this._sync.unlock();
      }
      
      // Cancel all operations, including the await method.
      this.checkCancel(); 
      // Let the thread enters into the pause state but the thread still have
      // to wait until the given deadline, even if it is resumed before the
      // deadline.
      this.checkPause();
    }
    
    _LOG.debug("wait done");
  }
  
  @Override
  public void checkInterruption() throws InterruptedException
  {
    //_LOG.debug("checking interruption");
    
    // Don't call this.isInterrupted(), as this method takes the state of this
    // object that represents a thread rather than taking the
    // context of the current running thread.
    if(Thread.currentThread() == this && 
       Thread.currentThread().isInterrupted())
    {
      String msg = "the thread has been interrupted,";
      _LOG.debug(msg);
      throw new InterruptedException(msg);
    }
    else
    {
      //_LOG.debug("nothing to do");
    }
  }
  
  @Override
  public boolean cancel()
  {
    try
    {
      _LOG.debug("ask the thread to cancel");
      
      // Must take the lock so as to read the shared resources.
      try
      {
        this._sync.lockInterruptibly();
      }
      catch (InterruptedException e)
      {
        throw new RuntimeException(e);
      }
      
      if(false == this._has_to_pause  &&
         false == this._has_to_cancel &&
         this._is_running             &&
         false == (Thread.currentThread() == this))
      {
        this._has_to_cancel = true;
        
        this.handleAwaitingThread();
        
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
  public void checkCancel() throws CancellationException
  {
    try
    { 
      // Must take the lock so as to read the shared resources.
      try
      {
        this._sync.lockInterruptibly();
      }
      catch (InterruptedException e)
      {
        throw new RuntimeException(e);
      }
      
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
}
