package fr.gardoll.ace.controller.core;

public interface ThreadControl
{
  // Ask to the thread to pause itself. Not blocking.
  // The thread has to define check points that where it can pause.
  // Return false if the thread has terminated meanwhile. True otherwise.
  public boolean pause() throws InterruptedException;
  
  // Resume the thread.
  // Return false if the thread has terminated meanwhile. True otherwise.
  public boolean unPause() throws InterruptedException;
  
  // Ask to the thread to cancel itself. Not blocking.
  // The thread has to define check points that where it can cancel.
  // Return false if the thread has terminated meanwhile. True otherwise.
  public boolean cancel() throws InterruptedException;
  
  // Check point for the thread.
  // Make the instance of the AbstractThreadControl to interrupt if another thread
  // call the interrupt method to do so.
  public void checkInterruption() throws InterruptedException ;
  
  // Check point for the thread.
  // Make the instance of the AbstractThreadControl to cancel if another thread
  // has called the cancel method to do so.
  public void checkCancel() throws CancellationException, InterruptedException;
  
  // Check point for the thread.
  // Make the instance of the AbstractThreadControl to pause if another thread
  // has called the pause method to do so.
  public void checkPause() throws InterruptedException;
  
  // Return true if the thread is running operations. False otherwise.
  // Blocking call.
  public boolean isRunning();
  
  // Start the operations of the thread.
  public void start();
}
