package fr.gardoll.ace.controller.ui;

import java.awt.Window ;
import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.Optional ;

import javax.swing.JOptionPane ;
import javax.swing.JPanel ;
import javax.swing.SwingUtilities ;

import org.apache.commons.lang3.tuple.Pair ;
import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.Action ;
import fr.gardoll.ace.controller.core.ControlPanel ;
import fr.gardoll.ace.controller.core.ToolControl ;
import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.protocol.Sequence ;

public abstract class AbstractJPanelObserver  extends JPanel implements ControlPanel
{
  private static final long serialVersionUID = -6962722774816032530L ;

  private static final Logger _LOG = LogManager.getLogger(AbstractJPanelObserver.class.getName());
  
  protected static final SimpleDateFormat _DATE_FORMATTER = new SimpleDateFormat("HH:mm");
  
  protected final ToolControl _ctrl ;
  
  protected boolean _isCloseEnable = false;
  
  protected abstract void displayToUserLogSys(String msg);
  protected abstract void enableCloseControl(boolean isEnable) ;
  
  public AbstractJPanelObserver(ToolControl ctrl)
  {
    this._ctrl = ctrl;
  }
  
  @Override
  public void dispose()
  {
    try
    {
      // Sleep 1 second so as the user to read the log before closing the window.
      Thread.sleep(1000);
    }
    catch (InterruptedException e)
    {
      // Nothing to do.
    }
    
    Window parent = UiUtils.getParentFrame(this);
    if(parent != null)
    {
      _LOG.debug("closing the parent frame and may shutdown the JVM");
      parent.dispose();
    }
  }

