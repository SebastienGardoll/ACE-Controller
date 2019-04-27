package fr.gardoll.ace.controller.core;

public interface ThreadControl
{
  public void pause() throws InterruptedException;
  
  public void unPause() throws InterruptedException;
}
