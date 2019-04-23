package fr.gardoll.ace.controller.core;

public class OrganiseurThreadSequence extends Thread
{

  private boolean _isPaused ;

  public ThreadSequence adresseThreadSequence()
  {
    // TODO Auto-generated method stub
    return null ;
  }

  public void pause()
  {
    this._isPaused = true;
  }
  
  public void unPause()
  {
    this._isPaused = false;
  }

}
