package fr.gardoll.ace.controller.tools.settings.general ;

import java.text.ParseException ;

import javax.swing.SpinnerNumberModel ;

import fr.gardoll.ace.controller.settings.ConfigurationException ;
import fr.gardoll.ace.controller.settings.GeneralSettings ;
import fr.gardoll.ace.controller.ui.TextFieldRealNumber ;

//TODO check syringe volume against rinse volume.
public class PumpPanel extends javax.swing.JPanel implements Panel
{
  private static final long serialVersionUID = -4086064621880324437L ;
  
  public static final String NAME = "pump" ;

  private TextFieldRealNumber syringeVolTextFieldRealNumber ;

  private TextFieldRealNumber pumpMaxRateTextFieldRealNumber ;

  private TextFieldRealNumber rinseVolTextFieldRealNumber ;

  private TextFieldRealNumber syringeDiaTextFieldRealNumber ;

  /**
   * Creates new form PumpPanel
   * @throws ConfigurationException 
   */
  public PumpPanel() throws ConfigurationException
  {
    initComponents() ;
    initCustom() ;
    load() ;
  }

  private void load() throws ConfigurationException
  {
    GeneralSettings settings = GeneralSettings.instance();
    
    String pumpMaxRateValue = String.valueOf(settings.getDebitMaxPousseSeringue());
    this.pumpMaxRateTextField.setText(pumpMaxRateValue);
    
    this.oneSyringeRadioButton.setSelected(settings.getNbSeringue() == 1);
    this.twoSyringeRadioButton.setSelected(settings.getNbSeringue() == 2);
    
    String rinseVolumeValue = String.valueOf(settings.getVolumeRincage());
    this.rinseVolumeTextField.setText(rinseVolumeValue);
    
    this.rinseNumberSpinner.setValue(settings.getNbRincage());
    
    String syringeDiameterValue = String.valueOf(settings.getDiametreSeringue());
    this.syringeDiameterTextField.setText(syringeDiameterValue);
    
    String syringeVolumeValue = String.valueOf(settings.getVolumeMaxSeringue());
    this.syringeVolumeTextField.setText(syringeVolumeValue);
  }

  @Override
  public void set() throws ConfigurationException
  {
    int nbRinse            = (int) this.rinseNumberSpinner.getValue();
    double rinseVolume     =       this.rinseVolTextFieldRealNumber.parse();
    double syringeVolume   =       this.syringeVolTextFieldRealNumber.parse();
    double syringeDiameter =       this.syringeDiaTextFieldRealNumber.parse();
    double pumpMaxRate     =       this.pumpMaxRateTextFieldRealNumber.parse();
    int nbSyringe          =      (this.oneSyringeRadioButton.isSelected())?1:2;
    
    GeneralSettings settings = GeneralSettings.instance();
    settings.setNbRincage(nbRinse);
    settings.setVolumeRincage(rinseVolume);
    settings.setVolumeMaxSeringue(syringeVolume);
    settings.setDiametreSeringue(syringeDiameter);
    settings.setDebitMaxPousseSeringue(pumpMaxRate);
    settings.setNbSeringue(nbSyringe);
  }

  @Override
  public String getName()
  {
    return PumpPanel.NAME ;
  }

  @Override
  public void check() throws ConfigurationException
  {
    try
    {
      this.rinseNumberSpinner.commitEdit();
    }
    catch (ParseException e)
    {
      throw new ConfigurationException("failed to fetch the number of rincing", e);
    }
    
    int nbRinse            = (int) this.rinseNumberSpinner.getValue();
    double rinseVolume     =       this.rinseVolTextFieldRealNumber.parse();
    double syringeVolume   =       this.syringeVolTextFieldRealNumber.parse();
    double syringeDiameter =       this.syringeDiaTextFieldRealNumber.parse();
    double pumpMaxRate     =       this.pumpMaxRateTextFieldRealNumber.parse();
    int nbSyringe          =      (this.oneSyringeRadioButton.isSelected())?1:2;
    
    GeneralSettings settings = GeneralSettings.instance();
    settings.checkNbRincage(nbRinse);
    settings.checkVolumeRincage(rinseVolume);
    settings.checkVolumeMaxSeringue(syringeVolume);
    settings.checkDiametreSeringue(syringeDiameter);
    settings.checkDebitMaxPousseSeringue(pumpMaxRate);
    settings.checkNbSeringue(nbSyringe);
  }

