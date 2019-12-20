package fr.gardoll.ace.controller.tools.extraction ;

import java.awt.Dialog ;
import java.awt.event.WindowAdapter ;
import java.awt.event.WindowEvent ;
import java.io.File ;
import java.nio.file.Path ;
import java.util.Optional ;

import javax.swing.JFileChooser ;
import javax.swing.SpinnerNumberModel ;
import javax.swing.WindowConstants ;
import javax.swing.filechooser.FileFilter ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.Names ;
import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.protocol.Protocol ;

public class ExtractionConfigurationDialog extends javax.swing.JDialog
{
  private static final Logger _LOG = LogManager.getLogger(ExtractionConfigurationDialog.class.getName());
  
  private static int _DEFAULT_MAX_SEQUENCES  = 99;
  private static int _DEFAULT_MIN_SEQUENCES  =  1;
  private static int _DEFAULT_SEQUENCE_VALUE =  1;
  private static int _DEFAULT_MIN_COLUMNS    =  1;
  private static int _DEFAULT_COLUMN_VALUE   =  1;
  private static int _DEFAULT_STEP           =  1;
  
  private static final long serialVersionUID = -1634098099578213468L ;
  
  private Optional<InitSession> _initSession = Optional.empty();

  private Optional<Path> _protocolPath = Optional.empty();
  
  private JFileChooser _fileChooser = new JFileChooser();

