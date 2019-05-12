package fr.gardoll.ace.controller.ui;

public interface ToolControl extends Observable
{
  public void enableControlPanel(boolean isEnable);
  
  public void addControlPanel(ControlPanel ctrlPanel);
  
  public void removeControlPanel(ControlPanel ctrlPanel);
}
