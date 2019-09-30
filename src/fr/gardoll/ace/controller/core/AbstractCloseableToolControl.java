package fr.gardoll.ace.controller.core;

import java.util.Optional ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

public abstract class AbstractCloseableToolControl extends AbstractToolControl
{
  private static final Logger _LOG = LogManager.getLogger(AbstractCloseableToolControl.class.getName());
  
  public AbstractCloseableToolControl(ParametresSession parametresSession,
                                  boolean hasPump, boolean hasAutosampler,
                                  boolean hasValves)
      throws InitializationException, InterruptedException
  {
    super(parametresSession, hasPump, hasAutosampler, hasValves);
  }
  
  @Override
  public void close()
  {
    Runnable r = new Runnable()
    {

      @Override
      public void run()
      {
        AbstractCloseableToolControl.this.notifyAction(new Action(ActionType.CLOSING, Optional.empty()));
        try
        {
          AbstractCloseableToolControl.this.getState().close();
        }
        catch (Exception e)
        {
          String msg = "closing has crashed";
          _LOG.fatal(msg, e);
          AbstractCloseableToolControl.this.handleException(msg, e);
        }
      }
    } ;
    
    new Thread(r).start();
  }
  
  @Override
  void cancelOperations() throws InterruptedException
  {
    throw new UnsupportedOperationException("cancel operations is not implemented");
  }
  
  @Override
  void reinitOperations() throws InterruptedException
  {
    throw new UnsupportedOperationException("reinit operations is not implemented");
  }
  
  @Override
  void pauseOperations() throws InterruptedException
  {
    throw new UnsupportedOperationException("pause operations is not implemented");
  }
 
  @Override
  void resumeOperations() throws InterruptedException
  {
    throw new UnsupportedOperationException("resume operations is not implemented");
  }
}
