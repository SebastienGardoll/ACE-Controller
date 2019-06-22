package fr.gardoll.ace.controller.core;

public interface ToolControlOperations extends Observable
{
  public ToolState getState();
  public void setState(ToolState newState);
  
  public void cancelOperations() throws InterruptedException;
  public void pauseOperations()  throws InterruptedException;
  public void resumeOperations() throws InterruptedException;
  public void closeOperations()  throws InterruptedException;
  public void reinitOperations() throws InterruptedException;
}
