package fr.gardoll.ace.controller.ui;

import java.awt.event.WindowAdapter ;
import java.awt.event.WindowEvent ;

import javax.swing.JFrame ;
import javax.swing.WindowConstants ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

public abstract class AbstractJFrame extends JFrame
{
  private static final Logger _LOG = LogManager.getLogger(AbstractJFrame.class.getName());
  
  private static final long serialVersionUID = -2440934563391351769L ;
  
  public AbstractJFrame(AbstractJPanelObserver mainPanel)
  {
    this.getContentPane().add(mainPanel);
    
    // Manage the closing operations.
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent e)
      {
        boolean closeDialog = false;
        
        if(false == mainPanel.isClosed())
        {
          closeDialog = mainPanel.close() ;
        }
        else 
        {
          // Main panel is already close. Just shutdown the dialog.
          _LOG.debug("main panel is already closed");
          closeDialog = true;
        }
        
        if(closeDialog)
        {
          _LOG.debug("closing the dialog and may shutdown the JVM");
          AbstractJFrame.this.dispose();
        }
        else
        {
          _LOG.debug("closing has been cancelled");
        }
      }
    }) ;
    this.pack();
    this.setLocationRelativeTo(null) ;
  }
}
