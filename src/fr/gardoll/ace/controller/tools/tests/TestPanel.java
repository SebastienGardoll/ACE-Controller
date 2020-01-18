package fr.gardoll.ace.controller.tools.tests ;

import java.awt.Color ;
import java.util.ArrayList ;
import java.util.List ;

import javax.swing.text.BadLocationException ;
import javax.swing.text.DefaultHighlighter ;
import javax.swing.text.Highlighter ;

import org.apache.commons.lang3.tuple.ImmutablePair ;
import org.apache.commons.lang3.tuple.Pair ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.Action ;
import fr.gardoll.ace.controller.core.ActionType ;
import fr.gardoll.ace.controller.core.Log ;
import fr.gardoll.ace.controller.tools.tests.AsbtractTest.TestControl ;
import fr.gardoll.ace.controller.ui.AbstractCancelableJPanelObserver ;

public class TestPanel extends AbstractCancelableJPanelObserver
{
  private static final long serialVersionUID = 3863282861526799243L ;

  private static final Logger _LOG = Log.HIGH_LEVEL ;

  private static final DefaultHighlighter.DefaultHighlightPainter _HIGHLIGHTER_PAINTER = new DefaultHighlighter.DefaultHighlightPainter(
      Color.YELLOW) ;

  private final TestControl _ctrl ;
  private final List<Pair<Integer, Integer>> _highlightingData = new ArrayList<>() ;

  public TestPanel(TestControl ctrl, List<Operation> operations)
  {
    super(ctrl) ;
    this._ctrl = ctrl ;
    initComponents() ;
    initCustom(operations) ;
  }

  private void initCustom(List<Operation> operations)
  {
    StringBuilder sb = new StringBuilder() ;
    int offset = 0 ;

    for (Operation operation : operations)
    {
      sb.append(operation.name) ;
      sb.append('\n') ;
      int start = offset ;
      int end = start + operation.name.length() ;
      offset = end + 1 ;
      Pair<Integer, Integer> p = new ImmutablePair<>(start, end) ;
      this._highlightingData.add(p) ;
    }

    sb.append("\nDONE") ;
    int start = offset + 1 ;
    int end = start + "DONE".length() ;
    Pair<Integer, Integer> p = new ImmutablePair<>(start, end) ;
    this._highlightingData.add(p) ;

    this.operationTextPane.setText(sb.toString()) ;
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

    operationPanel = new javax.swing.JPanel() ;
    textScrollPanel = new javax.swing.JScrollPane() ;
    operationTextPane = new javax.swing.JTextPane() ;
    controlPanel = new javax.swing.JPanel() ;
    runCancelButton = new javax.swing.JButton() ;
    filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
        new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767)) ;
    closeButton = new javax.swing.JButton() ;

    setPreferredSize(new java.awt.Dimension(780, 460)) ;
    setLayout(new java.awt.GridBagLayout()) ;

    operationPanel
        .setBorder(javax.swing.BorderFactory.createTitledBorder("Operations")) ;
    operationPanel.setLayout(new java.awt.GridBagLayout()) ;

    operationTextPane.setEditable(false) ;
    textScrollPanel.setViewportView(operationTextPane) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    operationPanel.add(textScrollPanel, gridBagConstraints) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    add(operationPanel, gridBagConstraints) ;

    controlPanel.setLayout(new java.awt.GridBagLayout()) ;

    runCancelButton.setText("run") ;
    runCancelButton.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        runCancelButtonActionPerformed(evt) ;
      }
    }) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    controlPanel.add(runCancelButton, gridBagConstraints) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 1 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    controlPanel.add(filler1, gridBagConstraints) ;

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
    gridBagConstraints.gridx = 2 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    controlPanel.add(closeButton, gridBagConstraints) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 1 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 0.1 ;
    gridBagConstraints.weighty = 0.1 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    add(controlPanel, gridBagConstraints) ;
  }// </editor-fold>

  private void closeButtonActionPerformed(java.awt.event.ActionEvent evt)
  {
    _LOG.debug("**** event close ****") ;
    this.close() ;
  }

  private void runCancelButtonActionPerformed(java.awt.event.ActionEvent evt)
  {
    if (this._isStartEnable)
    {
      _LOG.debug("**** event run ****") ;
      this._ctrl.start() ;
    }
    else
    {
      this._ctrl.cancel() ;
    }
  }

  // Variables declaration - do not modify
  private javax.swing.JButton closeButton ;
  private javax.swing.JPanel controlPanel ;
  private javax.swing.Box.Filler filler1 ;
  private javax.swing.JTextPane operationTextPane ;
  private javax.swing.JButton runCancelButton ;
  private javax.swing.JScrollPane textScrollPanel ;
  private javax.swing.JPanel operationPanel ;
  // End of variables declaration

  @Override
  protected void enableReinitControl(boolean isEnable)
  {
    // Nothing to do.
  }

  @Override
  protected void enableStartControl(boolean isEnable)
  {
    runCancelButton.setEnabled(isEnable || this._isCancelEnable) ;
    if (isEnable)
    {
      runCancelButton.setText("run") ;
    }
    else
    {
      runCancelButton.setText("cancel") ;
    }
  }

  @Override
  protected void enableCancelControl(boolean isEnable)
  {
    runCancelButton.setEnabled(isEnable || this._isStartEnable) ;
    if (isEnable)
    {
      runCancelButton.setText("cancel") ;
    }
    else
    {
      runCancelButton.setText("run") ;
    }
  }

  @Override
  protected void displayToUserLogSys(String msg)
  {
    // Nothing to do.
  }

  @Override
  protected void processAction(Action action)
  {
    if (action.type == ActionType.TEST)
    {
      int index = (int) action.data.get() ;
      this.highlightOperation(index) ;
    }
  }

  @Override
  protected void enableCloseControl(boolean isEnable)
  {
    closeButton.setEnabled(isEnable) ;
  }

  private void highlightOperation(int index)
  {
    try
    {
      Highlighter highlighter = this.operationTextPane.getHighlighter() ;
      highlighter.removeAllHighlights() ;

      if (index < 0) // Highlights the done marker.
      {
        index = this._highlightingData.size() - 1 ;
      }

      if (index < this._highlightingData.size())
      {
        Pair<Integer, Integer> p = this._highlightingData.get(index) ;
        int start = p.getLeft() ;
        int end = p.getRight() ;
        highlighter.addHighlight(start, end, _HIGHLIGHTER_PAINTER) ;
      }
    }
    catch (BadLocationException e)
    {
      // Should never happen.
    }
  }
}