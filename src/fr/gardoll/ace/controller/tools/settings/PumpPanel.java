package fr.gardoll.ace.controller.tools.settings ;

import java.text.ParseException ;

import javax.swing.SpinnerNumberModel ;

import fr.gardoll.ace.controller.core.ConfigurationException ;
import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.pump.PousseSeringue ;
import fr.gardoll.ace.controller.ui.TextFieldRealNumber ;

//TODO check syringe volume against rince volume.
public class PumpPanel extends javax.swing.JPanel implements Panel
{
  private static final long serialVersionUID = -4086064621880324437L ;
  
  private static final double _DEFAULT_MAX_SYRINGE_VOLUME   = 100.; // in mL
  private static final double _DEFAULT_MAX_PUMP_MAX_RATE    = 15.;  // in mL/min
  private static final double _DEFAULT_MAX_RINCE_VOLUME     = 100.; // in mL
  private static final double _DEFAULT_MAX_SYRINGE_DIAMETER = 100.; // in mm
  
  private static final double _DEFAULT_MIN_PUMP_MAX_RATE    = 0.1; // in mL/min
  private static final double _DEFAULT_MIN_RINCE_VOLUME     = 0.1; // in mL
  private static final double _DEFAULT_MIN_SYRINGE_DIAMETER = 1.; // in mm
  
  private static final int _DEFAULT_MIN_RINCE_NB = 1;
  private static final int _DEFAULT_MAX_RINCE_NB = 10;
  
  private static final double _MIN_SYRINGE_VOLUME = 
      PousseSeringue.volumeAjustement() + PousseSeringue.volumeSecurite() ;
  
  public static final String NAME = "pump" ;

  private TextFieldRealNumber syringeVolTextFieldRealNumber ;

  private TextFieldRealNumber pumpMaxRateTextFieldRealNumber ;

  private TextFieldRealNumber rinceVolTextFieldRealNumber ;

  private TextFieldRealNumber syringeDiaTextFieldRealNumber ;

  /**
   * Creates new form PumpPanel
   */
  public PumpPanel()
  {
    initComponents() ;
    initCustom() ;
    load() ;
  }

  private void load()
  {
    ParametresSession session = ParametresSession.getInstance();
    
    String pumpMaxRateValue = String.valueOf(session.debitMaxPousseSeringue());
    this.pumpMaxRateTextField.setText(pumpMaxRateValue);
    
    this.oneSyringeRadioButton.setSelected(session.nbSeringue() == 1);
    this.twoSyringeRadioButton.setSelected(session.nbSeringue() == 2);
    
    String rinceVolumeValue = String.valueOf(session.volumeRincage());
    this.rinceVolumeTextField.setText(rinceVolumeValue);
    
    this.rinceNumberSpinner.setValue(session.nbRincage());
    
    String syringeDiameterValue = String.valueOf(session.diametreSeringue());
    this.syringeDiameterTextField.setText(syringeDiameterValue);
    
    String syringeVolumeValue = String.valueOf(session.volumeMaxSeringue());
    this.syringeVolumeTextField.setText(syringeVolumeValue);
  }

  @Override
  public void save() throws ConfigurationException
  {
    // TODO Auto-generated method stub

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
      this.rinceNumberSpinner.commitEdit();
    }
    catch (ParseException e)
    {
      throw new ConfigurationException("failed to fetch the number of rincing", e);
    }
    
    int nbRince            = (int) this.rinceNumberSpinner.getValue();
    double rinceVolume     =       this.rinceVolTextFieldRealNumber.parse();
    double syringeVolume   =       this.syringeVolTextFieldRealNumber.parse();
    double syringeDiameter =       this.syringeDiaTextFieldRealNumber.parse();
    double pumpMaxRate     =       this.pumpMaxRateTextFieldRealNumber.parse();
    int nbSyringe          =      (this.oneSyringeRadioButton.isSelected())?1:2;
    
    // Basic checking.
    
    if(nbSyringe !=1 || nbSyringe != 2)
    {
      String msg = String.format("the number of syringe must be 1 or 2 not '%s'", nbSyringe);
      throw new ConfigurationException(msg);
    }
    
