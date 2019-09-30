package fr.gardoll.ace.controller.ui;

import javax.swing.SwingUtilities ;

import fr.gardoll.ace.controller.core.ControlPanel ;
import fr.gardoll.ace.controller.core.ToolControl ;

public abstract class AbstractCancelableJPanelObserver extends AbstractCloseableJPanelObserver
  implements ControlPanel
{
  private static final long serialVersionUID = -5231053034769326388L ;

  protected boolean _isResetEnable  = false;
  protected boolean _isStartEnable  = false;
  protected boolean _isCancelEnable = false;
  
  protected abstract void enableReinitControl(boolean isEnable) ;
  protected abstract void enableStartControl(boolean isEnable) ;
  protected abstract void enableCancelControl(boolean isEnable) ;
  
  public AbstractCancelableJPanelObserver(ToolControl ctrl)
  {
    super(ctrl) ;
  }
  
  @Override
  public final void enableCancel(boolean isEnable)
  {
    this._isCancelEnable = isEnable;
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        AbstractCancelableJPanelObserver.this.enableCancelControl(isEnable);
      }
    });
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
        AbstractCancelableJPanelObserver.this.enableReinitControl(isEnable);
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
        AbstractCancelableJPanelObserver.this.enableStartControl(isEnable);
      }
    });
  }
}
