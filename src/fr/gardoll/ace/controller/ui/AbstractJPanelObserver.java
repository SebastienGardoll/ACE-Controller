package fr.gardoll.ace.controller.ui;

import javax.swing.JPanel ;
import javax.swing.SwingUtilities ;

public abstract class AbstractJPanelObserver extends JPanel implements Observer, ControlPanel
{
  private static final long serialVersionUID = -3914638188506779210L ;

  abstract protected void processAction(Action action);
  
  // Throwable can be null.
  abstract protected void processError(String msg, Throwable e);
  
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

  @Override
  public void reportError(String msg, Throwable e)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        AbstractJPanelObserver.this.processError(msg, e);
      }
    });
  }
  
  @Override
  public void reportError(String msg)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        AbstractJPanelObserver.this.processError(msg, null);
      }
    });
  }
  
  @Override
  public void displayModalMessage(String msg)
  {
    // TODO Auto-generated method stub
    
  }
}
