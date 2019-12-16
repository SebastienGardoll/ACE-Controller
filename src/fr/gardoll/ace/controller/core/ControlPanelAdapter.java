package fr.gardoll.ace.controller.core;

public abstract class ControlPanelAdapter implements ControlPanel
{
  @Override
  public void reportError(String msg, Throwable e)
  {
    // Nothing to do.
  }

  @Override
  public void reportError(String msg)
  {
    // Nothing to do.  
  }

  @Override
  public void displayModalMessage(String msg)
  {
    // Nothing to do.
  }

  @Override
  public boolean close()
  {
    // Nothing to do.
    return true;
  }

  @Override
  public void dispose()
  {
    // Nothing to do.
  }

  @Override
  public void enableStart(boolean isEnable)
  {
    // Nothing to do.
  }

  @Override
  public void enableClose(boolean isEnable)
  {
    // Nothing to do.
  }

  @Override
  public void enablePause(boolean isEnable)
  {
    // Nothing to do.
  }

  @Override
  public void enableResume(boolean isEnable)
  {
    // Nothing to do.
  }

  @Override
  public void enableCancel(boolean isEnable)
  {
    // Nothing to do.
  }

  @Override
  public void enableReinit(boolean isEnable)
  {
    // Nothing to do.
  }
  
  @Override
  public void enableCarousel(boolean isEnable)
  {
    // Nothing to do.
  }
}
