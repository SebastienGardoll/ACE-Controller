package fr.gardoll.ace.controller.ui;

import java.awt.event.WindowAdapter ;
import java.awt.event.WindowEvent ;

import javax.swing.JDialog ;
import javax.swing.WindowConstants ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

public abstract class AbstractJFrame extends JDialog
{
  private static final long serialVersionUID = -2440934563391351769L ;
  
  private static final Logger _LOG = LogManager.getLogger(AbstractJFrame.class.getName());
  
  public AbstractJFrame(AbstractJPanelObserver mainPanel)
  {
    super(null, ModalityType.APPLICATION_MODAL);
    this.getContentPane().add(mainPanel);
    
    // Manage the closing operations.
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent e)
      {
        _LOG.debug("windowClosing");
        mainPanel.close();
      }
    }) ;
    this.pack();
    this.setLocationRelativeTo(null) ;
  }
}
