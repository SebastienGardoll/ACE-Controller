package fr.gardoll.ace.controller.core;

public interface ThreadControl
{
  // Block the caller until the thread is paused.
  // The thread has to define check points that where it can pause. 
  public void pause() throws InterruptedException;
  
  // Resume the thread.
  public void unPause() throws InterruptedException;
}