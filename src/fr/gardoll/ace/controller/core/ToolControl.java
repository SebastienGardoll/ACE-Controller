package fr.gardoll.ace.controller.core;

public interface ToolControl extends Observable, ControlPanelHandler
{
  public void displayControlPanelModalMessage(String msg) ;
  
  public void pause();
  
  public void resume();
  
  public void cancel();
  
  public void reinit();
  
  public void close();
}
