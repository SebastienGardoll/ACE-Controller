package fr.gardoll.ace.controller.core;

public interface ToolControl extends Observable, ControlPanelHandler
{
  public void displayControlPanelModalMessage(String msg) ;
  
  public void pause() throws InterruptedException;
  
  public void unPause() throws InterruptedException;
  
  public void cancel() throws InterruptedException;
  
  public void reinit() throws InterruptedException;
  
  public void close() throws InterruptedException;
  
  //public ToolState getState();
  
  //public void setState(ToolState state);
  
  //public void cancelOnPause() throws InterruptedException;
}
