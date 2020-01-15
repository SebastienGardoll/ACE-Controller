package fr.gardoll.ace.controller.ui;

import fr.gardoll.ace.controller.core.Log ;
import fr.gardoll.ace.controller.core.ToolControl ;

public class CloseableJPanelObserverStub extends AbstractCloseableJPanelObserver
{
  public CloseableJPanelObserverStub(ToolControl ctrl)
  {
    super(ctrl) ;
  }

  private static final long serialVersionUID = 3868647314433761655L ;

  @Override
  protected void displayToUserLogSys(String msg)
  {
    Log.UI.trace(String.format("display '%s'", msg.strip()));
  }

  @Override
  protected void enableCloseControl(boolean isEnable)
  {
    // Nothing to do.
  }
  
  @Override
  public void dispose()
  {
    // Nothing to do.
  }
}
