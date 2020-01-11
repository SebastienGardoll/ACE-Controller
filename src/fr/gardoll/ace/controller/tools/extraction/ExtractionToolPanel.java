package fr.gardoll.ace.controller.tools.extraction ;

import java.awt.event.AdjustmentEvent ;
import java.awt.event.AdjustmentListener ;
import java.util.Optional ;

import javax.swing.text.DefaultCaret ;

import org.apache.commons.lang3.tuple.Pair ;
import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.Action ;
import fr.gardoll.ace.controller.protocol.Sequence ;
import fr.gardoll.ace.controller.ui.AbstractPausableJPanelObserver ;
import fr.gardoll.ace.controller.ui.UiUtils ;

public class ExtractionToolPanel extends AbstractPausableJPanelObserver
{
  private static final Logger _LOG = LogManager
      .getLogger(ExtractionToolPanel.class.getName()) ;

  private static final long serialVersionUID = -4485547824494200127L ;

  private final ExtractionToolControl _ctrl ;

  private int _maxColumn ;
  private int _maxSequence ;

  private javax.swing.text.DefaultCaret caret ;
  private javax.swing.BoundedRangeModel model ;

  public ExtractionToolPanel(ExtractionToolControl ctrl)
  {
    super(ctrl) ;
    this._ctrl = ctrl ;
    initComponents() ;
    initCustom() ;
  }

  private void initCustom()
  {
    setupSmartScrolling() ;
  }

