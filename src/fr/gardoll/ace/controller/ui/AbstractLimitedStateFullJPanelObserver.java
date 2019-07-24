package fr.gardoll.ace.controller.ui;

import javax.swing.SwingUtilities ;

import fr.gardoll.ace.controller.core.ControlPanel ;
import fr.gardoll.ace.controller.core.ToolControl ;

public abstract class AbstractLimitedStateFullJPanelObserver extends AbstractJPanelObserver
  implements ControlPanel
{
  private static final long serialVersionUID = -5231053034769326388L ;

  protected boolean _isResetEnable  = false;
  protected boolean _isStartEnable  = false;
  
  protected abstract void enableReinitControl(boolean isEnable) ;
  protected abstract void enableStartControl(boolean isEnable) ;
  
  public AbstractLimitedStateFullJPanelObserver(ToolControl ctrl)
  {
    super(ctrl) ;
  }
  
  @Override
  public final void enableReinit(boolean isEnable)
  {
    this._isResetEnable = isEnable;
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        AbstractLimitedStateFullJPanelObserver.this.enableReinitControl(isEnable);
      }
    });
  }
  
  @Override
  public final void enableStart(boolean isEnable)
  {
    this._isStartEnable = isEnable;
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        AbstractLimitedStateFullJPanelObserver.this.enableStartControl(isEnable);
      }
    });
  }
}
