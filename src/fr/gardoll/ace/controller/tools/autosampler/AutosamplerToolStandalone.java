package fr.gardoll.ace.controller.tools.autosampler;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.ui.AbstractJFrame ;

public class AutosamplerToolStandalone extends AbstractJFrame
{
  private static final Logger _LOG = LogManager.getLogger(AutosamplerToolStandalone.class.getName());
  private static final long serialVersionUID = -6062838678688858409L ;
  private final AutosamplerToolControl _ctrl ;
  private final AutosamplerToolPanel _toolPanel ;

  public AutosamplerToolStandalone(AutosamplerToolPanel toolPanel,
                                   AutosamplerToolControl ctrl)
  {
    super(toolPanel) ;
    this._ctrl = ctrl;
    this._toolPanel = toolPanel;
  }

  @Override
  protected void onCloseOperation()
  {
    try
    {
      this._ctrl.close();
    }
    catch (InterruptedException e)
    {
      String msg = "error while closing the tool";
      _LOG.error(String.format("%s: %s", msg, e.getMessage()));
      this._toolPanel.reportError(msg, e);
    }
  }
  
  // TODO: to be tested.
  public static void main(String[] args)
  {
    try(ParametresSession parametresSession = ParametresSession.getInstance())
    {
      AutosamplerToolControl ctrl = new AutosamplerToolControl(parametresSession);
      AutosamplerToolPanel toolPanel = new AutosamplerToolPanel(ctrl);
      ctrl.addControlPanel(toolPanel);
      AutosamplerToolStandalone tool = new AutosamplerToolStandalone(toolPanel, ctrl);
      tool.setVisible(true);
    }
    catch (InitializationException | InterruptedException e)
    {
      String msg = "error while initialisating the tool";
      _LOG.error(String.format("%s: %s", msg, e.getMessage()));
      Utils.reportError(msg, e);
    }
  }

}
