package fr.gardoll.ace.controller.ui;

public interface Observer
{
  // Must not block or be time consuming.
  public void majActionActuelle(Action action) ;
  
  // Must not block or be time consuming.
  // The throwable parameter can be null.
  public void reportError(String msg, Throwable e);
  public void reportError(String msg);
  
  public void displayModalMessage(String msg);
}
