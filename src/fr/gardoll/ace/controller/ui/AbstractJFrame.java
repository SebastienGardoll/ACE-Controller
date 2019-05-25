package fr.gardoll.ace.controller.ui;

import java.awt.event.WindowAdapter ;
import java.awt.event.WindowEvent ;

import javax.swing.JFrame ;
import javax.swing.JOptionPane ;
import javax.swing.JPanel ;
import javax.swing.WindowConstants ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

public abstract class AbstractJFrame extends JFrame
{
  private static final Logger _LOG = LogManager.getLogger(AbstractJFrame.class.getName());
  
  private static final long serialVersionUID = -2440934563391351769L ;
  
  public AbstractJFrame(JPanel mainPanel)
  {
    this.getContentPane().add(mainPanel);
    
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent e)
      {
        int choice = JOptionPane.showConfirmDialog(AbstractJFrame.this,
            "Do you want to exit (eventually cancel the running operations) ?") ;
        if (choice == JOptionPane.OK_OPTION)
        {
          _LOG.debug("running the close operations");
          try
          {
            AbstractJFrame.this.onCloseOperation();
          }
          catch(Exception ex)
          {
            _LOG.fatal("error while performing close operation", ex);
          }
          
          _LOG.debug("closing the main frame and shutting down the JVM");
          AbstractJFrame.this.dispose();
        }
      }
    }) ;
  }
  
  protected abstract void onCloseOperation();

}
