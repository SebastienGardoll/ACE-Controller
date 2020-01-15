package fr.gardoll.ace.controller.ui;

import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.Log ;
import fr.gardoll.ace.controller.core.ToolControl ;

public class CloseableJPanelObserverStub extends AbstractCloseableJPanelObserver
{
  private static final Logger _LOG = Log.STUB;
  
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
    _LOG.trace(String.format("close control set to %s", isEnable));
  }
  
  @Override
  public void dispose()
  {
    _LOG.trace("dispose");
  }
}
