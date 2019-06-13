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
        if(false == mainPanel.isClosed())
        {
          if(mainPanel.close(AbstractJFrame.this))
          {
            _LOG.debug("the frame has been closed");
          }
          else
          {
            _LOG.debug("closing the frame has been cancelled");
          }
        }
        else 
        {
          // Main panel is already close. Just shutdown the dialog.
          _LOG.debug("main panel is already closed");
          AbstractJFrame.this.dispose();
        }
      }
    }) ;
    this.pack();
    this.setLocationRelativeTo(null) ;
  }
}
