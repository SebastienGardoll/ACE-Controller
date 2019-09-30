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
          AbstractCloseableToolControl.this.closeOperations();
        }
        catch (Exception e)
        {
          String msg = "closing operations has crashed";
          _LOG.fatal(msg, e);
          // Reinit takes place in the main thread, there isn't any operating
          // thread that is running. So it is safe to change the state here.
          AbstractCloseableToolControl.this.handleException(msg, e);
        }
        
        for(ControlPanel panel: AbstractCloseableToolControl.this.getCtrlPanels())
        {
          panel.dispose();
        }
      }
    } ;
    
    new Thread(r).start();
  }
  
  @Override
  void cancelOperations() throws InterruptedException
  {
    throw new UnsupportedOperationException();
  }
  
  @Override
  void reinitOperations() throws InterruptedException
  {
    throw new UnsupportedOperationException();
  }
  
  @Override
  void pauseOperations() throws InterruptedException
  {
    throw new UnsupportedOperationException();
  }
 
  @Override
  void resumeOperations() throws InterruptedException
  {
    throw new UnsupportedOperationException();
  }
}
