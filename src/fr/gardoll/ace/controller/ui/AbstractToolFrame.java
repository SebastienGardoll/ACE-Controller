package fr.gardoll.ace.controller.ui;

import java.awt.Dimension ;
import java.awt.Toolkit ;

import javax.swing.JDialog ;
import javax.swing.JPanel ;
import javax.swing.WindowConstants ;

import fr.gardoll.ace.controller.core.ParametresSession ;

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
    
    if(ParametresSession.getInstance().isFullScreen())
    {
      // Set full screen.
      Dimension dim = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
      this.setSize(dim);
    }
    
    this.setLocationRelativeTo(null) ;
  }
}
