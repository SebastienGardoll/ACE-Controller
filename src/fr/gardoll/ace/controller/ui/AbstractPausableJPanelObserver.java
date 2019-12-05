package fr.gardoll.ace.controller.ui;

import javax.swing.JOptionPane ;
import javax.swing.SwingUtilities ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.ControlPanel ;
import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.core.ToolControl ;

public abstract class AbstractPausableJPanelObserver extends AbstractCancelableJPanelObserver
                                                         implements ControlPanel
{
  private static final long serialVersionUID = -3914638188506779210L ;
  private static final Logger _LOG = LogManager.getLogger(AbstractPausableJPanelObserver.class.getName());
  
  protected boolean _isResumeEnable = false;
  protected boolean _isPauseEnable  = false;
  
  protected abstract void enablePauseControl(boolean isEnable) ;
  protected abstract void enableResumeControl(boolean isEnable) ;
  
  public AbstractPausableJPanelObserver(ToolControl ctrl)
  {
    super(ctrl);
  }
  
  @Override
  public final void enablePause(boolean isEnable)
  {
    this._isPauseEnable = isEnable;
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        AbstractPausableJPanelObserver.this.enablePauseControl(isEnable);
      }
    });
  }
  
  @Override
  public final void enableResume(boolean isEnable)
  {
    this._isResumeEnable = isEnable;
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        AbstractPausableJPanelObserver.this.enableResumeControl(isEnable);
      }
    });
  }
  
  protected void pauseAndResume()
  {
    if(this._isPauseEnable)
    {
      this._ctrl.pause();
    }
    else
    {
      this._ctrl.resume();
    }
  }
  
  protected boolean cancelAndReinit()
  {
    int choice = JOptionPane.OK_OPTION;
    
    if(false == ParametresSession.getInstance().isDebug())
    {
      String msg = null;
      
      if(this._isCancelEnable)
      {
        msg = "Do you want to cancel the running operations (and returning to the initial position) ?";
      }
      else
      {
        msg = "Do you want to reinitialize ACE ?";
      }
      
      choice = JOptionPane.showConfirmDialog(this, msg) ;
    }
    
    if (choice == JOptionPane.OK_OPTION)
    {
      if(this._isCancelEnable)
      {
        _LOG.debug("calling cancel from the panel") ;
        this._ctrl.cancel();
      }
      else
      {
        _LOG.debug("calling reinit from the panel") ;
        this._ctrl.reinit();
      }
      
      return true;
    }
    else
    {
      if(this._isCancelEnable)
      {
        _LOG.debug("cancel from the panel has been cancelled") ;
      }
      else
      {
        _LOG.debug("reinit from the panel has been cancelled") ;
      }
      
      return false;
    }
  }
}
