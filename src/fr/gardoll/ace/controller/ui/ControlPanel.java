package fr.gardoll.ace.controller.ui;

import javax.swing.JFrame ;

public interface ControlPanel
{
  public void enableControl(boolean isEnable);
  public boolean close(JFrame parent);
  public boolean isClosed(); 
}
