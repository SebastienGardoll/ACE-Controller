package fr.gardoll.ace.controller.core;

import java.util.Set ;

public interface ControlPanelHandler
{
  public void addControlPanel(ControlPanel ctrlPanel);
  public void removeControlPanel(ControlPanel ctrlPanel);
  public Set<ControlPanel> getCtrlPanels(); // return immutable set.
}
