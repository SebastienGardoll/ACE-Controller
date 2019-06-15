package fr.gardoll.ace.controller.ui;

import java.awt.event.WindowAdapter ;
import java.awt.event.WindowEvent ;

import javax.swing.JDialog ;
import javax.swing.WindowConstants ;

public abstract class AbstractJFrame extends JDialog
{
  private static final long serialVersionUID = -2440934563391351769L ;
  
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
        mainPanel.close(AbstractJFrame.this);
      }
    }) ;
    this.pack();
    this.setLocationRelativeTo(null) ;
  }
}
