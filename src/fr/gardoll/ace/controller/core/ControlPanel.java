package fr.gardoll.ace.controller.core;

public interface ControlPanel
{
  // Must not block and be time consuming.
  public void majActionActuelle(Action action) ;
  
  // May be blocking call.
  // The throwable parameter can be null.
  public void reportError(String msg, Throwable e);
  public void reportError(String msg);
  
  public void displayModalMessage(String msg);
  
  public boolean close();

  public void dispose() ;
  
  public void enableStart(boolean isEnable);
  public void enableClose(boolean isEnable);
  public void enablePause(boolean isEnable);
  public void enableResume(boolean isEnable);
  public void enableCancel(boolean isEnable);
  public void enableReinit(boolean isEnable);
}
