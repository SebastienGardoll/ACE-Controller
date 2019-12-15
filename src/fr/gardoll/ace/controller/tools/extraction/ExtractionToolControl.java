package fr.gardoll.ace.controller.tools.extraction;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.AbstractPausableToolControl ;
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.ParametresSession ;

public class ExtractionToolControl extends AbstractPausableToolControl
{
  private static final Logger _LOG = LogManager.getLogger(ExtractionToolControl.class.getName());
  
  public ExtractionToolControl(ParametresSession parametresSession)
                            throws InitializationException, InterruptedException
  {
    super(parametresSession, true, true, true) ;
  }

  void start(InitSession initSession)
  {
    
    // TODO check initSession object 
    
    try
    {
      ExtractionThreadControl thread = new ExtractionThreadControl(this, initSession);
      _LOG.debug("starting extraction thread");
      this.start(thread);
    }
    catch(Exception e)
    {
      String msg = "extraction thread start has crashed";
      _LOG.fatal(msg, e);
      this.handleException(msg, e);
    }
  }
  
  @Override
  protected void closeOperations() throws InterruptedException
  {
    // TODO
  }
}
