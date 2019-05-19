package fr.gardoll.ace.controller.core;

import fr.gardoll.ace.controller.common.CancellationException ;

public interface ThreadControl
{
  // Block the caller until the thread is paused.
  // The thread has to define check points that where it can pause. 
  public void pause() throws InterruptedException;
  
  // Resume the thread.
  public void unPause() throws InterruptedException;
  
  // Block the caller until the thread is canceled.
  // The thread has to define check points that where it can cancel.
  public void cancel() throws InterruptedException;
  
  public void checkInterruption() throws InterruptedException ;
  
  public void checkCancel() throws CancellationException, InterruptedException;
  
  public void checkPause() throws InterruptedException;

  public boolean isAlive() ;
}