  @Override
  public void majActionActuelle(Action action)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        AbstractJPanelObserver.this.processAction(action);
      }
    });
  }
  
  // Throwable can be null.
  @Override
  public void reportError(String msg, Throwable e)
  {
    Utils.reportError(msg, e);
    this.displayToUserLogSys(String.format("%s # %s: %s\n", _DATE_FORMATTER.format(new Date()), 
        msg, e.getMessage()));
  }
  
  @Override
  public void reportError(String msg)
  {
    Utils.reportError(msg, null);
  }
  
  @Override
  public void displayModalMessage(String msg)
  {
    JOptionPane.showMessageDialog(null, msg, "Information",
        JOptionPane.INFORMATION_MESSAGE);
  }
  
  protected void processAction(Action action)
  {
    String msg = null;
    
    switch(action.type)
    {
      case ARM_MOVING:
      {
        msg = "arm is moving";
        break ;
      }
      
      case ARM_END_MOVING:
      {
        msg = "arm reached the position";
        break;
      }
      
      case CANCELING:
      {
        msg = "canceling";
        break ;
      }
      
      case CAROUSEL_MOVING:
      {
        msg = String.format("carousel is moving to position %s", action.data.get());
        break ;
      }
      
      case CAROUSEL_RELATIVE_MOVING:
      {
        msg = String.format("carousel is moving %s positions", action.data.get());
        break;
      }
      
      case CAROUSEL_END_MOVING:
      {
        msg = "carousel reached the position";
        break;
      }
      
      case PAUSING:
      {
        msg = "pausing";
        break ;
      }
      
      case PAUSE_DONE:
      {
        msg = "operations are paused";
        break;
      }
      
      case RESUME:
      {
        msg = "resuming";
        break ;
      }
      
      case RESUME_DONE:
      {
        msg = "resuming done";
        break;
      }
      
      case WAIT_CANCEL:
      {
        msg = "waiting for cancellation";
        break ;
      }
      
      case WAIT_PAUSE:
      {
        msg = "waiting for pause";
        break ;
      }
      
      case REINIT:
      {
        msg = "reinitializing";
        break;
      }
      
      case REINIT_DONE:
      {
        msg = "reinitialization is done";
        break;
      }
      
      case CANCEL_DONE:
      {
        msg = "cancellation is done";
        break;
      }
      
      case CLOSING:
      {
        msg = "closing the tool";
        break;
      }
      
      case OPEN_VALVE:
      {
        @SuppressWarnings("unchecked")
        Pair<Integer,Optional<Integer>> payload = (Pair<Integer, Optional<Integer>>) action.data.get();
        
        int valveId = payload.getLeft();
        Optional<Integer> lastValveId = payload.getRight();
        
        if(lastValveId.isPresent())
        {
          msg = String.format("closing valve '%s' , opening valve '%s'", lastValveId.get(), valveId);
        }
        else
        {
          msg = String.format("opening valve '%s'", valveId);
        }
        
        break;
      }
      
      case CLOSE_VALVES:
      {
        @SuppressWarnings("unchecked")
        Pair<Integer,Optional<Integer>> payload = (Pair<Integer, Optional<Integer>>) action.data.get();
        
        msg = String.format("closing valve '%s'", payload.getLeft());
        break;
      }
      
      case CLOSE_ALL_VALVES:
      {
        msg = "closing all valves";
        break;
      }

      case WITHDRAWING:
      {
        if(action.data.isEmpty())
        {
          msg = "pump is withdrawing";          
        }
        else
        {
          msg = String.format("pump is withdrawing %s mL", action.data.get());
        }
        
        break;
      }
      
      case INFUSING:
      {
        if(action.data.isEmpty())
        {
          msg = "pump is infusing";
        }
        else
        {
          msg = String.format("pump is infusing %s mL", action.data.get());
        }
        
        break;
      }
      
      case USR_MSG:
      {
        msg = action.data.get().toString();
        break;
      }
      
      case DRAIN_PUMP:
      {
        msg = "draining the pump";
        break;
      }
      
      case SEQUENCE_START:
      {
        @SuppressWarnings("unchecked")
        Pair<Integer, Sequence> pair = (Pair<Integer, Sequence>) action.data.get(); 
        Integer sequenceIndex = pair.getLeft();
        Sequence sequence = pair.getRight();
        msg = String.format("starting sequence %s: %s", sequenceIndex, sequence);
        break;
      }
      
      case SEQUENCE_AWAIT:
      {
        long delay = (long) action.data.get() / 1000l ; // Convert into seconds.
        msg = String.format("waiting %s seconds until the column processing is done",
                            delay);
        break;
      }
      
      case SEQUENCE_AWAIT_DONE:
      {
        msg = "wait done";
        break;
      }
      
      case NEXT_SEQUENCE_PREP:
      {
        msg = "preparing the pump for the next sequence";
        break;
      }
      
      case SEQUENCE_DONE:
      {
        msg = String.format("sequence %s is completed", action.data.get());
        break;
      }
      
      case H2O_RINCE:
      {
        msg = "rincing with H20";
        break;
      }
      
      case RINCE:
      {
        msg = String.format("rincing with eluent from valve %s", action.data.get());
        break;
      }
      
      case REFILL_PUMP:
      {
        @SuppressWarnings("unchecked")
        Pair<Double, Integer> pair = (Pair<Double, Integer>) action.data.get(); 
        Double volume = pair.getLeft();
        Integer numEv = pair.getRight();
        msg = String.format("refilling the pump with %s mL from valve %s",
            volume, numEv);
        break;
      }
      
      case SEQUENCE_PAUSE_START:
      {
        msg = "waiting for the operator";
        break;
      }
      
      case SEQUENCE_PAUSE_DONE:
      {
        msg = "resuming by the operator";
        break;
      }
      
      case SESSION_START:
      {
        msg = "starting the session";
        break;
      }
      
      case SESSION_DONE:
      {
        msg = "the session is completed";
        break;
      }
      
      case INDEX_ARM:
      {
        msg = "indexing the arm above the column";
        break;
      }
      
      case CAROUSEL_TO_TRASH:
      {
        msg = "moving the carousel to the trash position";
        break;
      }
      
      case COLUMN_DIST_START:
      {
        msg = String.format("processing column %s", action.data.get());
        break;
      }
      
      case COLUMN_DIST_DONE:
      {
        msg = String.format("column %s is completed", action.data.get());
        break;
      }
      
      case PREPARING:
      {
        msg = "preparing the extraction";
        break;
      }
      
      case CAROUSEL_TURN_RIGHT:
      {
        msg = "turning the carousel to the right";
        break;
      }
      
      case CAROUSEL_TURN_LEFT:
      {
        msg = "turning the carousel to the left";
        break;
      }
      
      case POST_LAST_SEQ:
      {
        msg = "starting last sequence operations";
        break;
      }
      
      case POST_SESSION:
      {
        msg = "post session operations";
        break;
      }
      
      default:
      {
        _LOG.debug(String.format("nothing to do with action type '%s'", action.type));
        return ;
      }
    }
    
    this.displayToUserLogSys(String.format("%s > %s\n", _DATE_FORMATTER.format(new Date()), msg));
  }
}
