package fr.gardoll.ace.controller.tools.settings ;

import java.nio.file.Path ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.commons.lang3.tuple.ImmutablePair ;
import org.apache.commons.lang3.tuple.Pair ;

import fr.gardoll.ace.controller.core.ConfigurationException ;
import fr.gardoll.ace.controller.core.Names ;
import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.core.Utils ;

public class MiscellaneousPanel extends javax.swing.JPanel implements Panel
{
  private static final long serialVersionUID = 7507890290387055415L ;
  public static final String NAME = "miscellaneous" ;

  /**
   * Creates new form MiscellaneousPanel
   */
  public MiscellaneousPanel()
  {
    initComponents() ;
    load();
  }
  
  private void load()
  {
    ParametresSession session = ParametresSession.getInstance();
    boolean isDebug = session.isDebug();
    boolean isFullScreen = session.isFullScreen();
    
    this.debugModeCheckBox.setSelected(isDebug);
    this.fullScreenModeCheckBox.setSelected(isFullScreen);
  }

  @Override
  public void save() throws ConfigurationException
  {
    String debugModeValue = (this.debugModeCheckBox.isSelected())?Names.TRUE:Names.FALSE;
    ImmutablePair<String, String> debugMode = null;
    debugMode = new ImmutablePair<>(Names.SAC_IS_DEBUG, debugModeValue);
    
    String FullScreenModeValue = (this.fullScreenModeCheckBox.isSelected())?Names.TRUE:Names.FALSE;
    ImmutablePair<String, String> FullScreenMode = null;
    FullScreenMode = new ImmutablePair<>(Names.SAC_IS_FULL_SCREEN, FullScreenModeValue);
    
    List<Pair<String, String>> items = new ArrayList<>();
    items.add(debugMode);
    items.add(FullScreenMode);
    
    Map<String, List<Pair<String, String>>> sectionKeyValues = null;
    sectionKeyValues = new HashMap<>();
    
    sectionKeyValues.put(Names.SEC_ACE_CONTROLLER, items);
    
    ParametresSession session = ParametresSession.getInstance();
    Path configurationFilePath = session.getConfigurationFilePath();
    
    Utils.persistPropertyData(configurationFilePath, sectionKeyValues);
  }
  
  @Override
  public String getName()
  {
    return MiscellaneousPanel.NAME;
  }

  @Override
  public void check() throws ConfigurationException
  {
    // Nothing to do.
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

    miscModePanel = new javax.swing.JPanel() ;
    fullScreenModeCheckBox = new javax.swing.JCheckBox() ;
    debugModeCheckBox = new javax.swing.JCheckBox() ;

    miscModePanel
        .setBorder(javax.swing.BorderFactory.createTitledBorder("Modes")) ;
    miscModePanel.setLayout(new java.awt.GridBagLayout()) ;

    fullScreenModeCheckBox.setText("full screen mode") ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.ipadx = 46 ;
    gridBagConstraints.ipady = 39 ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    miscModePanel.add(fullScreenModeCheckBox, gridBagConstraints) ;

    debugModeCheckBox.setText("debug mode") ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 1 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.ipadx = 73 ;
    gridBagConstraints.ipady = 39 ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    miscModePanel.add(debugModeCheckBox, gridBagConstraints) ;

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this) ;
    this.setLayout(layout) ;
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
            .addGroup(layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                    layout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(miscModePanel,
                            javax.swing.GroupLayout.PREFERRED_SIZE, 244,
                            javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))) ;
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
            .addGroup(layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                    layout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(miscModePanel,
                            javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE,
                            javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))) ;
  }// </editor-fold>

  // Variables declaration - do not modify
  private javax.swing.JCheckBox debugModeCheckBox ;
  private javax.swing.JCheckBox fullScreenModeCheckBox ;
  private javax.swing.JPanel miscModePanel ;
  // End of variables declaration
}