    if(pumpMaxRate < _DEFAULT_MIN_PUMP_MAX_RATE)
    {
      String msg = String.format("the pump maximum rate (got '%s') cannot be less than %s",
          pumpMaxRate, _DEFAULT_MIN_PUMP_MAX_RATE);
      throw new ConfigurationException(msg);
    }
    
    if(syringeDiameter < _DEFAULT_MIN_SYRINGE_DIAMETER)
    {
      String msg = String.format("the syringe diameter (got '%s') cannot be less than %s",
          syringeDiameter, _DEFAULT_MIN_SYRINGE_DIAMETER);
      throw new ConfigurationException(msg);
    }
    
    if(rinceVolume < _DEFAULT_MIN_RINCE_VOLUME)
    {
      String msg = String.format("the rinse volume (got '%s') cannot be less than %s",
          rinceVolume, _DEFAULT_MIN_RINCE_VOLUME);
      throw new ConfigurationException(msg);
    }
    
    if(syringeVolume <= _MIN_SYRINGE_VOLUME)
    {
      String msg = String.format("the volume of the syringe (got '%s') must be greater than %s",
          syringeVolume, _MIN_SYRINGE_VOLUME);
      throw new ConfigurationException(msg);
    }
    
    if(nbRince < _DEFAULT_MIN_RINCE_NB)
    {
      String msg = String.format("the number of rinses (got '%s') cannot be less than %s",
          nbRince, _DEFAULT_MIN_RINCE_NB);
      throw new ConfigurationException(msg);
    }
    
    if(pumpMaxRate > _DEFAULT_MAX_PUMP_MAX_RATE)
    {
      String msg = String.format("the pump maximum rate (got '%s') cannot be greater than %s",
          pumpMaxRate, _DEFAULT_MAX_PUMP_MAX_RATE);
      throw new ConfigurationException(msg);
    }
    
    if(syringeDiameter > _DEFAULT_MAX_SYRINGE_DIAMETER)
    {
      String msg = String.format("the syringe diameter (got '%s') cannot be greate than %s",
          syringeDiameter, _DEFAULT_MAX_SYRINGE_DIAMETER);
      throw new ConfigurationException(msg);
    }
    
    if(rinceVolume > _DEFAULT_MAX_RINCE_VOLUME)
    {
      String msg = String.format("the rinse volume (got '%s') cannot be greater than %s",
          rinceVolume, _DEFAULT_MAX_RINCE_VOLUME);
      throw new ConfigurationException(msg);
    }
    
    if(syringeVolume > _DEFAULT_MAX_SYRINGE_VOLUME)
    {
      String msg = String.format("the volume of the syringe (got '%s') cannot be greater than %s",
          syringeVolume, _DEFAULT_MAX_SYRINGE_VOLUME);
      throw new ConfigurationException(msg);
    }
    
    if(nbRince > _DEFAULT_MAX_RINCE_NB)
    {
      String msg = String.format("the number of rinses (got '%s') cannot be greater than %s",
          nbRince, _DEFAULT_MAX_RINCE_NB);
      throw new ConfigurationException(msg);
    }
    
    // Consistency checking.
    
    if(false == Utils.isDividableBy250(rinceVolume))
    {
      String msg = String.format("the volume of rinse must be dividable by 0.25 (got '%s')", rinceVolume);
      throw new ConfigurationException(msg);
    }
    
    if(false == Utils.isDividableBy250(pumpMaxRate))
    {
      String msg = String.format("the pump rate must be dividable by 0.25 (got '%s')", pumpMaxRate);
      throw new ConfigurationException(msg);
    }
    
    if(syringeVolume < rinceVolume)
    {
      String msg = String.format("the volume of rinse (got '%s') cannot be greater than the volume of the syringe (got '%s')",
          rinceVolume, syringeVolume);
      throw new ConfigurationException(msg);
    }
    