  public ExtractionConfigurationDialog(Dialog parent,
      String title, boolean modal)
  {
    super(parent, title, modal) ;
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    this.pack();
    this.setLocationRelativeTo(null) ;
    this.addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent e)
      {
        ExtractionConfigurationDialog.this.cancel();
      }
    });
    
    initComponents() ;
    initCustom();
  }
  
  private void initCustom()
  {
    int maxColumnUtil = ParametresSession.getInstance().nbMaxColonne() - 1;
    
    SpinnerNumberModel nbColumnModel = new SpinnerNumberModel(
        _DEFAULT_COLUMN_VALUE, _DEFAULT_MIN_COLUMNS, maxColumnUtil,
        _DEFAULT_STEP);
    this.nbColumnSpinner.setModel(nbColumnModel);
    
    SpinnerNumberModel numColumnModel = new SpinnerNumberModel(
        _DEFAULT_COLUMN_VALUE, _DEFAULT_MIN_COLUMNS, maxColumnUtil,
        _DEFAULT_STEP);
    this.numColumnSpinner.setModel(numColumnModel);
    
    SpinnerNumberModel numSequenceModel = new SpinnerNumberModel(
        _DEFAULT_SEQUENCE_VALUE, _DEFAULT_MIN_SEQUENCES, _DEFAULT_MAX_SEQUENCES,
        _DEFAULT_STEP);
    this.numSequenceSpinner.setModel(numSequenceModel);
    
    this._fileChooser.setDialogTitle("select column file");
    this._fileChooser.setCurrentDirectory(Names.PROTOCOL_DIR_PATH.toFile());
    this._fileChooser.setMultiSelectionEnabled(false);
    this._fileChooser.setFileFilter(new FileFilter() 
    {
      @Override
      public boolean accept(File f)
      {
        if(f.isDirectory())
        {
          return true;
        }
        else
        {
          String fileName = f.getName();
          String file_extention = Utils.getFileExtention(fileName);
          return Protocol.PROTOCOL_FILE_EXTENTION.equals(file_extention);
        }
      }

      @Override
      public String getDescription()
      {
        return String.format("protocol file *.%s", Protocol.PROTOCOL_FILE_EXTENTION);
      }
    });
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">
  private void initComponents()
  {
    java.awt.GridBagConstraints gridBagConstraints ;

    protocolLabel = new javax.swing.JLabel() ;
    openButton = new javax.swing.JButton() ;
    protocolTextField = new javax.swing.JTextField() ;
    nbColumnLabel = new javax.swing.JLabel() ;
    nbColumnSpinner = new javax.swing.JSpinner() ;
    numSequenceLabel = new javax.swing.JLabel() ;
    numSequenceSpinner = new javax.swing.JSpinner() ;
    numColumnLabel = new javax.swing.JLabel() ;
    numColumnSpinner = new javax.swing.JSpinner() ;
    okButton = new javax.swing.JButton() ;
    cancelButton = new javax.swing.JButton() ;
    jSeparator1 = new javax.swing.JSeparator() ;

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE) ;
    getContentPane().setLayout(new java.awt.GridBagLayout()) ;

    protocolLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER) ;
    protocolLabel.setText("Protocol") ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    getContentPane().add(protocolLabel, gridBagConstraints) ;

    openButton.setText("open") ;
    openButton.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        openButtonActionPerformed(evt) ;
      }
    }) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 1 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    getContentPane().add(openButton, gridBagConstraints) ;

    protocolTextField.setEditable(false) ;
    protocolTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER) ;
    protocolTextField.setText("click on open") ;
    protocolTextField.setToolTipText("click on open button") ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 2 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    getContentPane().add(protocolTextField, gridBagConstraints) ;

    nbColumnLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER) ;
    nbColumnLabel.setText("Total number of columns") ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 1 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    getContentPane().add(nbColumnLabel, gridBagConstraints) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 1 ;
    gridBagConstraints.gridy = 1 ;
    gridBagConstraints.gridwidth = 2 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    getContentPane().add(nbColumnSpinner, gridBagConstraints) ;

    numSequenceLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER) ;
    numSequenceLabel.setText("Start at sequence") ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 2 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    getContentPane().add(numSequenceLabel, gridBagConstraints) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 1 ;
    gridBagConstraints.gridy = 2 ;
    gridBagConstraints.gridwidth = 2 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    getContentPane().add(numSequenceSpinner, gridBagConstraints) ;

    numColumnLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER) ;
    numColumnLabel.setText("Start at column") ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 3 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    getContentPane().add(numColumnLabel, gridBagConstraints) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 1 ;
    gridBagConstraints.gridy = 3 ;
    gridBagConstraints.gridwidth = 2 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    getContentPane().add(numColumnSpinner, gridBagConstraints) ;

    okButton.setText("ok") ;
    okButton.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        okButtonActionPerformed(evt) ;
      }
    }) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 5 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    getContentPane().add(okButton, gridBagConstraints) ;

    cancelButton.setText("cancel") ;
    cancelButton.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cancelButtonActionPerformed(evt) ;
      }
    }) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 2 ;
    gridBagConstraints.gridy = 5 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    getContentPane().add(cancelButton, gridBagConstraints) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 4 ;
    gridBagConstraints.gridwidth = 3 ;
    getContentPane().add(jSeparator1, gridBagConstraints) ;

    pack() ;
  }// </editor-fold>

  private void openButtonActionPerformed(java.awt.event.ActionEvent evt)
  {
    _LOG.debug("**** event protocol file opening ****");
    
    int returnValue = this._fileChooser.showOpenDialog(this);
    if(returnValue == JFileChooser.APPROVE_OPTION)
    {
      File file = this._fileChooser.getSelectedFile().getAbsoluteFile();
      _LOG.info(String.format("selected protocol file is '%s'", file));
      Path protocolPath = file.toPath();
      this._protocolPath = Optional.of(file.toPath());
      Path filename = protocolPath.getFileName();
      this.protocolTextField.setText(filename.toString());
    }
    else
    {
      _LOG.debug("cancel protocol file openning");
    }
  }

  private void okButtonActionPerformed(java.awt.event.ActionEvent evt)
  {
    _LOG.debug("starting extraction configuration checking");
    
    try
    {
      if(this._protocolPath.isEmpty())
      {
        Utils.reportError("protocol is missing", null);
        return;
      }
      
      this.nbColumnSpinner.commitEdit();
      Integer nbColumn = (Integer) this.nbColumnSpinner.getValue();
      
      this.numColumnSpinner.commitEdit();
      Integer numColumn = (Integer) this.numColumnSpinner.getValue();
      
      this.numSequenceSpinner.commitEdit();
      Integer numSequence = (Integer) this.numSequenceSpinner.getValue();
      
      InitSession initSession = new InitSession(nbColumn, numColumn,
          numSequence, this._protocolPath.get());
      
      this._initSession = Optional.of(initSession);
      
      this.dispose();
    }
    catch(Exception e)
    {
      _LOG.debug("init session checking failed", e);
      Utils.reportError("error", e);
    }
  }

  private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)
  {
    this.cancel();
  }
  
  public Optional<InitSession> getInitSession()
  {
    return this._initSession;
  }
  
  private void cancel()
  {
    _LOG.debug("extraction configuration is cancelled");
    this._initSession = Optional.empty();
    this.dispose();
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String args[])
  {
    /* Set the Nimbus look and feel */
    // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
    // (optional) ">
    /*
     * If Nimbus (introduced in Java SE 6) is not available, stay with the
     * default look and feel. For details see
     * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
     */
    try
    {
      for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager
          .getInstalledLookAndFeels())
      {
        if ("Nimbus".equals(info.getName()))
        {
          javax.swing.UIManager.setLookAndFeel(info.getClassName()) ;
          break ;
        }
      }
    }
    catch (ClassNotFoundException ex)
    {
      java.util.logging.Logger.getLogger(ExtractionConfigurationDialog.class.getName())
          .log(java.util.logging.Level.SEVERE, null, ex) ;
    }
    catch (InstantiationException ex)
    {
      java.util.logging.Logger.getLogger(ExtractionConfigurationDialog.class.getName())
          .log(java.util.logging.Level.SEVERE, null, ex) ;
    }
    catch (IllegalAccessException ex)
    {
      java.util.logging.Logger.getLogger(ExtractionConfigurationDialog.class.getName())
          .log(java.util.logging.Level.SEVERE, null, ex) ;
    }
    catch (javax.swing.UnsupportedLookAndFeelException ex)
    {
      java.util.logging.Logger.getLogger(ExtractionConfigurationDialog.class.getName())
          .log(java.util.logging.Level.SEVERE, null, ex) ;
    }
    // </editor-fold>

    /* Create and display the dialog */
    java.awt.EventQueue.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        ExtractionConfigurationDialog dialog = new ExtractionConfigurationDialog(
            null, "extraction configuration", true) ;
        dialog.addWindowListener(new java.awt.event.WindowAdapter()
        {
          @Override
          public void windowClosing(java.awt.event.WindowEvent e)
          {
            System.exit(0) ;
          }
        }) ;
        dialog.setVisible(true) ;
      }
    }) ;
  }

  // Variables declaration - do not modify
  private javax.swing.JButton cancelButton ;
  private javax.swing.JSeparator jSeparator1 ;
  private javax.swing.JLabel nbColumnLabel ;
  private javax.swing.JSpinner nbColumnSpinner ;
  private javax.swing.JLabel numColumnLabel ;
  private javax.swing.JSpinner numColumnSpinner ;
  private javax.swing.JLabel numSequenceLabel ;
  private javax.swing.JSpinner numSequenceSpinner ;
  private javax.swing.JButton okButton ;
  private javax.swing.JButton openButton ;
  private javax.swing.JLabel protocolLabel ;
  private javax.swing.JTextField protocolTextField ;
  // End of variables declaration
}