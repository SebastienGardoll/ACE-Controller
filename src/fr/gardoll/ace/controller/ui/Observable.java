package fr.gardoll.ace.controller.ui;

public interface Observable
{
  // Must not block and be time consuming.
  public void notifyAction(Action action);
  
  // May be blocking call.
  public void notifyError(String msg);
  public void notifyError(String msg, Throwable e);
  
  public void addObserver(Observer panel);
  
  public void removeObserver(Observer panel);
}