    {
      int inherentPumpMaxRate = PousseSeringue.debitMaxIntrinseque(syringeDiameter);
      
      if(pumpMaxRate > inherentPumpMaxRate)
      {
        String msg = String.format("the maximum rate of the pump (got '%s') cannot be greater than %s (computed for a syringe diameter of %s)", 
            pumpMaxRate, inherentPumpMaxRate, syringeDiameter);
        throw new ConfigurationException(msg);
      }
    }
  }

  private void initCustom()
  {
    this.syringebuttonGroup.add(this.oneSyringeRadioButton) ;
    this.syringebuttonGroup.add(this.twoSyringeRadioButton) ;

    this.pumpMaxRateTextFieldRealNumber = new TextFieldRealNumber("pump max rate",
        this.pumpMaxRateTextField, _DEFAULT_MIN_PUMP_MAX_RATE, _DEFAULT_MAX_PUMP_MAX_RATE) ;

    this.rinceVolTextFieldRealNumber = new TextFieldRealNumber("rince volume",
        this.rinceVolumeTextField, _DEFAULT_MIN_RINCE_VOLUME, _DEFAULT_MAX_RINCE_VOLUME) ;

    this.syringeDiaTextFieldRealNumber = new TextFieldRealNumber("syringe diameter",
        this.syringeDiameterTextField, _DEFAULT_MIN_SYRINGE_DIAMETER, _DEFAULT_MAX_SYRINGE_DIAMETER) ;
    
    this.syringeVolTextFieldRealNumber = new TextFieldRealNumber("syringe volume",
        this.syringeVolumeTextField, _MIN_SYRINGE_VOLUME, _DEFAULT_MAX_SYRINGE_VOLUME) ;
    
    SpinnerNumberModel nbRinceModel = new SpinnerNumberModel(
        1, _DEFAULT_MIN_RINCE_NB, _DEFAULT_MAX_RINCE_NB, 1) ;
    this.rinceNumberSpinner.setModel(nbRinceModel) ;
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
    rinceSettingsPanel = new javax.swing.JPanel() ;
    rinceVolumePanel = new javax.swing.JPanel() ;
    rinceVolumeTextField = new javax.swing.JTextField() ;
    rinceNumberPanel = new javax.swing.JPanel() ;
    rinceNumberSpinner = new javax.swing.JSpinner() ;
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

    rinceSettingsPanel
        .setBorder(javax.swing.BorderFactory.createTitledBorder("Rincing")) ;
    rinceSettingsPanel.setLayout(new java.awt.GridBagLayout()) ;

    rinceVolumePanel.setBorder(
        javax.swing.BorderFactory.createTitledBorder("Volume (mL)")) ;
    rinceVolumePanel.setLayout(new java.awt.GridBagLayout()) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    rinceVolumePanel.add(rinceVolumeTextField, gridBagConstraints) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    rinceSettingsPanel.add(rinceVolumePanel, gridBagConstraints) ;

    rinceNumberPanel.setBorder(
        javax.swing.BorderFactory.createTitledBorder("Number per cycle")) ;
    rinceNumberPanel.setLayout(new java.awt.GridBagLayout()) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    rinceNumberPanel.add(rinceNumberSpinner, gridBagConstraints) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 1 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    rinceSettingsPanel.add(rinceNumberPanel, gridBagConstraints) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 1 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    add(rinceSettingsPanel, gridBagConstraints) ;

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
  private javax.swing.JPanel rinceNumberPanel ;
  private javax.swing.JSpinner rinceNumberSpinner ;
  private javax.swing.JPanel rinceSettingsPanel ;
  private javax.swing.JPanel rinceVolumePanel ;
  private javax.swing.JTextField rinceVolumeTextField ;
  private javax.swing.JPanel syringeDiameterPanel ;
  private javax.swing.JTextField syringeDiameterTextField ;
  private javax.swing.JPanel syringeSettingsPanel ;
  private javax.swing.JPanel syringeVolumePanel ;
  private javax.swing.JTextField syringeVolumeTextField ;
  private javax.swing.ButtonGroup syringebuttonGroup ;
  private javax.swing.JRadioButton twoSyringeRadioButton ;
  // End of variables declaration
}
