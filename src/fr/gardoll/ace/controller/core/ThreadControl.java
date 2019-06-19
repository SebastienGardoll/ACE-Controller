package fr.gardoll.ace.controller.core;

public interface ThreadControl
{
  // Block the caller until the thread is paused.
  // The thread has to define check points that where it can pause.
  // Return false if the thread has terminated meanwhile. True otherwise.
  public boolean pause() throws InterruptedException;
  
  // Resume the thread.
  // Return false if the thread has terminated meanwhile. True otherwise.
  public boolean unPause() throws InterruptedException;
  
  // Block the caller until the thread is canceled.
  // The thread has to define check points that where it can cancel.
  // Return false if the thread has terminated meanwhile. True otherwise.
  public boolean cancel() throws InterruptedException;
  
  public void checkInterruption() throws InterruptedException ;
  
  public void checkCancel() throws CancellationException, InterruptedException;
  
  public void checkPause() throws InterruptedException;

  public boolean isAlive() ;
  
  public void start();
}
