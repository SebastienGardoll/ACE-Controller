package fr.gardoll.ace.controller.core;

public class ThreadSequence extends Thread
{
  private boolean _isPaused ;

  public void pause()
  {
    this._isPaused = true;
  }
  
  public void unPause()
  {
    this._isPaused = false;
  }
}