  private void initCustom()
  {
    this.syringebuttonGroup.add(this.oneSyringeRadioButton) ;
    this.syringebuttonGroup.add(this.twoSyringeRadioButton) ;

    this.pumpMaxRateTextFieldRealNumber = new TextFieldRealNumber("pump max rate",
        this.pumpMaxRateTextField) ;

    this.rinseVolTextFieldRealNumber = new TextFieldRealNumber("rinse volume",
        this.rinseVolumeTextField) ;

    this.syringeDiaTextFieldRealNumber = new TextFieldRealNumber("syringe diameter",
        this.syringeDiameterTextField) ;
    
    this.syringeVolTextFieldRealNumber = new TextFieldRealNumber("syringe volume",
        this.syringeVolumeTextField) ;
    
    SpinnerNumberModel nbRinseModel = new SpinnerNumberModel(
        1, GeneralSettings.DEFAULT_MIN_RINSE_NB, GeneralSettings.DEFAULT_MAX_RINSE_NB, 1) ;
    this.rinseNumberSpinner.setModel(nbRinseModel) ;
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

    syringebuttonGroup = new javax.swing.ButtonGroup() ;
    pumpSettingsPanel = new javax.swing.JPanel() ;
    pumpMaxRatePanel = new javax.swing.JPanel() ;
    pumpMaxRateTextField = new javax.swing.JTextField() ;
    pumpSyringePanel = new javax.swing.JPanel() ;
    oneSyringeRadioButton = new javax.swing.JRadioButton() ;
    twoSyringeRadioButton = new javax.swing.JRadioButton() ;
    rinseSettingsPanel = new javax.swing.JPanel() ;
    rinseVolumePanel = new javax.swing.JPanel() ;
    rinseVolumeTextField = new javax.swing.JTextField() ;
    rinseNumberPanel = new javax.swing.JPanel() ;
    rinseNumberSpinner = new javax.swing.JSpinner() ;
    syringeSettingsPanel = new javax.swing.JPanel() ;
    syringeDiameterPanel = new javax.swing.JPanel() ;
    syringeDiameterTextField = new javax.swing.JTextField() ;
    syringeVolumePanel = new javax.swing.JPanel() ;
    syringeVolumeTextField = new javax.swing.JTextField() ;

    setPreferredSize(new java.awt.Dimension(780, 460)) ;
    setLayout(new java.awt.GridBagLayout()) ;

    pumpSettingsPanel
        .setBorder(javax.swing.BorderFactory.createTitledBorder("Pump")) ;
    pumpSettingsPanel.setLayout(new java.awt.GridBagLayout()) ;

    pumpMaxRatePanel.setBorder(
        javax.swing.BorderFactory.createTitledBorder("Maximum rate (mL/min)")) ;
    pumpMaxRatePanel.setLayout(new java.awt.GridBagLayout()) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.ipadx = 70 ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    pumpMaxRatePanel.add(pumpMaxRateTextField, gridBagConstraints) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    pumpSettingsPanel.add(pumpMaxRatePanel, gridBagConstraints) ;

    pumpSyringePanel.setBorder(
        javax.swing.BorderFactory.createTitledBorder("Number of syringe")) ;
    pumpSyringePanel.setLayout(new java.awt.GridBagLayout()) ;

    oneSyringeRadioButton.setText("one") ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    pumpSyringePanel.add(oneSyringeRadioButton, gridBagConstraints) ;

    twoSyringeRadioButton.setText("two") ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 1 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    pumpSyringePanel.add(twoSyringeRadioButton, gridBagConstraints) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 1 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    pumpSettingsPanel.add(pumpSyringePanel, gridBagConstraints) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    add(pumpSettingsPanel, gridBagConstraints) ;

    rinseSettingsPanel
        .setBorder(javax.swing.BorderFactory.createTitledBorder("Rinsing")) ;
    rinseSettingsPanel.setLayout(new java.awt.GridBagLayout()) ;

    rinseVolumePanel.setBorder(
        javax.swing.BorderFactory.createTitledBorder("Volume (mL)")) ;
    rinseVolumePanel.setLayout(new java.awt.GridBagLayout()) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    rinseVolumePanel.add(rinseVolumeTextField, gridBagConstraints) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    rinseSettingsPanel.add(rinseVolumePanel, gridBagConstraints) ;

    rinseNumberPanel.setBorder(
        javax.swing.BorderFactory.createTitledBorder("Number per cycle")) ;
    rinseNumberPanel.setLayout(new java.awt.GridBagLayout()) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    rinseNumberPanel.add(rinseNumberSpinner, gridBagConstraints) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 1 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    rinseSettingsPanel.add(rinseNumberPanel, gridBagConstraints) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 1 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    add(rinseSettingsPanel, gridBagConstraints) ;

    syringeSettingsPanel
        .setBorder(javax.swing.BorderFactory.createTitledBorder("Syringe")) ;
    syringeSettingsPanel.setLayout(new java.awt.GridBagLayout()) ;

    syringeDiameterPanel.setBorder(
        javax.swing.BorderFactory.createTitledBorder("Diameter (mm)")) ;
    syringeDiameterPanel.setToolTipText("") ;
    syringeDiameterPanel.setLayout(new java.awt.GridBagLayout()) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.ipadx = 70 ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    syringeDiameterPanel.add(syringeDiameterTextField, gridBagConstraints) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    syringeSettingsPanel.add(syringeDiameterPanel, gridBagConstraints) ;

    syringeVolumePanel.setBorder(
        javax.swing.BorderFactory.createTitledBorder("Volume (mL)")) ;
    syringeVolumePanel.setLayout(new java.awt.GridBagLayout()) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    syringeVolumePanel.add(syringeVolumeTextField, gridBagConstraints) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 1 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    syringeSettingsPanel.add(syringeVolumePanel, gridBagConstraints) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 1 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    add(syringeSettingsPanel, gridBagConstraints) ;
  }// </editor-fold>

  // Variables declaration - do not modify
  private javax.swing.JRadioButton oneSyringeRadioButton ;
  private javax.swing.JPanel pumpMaxRatePanel ;
  private javax.swing.JTextField pumpMaxRateTextField ;
  private javax.swing.JPanel pumpSettingsPanel ;
  private javax.swing.JPanel pumpSyringePanel ;
  private javax.swing.JPanel rinseNumberPanel ;
  private javax.swing.JSpinner rinseNumberSpinner ;
  private javax.swing.JPanel rinseSettingsPanel ;
  private javax.swing.JPanel rinseVolumePanel ;
  private javax.swing.JTextField rinseVolumeTextField ;
  private javax.swing.JPanel syringeDiameterPanel ;
  private javax.swing.JTextField syringeDiameterTextField ;
  private javax.swing.JPanel syringeSettingsPanel ;
  private javax.swing.JPanel syringeVolumePanel ;
  private javax.swing.JTextField syringeVolumeTextField ;
  private javax.swing.ButtonGroup syringebuttonGroup ;
  private javax.swing.JRadioButton twoSyringeRadioButton ;
  // End of variables declaration
}
