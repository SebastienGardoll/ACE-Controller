package fr.gardoll.ace.controller.core;

public interface ControlPanel extends Observer
{
  public void enableStart(boolean isEnable);
  public void enableClose(boolean isEnable);
  public void enablePause(boolean isEnable);
  public void enableResume(boolean isEnable);
  public void enableCancel(boolean isEnable);
  public void enableReinit(boolean isEnable);
}
