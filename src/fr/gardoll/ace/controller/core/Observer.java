package fr.gardoll.ace.controller.core;

public interface Observer
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
}
