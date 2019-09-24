package fr.gardoll.ace.controller.ui;

import java.awt.Window ;
import java.text.SimpleDateFormat ;
import java.util.Date ;

import javax.swing.JOptionPane ;
import javax.swing.JPanel ;
import javax.swing.SwingUtilities ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.Action ;
import fr.gardoll.ace.controller.core.ControlPanel ;
import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.core.ToolControl ;
import fr.gardoll.ace.controller.core.Utils ;

public abstract class AbstractJPanelObserver  extends JPanel implements ControlPanel
{
  private static final long serialVersionUID = -6962722774816032530L ;

  private static final Logger _LOG = LogManager.getLogger(AbstractJPanelObserver.class.getName());
  
  protected static final SimpleDateFormat _DATE_FORMATTER = new SimpleDateFormat("HH:mm:ss");
  
  protected final ToolControl _ctrl ;
  
  protected boolean _isCloseEnable = false;
  
  protected abstract void displayToUserLogSys(String msg);
  protected abstract void enableCloseControl(boolean isEnable) ;
  
  public AbstractJPanelObserver(ToolControl ctrl)
  {
    this._ctrl = ctrl;
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
  public boolean close()
  {
    int choice = JOptionPane.OK_OPTION;
    
    if(false == ParametresSession.getInstance().isDebug())
    {
      choice = JOptionPane.showConfirmDialog(this,
          "Do you want to exit (and cancel the running operations) ?") ;
    }
    
    if (choice == JOptionPane.OK_OPTION)
    {
      _LOG.debug("running closing operations from the panel") ;
      
      this._ctrl.close();
      return true;
    }
    else
    {
      _LOG.debug("the panel closing has been cancelled") ;
      return false;
    }
  }
  
  @Override
  public void dispose()
  {
    try
    {
      // Sleep 1 second so as the user to read the log before closing the window.
      Thread.sleep(1000);
    }
    catch (InterruptedException e)
    {
      // Nothing to do.
    }
    
    Window parent = UiUtils.getParentFrame(this);
    if(parent != null)
    {
      _LOG.debug("closing the parent frame and may shutdown the JVM");
      parent.dispose();
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
    this.displayToUserLogSys(String.format("%s # %s: %s\n", _DATE_FORMATTER.format(new Date()), 
        msg, e.getMessage()));
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
  
  protected void processAction(Action action)
  {
    String msg = null;
    
    switch(action.type)
    {
      case ARM_MOVING:
      {
        msg = "arm is moving";
        break ;
      }
      
      case ARM_END_MOVING:
      {
        msg = "arm reached the position";
        break;
      }
      
      case CANCELING:
      {
        msg = "canceling";
        break ;
      }
      
      case CAROUSEL_MOVING:
      {
        msg = String.format("carousel is moving to position %s", action.data);
        break ;
      }
      
      case CAROUSEL_RELATIVE_MOVING:
      {
        msg = String.format("carousel is moving %s positions", action.data);
        break;
      }
      
      case CAROUSEL_END_MOVING:
      {
        msg = "carousel reached the position";
        break;
      }
      
      case PAUSING:
      {
        msg = "pausing";
        break ;
      }
      
      case PAUSE_DONE:
      {
        msg = "operations are paused";
        break;
      }
      
      case RESUME:
      {
        msg = "resuming";
        break ;
      }
      
      case RESUME_DONE:
      {
        msg = "resuming done";
        break;
      }
      
      case WAIT_CANCEL:
      {
        msg = "waiting for cancellation";
        break ;
      }
      
      case WAIT_PAUSE:
      {
        msg = "waiting for pause";
        break ;
      }
      
      case REINIT:
      {
        msg = "reinitializing";
        break;
      }
      
      case REINIT_DONE:
      {
        msg = "reinitialization is done";
        break;
      }
      
      case CANCEL_DONE:
      {
        msg = "cancellation is done";
        break;
      }
      
      case CLOSING:
      {
        msg = "closing the panel";
        break;
      }
      
      case OPEN_VALVE:
      {
        msg = String.format("opening valve '%s'", action.data.get());
        break;
      }
      
      case CLOSE_VALVES:
      {
        msg = String.format("closing valve '%s'", action.data.get());
        break;
      }
      
      case CLOSE_ALL_VALVES:
      {
        msg = "closing all valves";
        break;
      }

      case WITHDRAWING:
      case INFUSING:
      default:
      {
        _LOG.debug(String.format("nothing to do with action type '%s'", action.type));
        return ;
      }
    }
    
    this.displayToUserLogSys(String.format("%s > %s\n", _DATE_FORMATTER.format(new Date()), msg));
  }
  
  @Override
  public void enableStart(boolean isEnable)
  {  
    throw new UnsupportedOperationException();
  }
  
  @Override
  public void enablePause(boolean isEnable)
  {  
    throw new UnsupportedOperationException();
  }
  
  @Override
  public void enableResume(boolean isEnable)
  {  
    throw new UnsupportedOperationException();
  }
  
  @Override
  public void enableCancel(boolean isEnable)
  {  
    throw new UnsupportedOperationException();
  }
  
  @Override
  public void enableReinit(boolean isEnable)
  {  
    throw new UnsupportedOperationException();
  }
}
