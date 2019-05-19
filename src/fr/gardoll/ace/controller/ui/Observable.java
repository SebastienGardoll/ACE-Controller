package fr.gardoll.ace.controller.ui;

public interface Observable
{
  public void notifyAction(Action action);
  
  public void notifyError(String msg);
  public void notifyError(String msg, Throwable e);
  
  public void addObserver(Observer panel);
  
  public void removeObserver(Observer panel);
}
