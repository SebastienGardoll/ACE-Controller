package fr.gardoll.ace.controller.tools.extraction;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.AbstractThreadControl ;
import fr.gardoll.ace.controller.core.AbstractToolControl ;
import fr.gardoll.ace.controller.core.CancellationException ;
import fr.gardoll.ace.controller.core.InitializationException ;

public class ExtractionThreadControl extends AbstractThreadControl
{
  public ExtractionThreadControl(AbstractToolControl toolCtrl)
  {
    super(toolCtrl) ;
  }

  private static final Logger _LOG = LogManager.getLogger(ExtractionThreadControl.class.getName());

  @Override
  protected void threadLogic() throws InterruptedException,
      CancellationException, InitializationException, Exception
  {
    /*
    To be computed
    int previousTime;
    int previousNumEv;
    int previousVolume;
    boolean isLastSequence;
    */
  }
}
