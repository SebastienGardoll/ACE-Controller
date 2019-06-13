package fr.gardoll.ace.controller.ui;

import javax.swing.JFrame ;
import javax.swing.JOptionPane ;
import javax.swing.JPanel ;
import javax.swing.SwingUtilities ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.Utils ;

public abstract class AbstractJPanelObserver extends JPanel implements Observer, ControlPanel
{
  private static final long serialVersionUID = -3914638188506779210L ;
  private static final Logger _LOG = LogManager.getLogger(AbstractJPanelObserver.class.getName());
  private final AbstractToolControl _ctrl ;
  private boolean _isClosed = false;

  abstract protected void processAction(Action action);
  
  public AbstractJPanelObserver(AbstractToolControl ctrl)
  {
    this._ctrl = ctrl;
  }
  
  @Override
  public boolean close(JFrame parent)
  {
    if(false == this.isClosed())
    {
      int choice = JOptionPane.showConfirmDialog(this,
          "Do you want to exit (and cancel the running operations) ?") ;
      if (choice == JOptionPane.OK_OPTION)
      {
        _LOG.debug("running the panel closing operations") ;
        try
        {
          this._ctrl.close();
          if(parent != null)
          {
            _LOG.debug("closing the parent frame and may shutdown the JVM");
            parent.dispose();
          }
        }
        catch (Exception ex)
        {
          String msg = "error while performing close operation" ;
          _LOG.fatal(String.format("%s: %s", msg, ex.getMessage()), ex) ;
          Utils.reportError(msg, ex) ;
        }
        
        return true;
      }
      else
      {
        return false;
      }
    }
    else
    {
      return true;
    }
  }

  @Override
  public boolean isClosed()
  {
    return this._isClosed ;
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
