package fr.gardoll.ace.controller.ui;

import javax.swing.JOptionPane ;
import javax.swing.JPanel ;
import javax.swing.SwingUtilities ;

public abstract class AbstractJPanelObserver extends JPanel implements Observer, ControlPanel
{
  private static final long serialVersionUID = -3914638188506779210L ;

  abstract protected void processAction(Action action);
  
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
    String displayedMsg = null;
    if (e != null)
    {
      displayedMsg = String.format("%s: %s", msg, e.getMessage());
    }
    else
    {
      displayedMsg = msg;
    }
     
    JOptionPane.showMessageDialog(null, displayedMsg, "Error",
        JOptionPane.ERROR_MESSAGE);
  }
  
  @Override
  public void reportError(String msg)
  {
    this.reportError(msg, null);
  }
  
  @Override
  public void displayModalMessage(String msg)
  {
    JOptionPane.showMessageDialog(null, msg, "Information",
        JOptionPane.INFORMATION_MESSAGE);
  }
}
