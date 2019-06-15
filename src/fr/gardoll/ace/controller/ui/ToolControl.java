package fr.gardoll.ace.controller.ui;

public interface ToolControl extends Observable
{
  public void enableControlPanel(boolean isEnable);
  
  public void displayControlPanelModalMessage(String msg) ;
  
  public void addControlPanel(ControlPanel ctrlPanel);
  
  public void removeControlPanel(ControlPanel ctrlPanel);
  
  public void pause() throws InterruptedException;
  
  public void unPause() throws InterruptedException;
  
  public void cancel() throws InterruptedException;
  
  public void close() throws InterruptedException;
  
  public boolean isClosed();
  
  public boolean isPaused();
}
