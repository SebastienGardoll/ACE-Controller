package fr.gardoll.ace.controller.core;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

public interface ThreadControl
{
  // Ask to the thread to pause itself. Not blocking call.
  // The thread has to define check points that where it can pause.
  // Return false if the thread has terminated meanwhile. True otherwise.
  // Pause a canceled thread is not permitted.
  public boolean pause() throws InterruptedException;
  
  // Resume the thread. Not blocking call.
  // Return false if the thread has terminated meanwhile. True otherwise.
  public boolean unPause() throws InterruptedException;
  
  // Ask to the thread to cancel itself. Not blocking call.
  // The thread has to define check points that where it can cancel.
  // Return false if the thread has terminated meanwhile. True otherwise.
  // Cancel a paused thread is not permitted.
  public boolean cancel() throws InterruptedException;
  
  // Check point for interrupting the thread.
  // Make the instance of the ThreadControl to interrupt if another thread
  // call the interrupt method to do so.
  public void checkInterruption() throws InterruptedException ;
  
  // Check point for canceling the thread.
  // Make the instance of the ThreadControl to cancel if another thread
  // has called the cancel method to do so.
  public void checkCancel() throws CancellationException, InterruptedException;
  
  // Check point for pausing the thread. Blocking call.
  // Make the instance of the ThreadControl to pause if another thread
  // has called the pause method to do so.
  public void checkPause() throws InterruptedException;
  
  // Return true if the thread is running operations. False otherwise.
  // Blocking call.
  public boolean isRunning();
  
  // Start the operations of the thread. Not blocking call.
  public void start();
  
  public static void check() throws CancellationException
  {
    Thread thread = Thread.currentThread();
    
    if(thread instanceof ThreadControl)
    {
      ThreadControl threadCtrl = (ThreadControl) thread;
      try
      {
        threadCtrl.checkCancel();
        threadCtrl.checkPause();
      }
      catch(InterruptedException e)
      {
        Logger _LOG = LogManager.getLogger(ThreadControl.class.getName());
        _LOG.fatal(e);
        // Better throw a runtime exception so as to stop the execution of
        // the current thread.
        throw new RuntimeException(e);
      }
    }
  }
}