  private void setupSmartScrolling()
  {
    caret = (javax.swing.text.DefaultCaret) this.logTextArea.getCaret() ;

    javax.swing.JScrollBar scrollBar = this.logTextScrollPane
        .getVerticalScrollBar() ;
    model = scrollBar.getModel() ;
    scrollBar.addAdjustmentListener(new AdjustmentListener()
    {
      @Override
      public void adjustmentValueChanged(AdjustmentEvent e)
      {
        if (model.getValue() == model.getMaximum() - model.getExtent())
        {
          caret.setDot(logTextArea.getText().length()) ;
          caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE) ;
        }
        else
        {
          caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE) ;
        }
      }
    }) ;
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

    upperPanel = new javax.swing.JPanel() ;
    statusPanel = new javax.swing.JPanel() ;
    actionPanel = new javax.swing.JPanel() ;
    actionValueLabel = new javax.swing.JLabel() ;
    sequencePanel = new javax.swing.JPanel() ;
    sequenceValueLabel = new javax.swing.JLabel() ;
    columnPanel = new javax.swing.JPanel() ;
    columnValueLabel = new javax.swing.JLabel() ;
    carouselControlPanel = new javax.swing.JPanel() ;
    turnLeftButton = new javax.swing.JButton() ;
    turnRightButton = new javax.swing.JButton() ;
    logPanel = new javax.swing.JPanel() ;
    logTextScrollPane = new javax.swing.JScrollPane() ;
    logTextArea = new javax.swing.JTextArea() ;
    buttonPanel = new javax.swing.JPanel() ;
    startCancelButton = new javax.swing.JButton() ;
    pauseToggleButton = new javax.swing.JToggleButton() ;
    closeButton = new javax.swing.JButton() ;
    buttonFiller = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
        new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0)) ;

    setPreferredSize(new java.awt.Dimension(780, 460)) ;
    setLayout(new java.awt.GridBagLayout()) ;

    upperPanel.setLayout(new java.awt.GridBagLayout()) ;

    statusPanel.setLayout(new java.awt.GridBagLayout()) ;

    actionPanel
        .setBorder(javax.swing.BorderFactory.createTitledBorder("Action")) ;

    actionValueLabel.setText("-") ;
    actionPanel.add(actionValueLabel) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    statusPanel.add(actionPanel, gridBagConstraints) ;

    sequencePanel
        .setBorder(javax.swing.BorderFactory.createTitledBorder("Sequence")) ;

    sequenceValueLabel.setText("-") ;
    sequencePanel.add(sequenceValueLabel) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 1 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    statusPanel.add(sequencePanel, gridBagConstraints) ;

    columnPanel
        .setBorder(javax.swing.BorderFactory.createTitledBorder("Column")) ;

    columnValueLabel.setText("-") ;
    columnPanel.add(columnValueLabel) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 2 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    statusPanel.add(columnPanel, gridBagConstraints) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 0.1 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    upperPanel.add(statusPanel, gridBagConstraints) ;

    carouselControlPanel.setBorder(
        javax.swing.BorderFactory.createTitledBorder("Carousel Controls")) ;
    carouselControlPanel.setLayout(new java.awt.GridBagLayout()) ;

    turnLeftButton.setText("turn Left") ;
    turnLeftButton.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        turnLeftButtonActionPerformed(evt) ;
      }
    }) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(35, 25, 35, 25) ;
    carouselControlPanel.add(turnLeftButton, gridBagConstraints) ;

    turnRightButton.setText("turn Right") ;
    turnRightButton.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        turnRightButtonActionPerformed(evt) ;
      }
    }) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(35, 25, 35, 25) ;
    carouselControlPanel.add(turnRightButton, gridBagConstraints) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 1 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 0.1 ;
    gridBagConstraints.weighty = 0.1 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    upperPanel.add(carouselControlPanel, gridBagConstraints) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 0.1 ;
    gridBagConstraints.weighty = 0.1 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    add(upperPanel, gridBagConstraints) ;

    logPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Log")) ;
    logPanel.setLayout(new java.awt.GridBagLayout()) ;

    logTextScrollPane.setHorizontalScrollBarPolicy(
        javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS) ;
    logTextScrollPane.setVerticalScrollBarPolicy(
        javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS) ;

    logTextArea.setEditable(false) ;
    logTextArea.setColumns(20) ;
    logTextArea.setLineWrap(true) ;
    logTextArea.setRows(5) ;
    logTextScrollPane.setViewportView(logTextArea) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.ipadx = 221 ;
    gridBagConstraints.ipady = 61 ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    logPanel.add(logTextScrollPane, gridBagConstraints) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 1 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    add(logPanel, gridBagConstraints) ;

    buttonPanel.setLayout(new java.awt.GridBagLayout()) ;

    startCancelButton.setText("start") ;
    startCancelButton.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        startCancelButtonActionPerformed(evt) ;
      }
    }) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    buttonPanel.add(startCancelButton, gridBagConstraints) ;

    pauseToggleButton.setText("pause") ;
    pauseToggleButton.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        pauseToggleButtonActionPerformed(evt) ;
      }
    }) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 1 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    buttonPanel.add(pauseToggleButton, gridBagConstraints) ;

    closeButton.setText("close") ;
    closeButton.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        closeButtonActionPerformed(evt) ;
      }
    }) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 3 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    buttonPanel.add(closeButton, gridBagConstraints) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 2 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    buttonPanel.add(buttonFiller, gridBagConstraints) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 2 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 0.2 ;
    gridBagConstraints.weighty = 0.2 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    add(buttonPanel, gridBagConstraints) ;
  }// </editor-fold>

  private void startCancelButtonActionPerformed(java.awt.event.ActionEvent evt)
  {
    _LOG.debug("**** event start ****") ;

    if (this._isStartEnable)
    {
      try
      {
        ExtractionConfigurationDialog diag = new ExtractionConfigurationDialog(
            UiUtils.getParentDialog(this), "extraction configuration", true) ;
        diag.setVisible(true) ;

        Optional<InitSession> initSession = diag.getInitSession() ;

        if (initSession.isPresent())
        {
          InitSession init = initSession.get() ;

          this._maxColumn = init.nbColonne ;
          this._maxSequence = init.protocol.nbMaxSequence ;

          this._ctrl.start(init) ;
        }
        else
        {
          // Nothing to do.
        }
      }
      catch(Exception e)
      {
        String msg = "extraction dialog has crashed";
        _LOG.fatal(msg, e);
        this.reportError(msg, e);
      }
    }
    else
    {
      this._ctrl.cancel() ;
    }
  }

  private void pauseToggleButtonActionPerformed(java.awt.event.ActionEvent evt)
  {
    _LOG.debug("**** event pause/resume ****") ;
    this.pauseAndResume() ;
  }

  private void closeButtonActionPerformed(java.awt.event.ActionEvent evt)
  {
    _LOG.debug("**** event close ****") ;
    this.close() ;
  }

  private void turnLeftButtonActionPerformed(java.awt.event.ActionEvent evt)
  {
    _LOG.debug("**** event turn left ****") ;
    this._ctrl.turnCarouselToLeft() ;
  }

  private void turnRightButtonActionPerformed(java.awt.event.ActionEvent evt)
  {
    _LOG.debug("**** event turn right ****") ;
    this._ctrl.turnCarouselToRight() ;
  }

  // Variables declaration - do not modify
  private javax.swing.JPanel actionPanel ;
  private javax.swing.JLabel actionValueLabel ;
  private javax.swing.Box.Filler buttonFiller ;
  private javax.swing.JPanel buttonPanel ;
  private javax.swing.JPanel carouselControlPanel ;
  private javax.swing.JButton closeButton ;
  private javax.swing.JPanel columnPanel ;
  private javax.swing.JLabel columnValueLabel ;
  private javax.swing.JPanel logPanel ;
  private javax.swing.JTextArea logTextArea ;
  private javax.swing.JScrollPane logTextScrollPane ;
  private javax.swing.JToggleButton pauseToggleButton ;
  private javax.swing.JPanel sequencePanel ;
  private javax.swing.JLabel sequenceValueLabel ;
  private javax.swing.JButton startCancelButton ;
  private javax.swing.JPanel statusPanel ;
  private javax.swing.JButton turnLeftButton ;
  private javax.swing.JButton turnRightButton ;
  private javax.swing.JPanel upperPanel ;
  // End of variables declaration

  @Override
  protected void enablePauseControl(boolean isEnable)
  {
    pauseToggleButton.setEnabled(isEnable || this._isResumeEnable) ;
    pauseToggleButton.setSelected(!isEnable) ;
    if (isEnable)
    {
      pauseToggleButton.setText("pause") ;
    }
    else
    {
      pauseToggleButton.setText("resume") ;
    }
  }

  @Override
  protected void enableResumeControl(boolean isEnable)
  {
    pauseToggleButton.setEnabled(isEnable || this._isPauseEnable) ;
    pauseToggleButton.setSelected(isEnable) ;

    if (isEnable)
    {
      pauseToggleButton.setText("resume") ;
    }
    else
    {
      pauseToggleButton.setText("pause") ;
    }
  }

  @Override
  protected void enableCarouselControl(boolean isEnable)
  {
    turnLeftButton.setEnabled(isEnable) ;
    turnRightButton.setEnabled(isEnable) ;
  }

  @Override
  protected void enableReinitControl(boolean isEnable)
  {
    // Reinit feature is not implemented, nothing to do.
  }

  @Override
  protected void enableStartControl(boolean isEnable)
  {
    startCancelButton.setEnabled(isEnable || this._isCancelEnable) ;
    if (isEnable)
    {
      startCancelButton.setText("start") ;
    }
    else
    {
      startCancelButton.setText("cancel") ;
    }
  }

  @Override
  protected void enableCancelControl(boolean isEnable)
  {
    startCancelButton.setEnabled(isEnable || this._isStartEnable) ;
    if (isEnable)
    {
      startCancelButton.setText("cancel") ;
    }
    else
    {
      startCancelButton.setText("start") ;
    }
  }

  @Override
  protected void displayToUserLogSys(String msg)
  {
    this.logTextArea.append(msg) ;
  }

  @Override
  protected void enableCloseControl(boolean isEnable)
  {
    closeButton.setEnabled(isEnable) ;
  }

  @Override
  protected void processAction(Action action)
  {
    super.processAction(action) ;

    switch (action.type)
    {
      case RESUME:
      {
        this.actionValueLabel.setText("resuming") ;
        break ;
      }

      case WAIT_CANCEL:
      {
        this.actionValueLabel.setText("cancelling") ;
        break ;
      }

      case WAIT_PAUSE:
      {
        this.actionValueLabel.setText("user pause") ;
        break ;
      }

      case REINIT:
      {
        this.actionValueLabel.setText("reinitializing") ;
        break ;
      }

      case REINIT_DONE:
      {
        this.actionValueLabel.setText("reinitialization done") ;
        this.columnValueLabel.setText("-") ;
        this.sequenceValueLabel.setText("-") ;
        break ;
      }

      case CLOSING:
      {
        this.actionValueLabel.setText("closing") ;
        break ;
      }

      case SEQUENCE_START:
      {
        @SuppressWarnings("unchecked")
        Pair<Integer, Sequence> pair = (Pair<Integer, Sequence>) action.data
            .get() ;
        Integer sequenceIndex = pair.getLeft() ;
        String text = String.format("%s/%s", sequenceIndex, this._maxSequence) ;
        this.sequenceValueLabel.setText(text) ;
        break ;
      }

      case SEQUENCE_AWAIT:
      {
        this.actionValueLabel.setText("waiting") ;
        break ;
      }

      case NEXT_SEQUENCE_PREP:
      {
        this.actionValueLabel.setText("preparing next seq") ;
        break ;
      }

      case SEQUENCE_PAUSE_START:
      {
        this.actionValueLabel.setText("seq pause") ;
        break ;
      }

      case COLUMN_DIST_START:
      {
        this.actionValueLabel.setText("distributing") ;
        String text = String.format("%s/%s", action.data.get(),
            this._maxColumn) ;
        this.columnValueLabel.setText(text) ;
        break ;
      }

      case PREPARING:
      {
        this.actionValueLabel.setText("preparing seq") ;
        break ;
      }

      case POST_LAST_SEQ:
      {
        this.actionValueLabel.setText("rincing") ;
        this.columnValueLabel.setText("-") ;
        this.sequenceValueLabel.setText("-") ;
        break ;
      }

      case POST_SESSION:
      {
        this.actionValueLabel.setText("home") ;
        break ;
      }

      case SESSION_DONE:
      {
        this.actionValueLabel.setText("done") ;
        break ;
      }

      default:
      {
        // Nothing to do.
        return ;
      }
    }
  }
}
