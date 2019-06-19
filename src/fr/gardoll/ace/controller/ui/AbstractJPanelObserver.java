package fr.gardoll.ace.controller.ui;

import java.awt.Window ;
import java.text.SimpleDateFormat ;

import javax.swing.JOptionPane ;
import javax.swing.JPanel ;
import javax.swing.SwingUtilities ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.AbstractToolControl ;
import fr.gardoll.ace.controller.core.Action ;
import fr.gardoll.ace.controller.core.ControlPanel ;
import fr.gardoll.ace.controller.core.Observer ;
import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.core.Utils ;

public abstract class AbstractJPanelObserver extends JPanel implements Observer, ControlPanel
{
  private static final long serialVersionUID = -3914638188506779210L ;
  private static final Logger _LOG = LogManager.getLogger(AbstractJPanelObserver.class.getName());
  
  protected static final SimpleDateFormat _DATE_FORMATTER = new SimpleDateFormat("HH:mm:ss");
  
  private final AbstractToolControl _ctrl ;
  
  protected boolean _isResumeEnable = false;
  protected boolean _isPauseEnable  = false;
  protected boolean _isResetEnable  = false;
  protected boolean _isCancelEnable = false;
  protected boolean _isStartEnable  = false;
  protected boolean _isCloseEnable = false;
  
  protected abstract void processAction(Action action);
  protected abstract void enableReinitControl(boolean isEnable) ;
  protected abstract void enablePauseControl(boolean isEnable) ;
  protected abstract void enableResumeControl(boolean isEnable) ;
  protected abstract void enableCancelControl(boolean isEnable) ;
  protected abstract void enableStartControl(boolean isEnable) ;
  protected abstract void enableCloseControl(boolean isEnable) ;
  
  public AbstractJPanelObserver(AbstractToolControl ctrl)
  {
    this._ctrl = ctrl;
  }
  
  protected void handleException(String msg, Exception e)
  {
    this.reportError(msg, e);
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
        AbstractJPanelObserver.this.enablePauseControl(isEnable);
      }
    });
  }
  
  @Override
  public void enableClose(boolean isEnable)
  {
    this._isCloseEnable = isEnable;
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        AbstractJPanelObserver.this.enableCloseControl(isEnable);
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
        AbstractJPanelObserver.this.enableResumeControl(isEnable);
      }
    });
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
        AbstractJPanelObserver.this.enableCancelControl(isEnable);
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
        AbstractJPanelObserver.this.enableReinitControl(isEnable);
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
        AbstractJPanelObserver.this.enableStartControl(isEnable);
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
        _LOG.debug("running the panel cancelling operations") ;
        this._ctrl.cancel();
      }
      else
      {
        _LOG.debug("running the panel reinit operations") ;
        this._ctrl.reinit();
      }
      
      return true;
    }
    else
    {
      if(this._isCancelEnable)
      {
        _LOG.debug("the panel cancelling operations has been skipped") ;
      }
      else
      {
        _LOG.debug("the panel reinit operations has been skipped") ;
      }
      
      return false;
    }
  }
  
  protected boolean close(Window parent)
  {
    int choice = JOptionPane.OK_OPTION;
    
    if(false == ParametresSession.getInstance().isDebug())
    {
      choice = JOptionPane.showConfirmDialog(this,
          "Do you want to exit (and cancel the running operations) ?") ;
    }
    
    if (choice == JOptionPane.OK_OPTION)
    {
      _LOG.debug("running the panel closing operations") ;
      
      this._ctrl.close();
      if(parent != null)
      {
        _LOG.debug("closing the parent frame and may shutdown the JVM");
        parent.dispose();
      }
      
      return true;
    }
    else
    {
      _LOG.debug("the panel closing has been cancelled") ;
      return false;
    }
  }

  @Override
  public void majActionActuelle(Action action)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        AbstractJPanelObserver.this.processAction(action);
      }
    });
  }

  // Throwable can be null.
  @Override
  public void reportError(String msg, Throwable e)
  {
    Utils.reportError(msg, e);
  }
  
  @Override
  public void reportError(String msg)
  {
    Utils.reportError(msg, null);
  }
  
  @Override
  public void displayModalMessage(String msg)
  {
    JOptionPane.showMessageDialog(null, msg, "Information",
        JOptionPane.INFORMATION_MESSAGE);
  }
}
