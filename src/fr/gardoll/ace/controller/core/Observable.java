package fr.gardoll.ace.controller.core;

public interface Observable extends ControlPanelHandler
{
  // Must not block and be time consuming.
  public void notifyAction(Action action);
  
  // May be blocking call.
  public void notifyError(String msg);
  public void notifyError(String msg, Throwable e);
}
