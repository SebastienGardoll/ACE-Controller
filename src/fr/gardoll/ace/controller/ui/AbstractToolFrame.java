package fr.gardoll.ace.controller.ui;

import javax.swing.JDialog ;
import javax.swing.JPanel ;
import javax.swing.WindowConstants ;

public abstract class AbstractToolFrame extends JDialog
{
  private static final long serialVersionUID = -2440934563391351769L ;
  
  public AbstractToolFrame(JPanel mainPanel)
  {
    super(null, ModalityType.APPLICATION_MODAL);
    this.getContentPane().add(mainPanel);
    
    // Manage the closing operations.
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    this.pack();
    this.setLocationRelativeTo(null) ;
  }
}
