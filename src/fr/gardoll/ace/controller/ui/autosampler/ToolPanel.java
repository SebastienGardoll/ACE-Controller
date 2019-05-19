package fr.gardoll.ace.controller.ui.autosampler;

import fr.gardoll.ace.controller.ui.AbstractJPanelObserver ;
import fr.gardoll.ace.controller.ui.Action ;
import fr.gardoll.ace.controller.ui.ControlPanel ;
import fr.gardoll.ace.controller.ui.Observer ;

public class ToolPanel extends AbstractJPanelObserver implements ControlPanel, Observer
{
  private static final long serialVersionUID = -5059319837139562760L ;

  @Override
  protected void processAction(Action action)
  {
    // TODO Auto-generated method stub
    
  }

  // Throwable can be null.
  @Override
  protected void processError(String msg, Throwable e)
  {
    // TODO Auto-generated method stub
  }
  
  @Override
  public void enableControl(boolean isEnable)
  {
    // TODO
  }

}
