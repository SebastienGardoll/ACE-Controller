package fr.gardoll.ace.controller.tools.autosampler;

import java.nio.file.Path ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.autosampler.Passeur ;
import fr.gardoll.ace.controller.column.Colonne ;
import fr.gardoll.ace.controller.core.AbstractThreadControl ;
import fr.gardoll.ace.controller.core.AbstractStateFullToolControl ;
import fr.gardoll.ace.controller.core.Action ;
import fr.gardoll.ace.controller.core.ActionType ;
import fr.gardoll.ace.controller.core.CancellationException ;
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.core.ToolControlOperations ;

public class AutosamplerToolControl extends AbstractStateFullToolControl
{
  private static final Logger _LOG = LogManager.getLogger(AutosamplerToolControl.class.getName());
  private Colonne colonne = null;
  private boolean hasColumn = false ;
  // autorise la fermeture de la fenêtre ou non à cause des threads
  
  public AutosamplerToolControl(ParametresSession parametresSession)
      throws InitializationException, InterruptedException
  {
    super(parametresSession, false, true, false);
  }
  
  void vibrate()
  {
    try
    {
      _LOG.debug("vibrate arm");
      this._passeur.vibration();
    }
    catch (Exception e)
    {
      String msg = "vibrating has crashed";
      _LOG.fatal(msg, e);
      this.handleException(msg, e);
    }
  }
  
  void armFreeMove(int value)
  {
    try
    {
      int nbPas = Passeur.convertBras(value) ;
      ArmThread thread = new ArmThread(this, this._passeur, nbPas, 1);
      _LOG.debug(String.format("start arm thread for free move '%s'", value));
      this.start(thread);
    }
    catch(Exception e)
    {
      String msg = "arm free move start has crashed";
      _LOG.fatal(msg, e);
      this.handleException(msg, e);
    }
  }
  
  void armGoButee()
  {
    try
    {
      // zero pour le choix fin de butée
      ArmThread thread = new ArmThread(this, this._passeur, 0, 0);
      _LOG.debug("start arm thread for go stop");
      this.start(thread);
    }
    catch(Exception e)
    {
      String msg = "arm go stop start has crashed";
      _LOG.fatal(msg, e);
      this.handleException(msg, e);
    }
  }
  
  void armGoColonne()
  {
    try
    {
      if (this.hasColumn == false)
      {
        _LOG.error("colonne not set");
        this.notifyError("Column file not set, open one before. Aborted.");
        return ;
      }
      else
      {
        ArmThread thread = new ArmThread(this, this._passeur, this.colonne);
        _LOG.debug("start arm thread for go to column");
        this.start(thread);
      }
    }
    catch(Exception e)
    {
      String msg = "arm go column start has crashed";
      _LOG.fatal(msg, e);
      this.handleException(msg, e);
    }
  }
  
  void armGoTrash()
  {
    try
    {
      ArmThread thread = new ArmThread(this, this._passeur, 0, 3);
      _LOG.debug("start arm thread for go to trash can");
      this.start(thread);
    }
    catch(Exception e)
    {
      String msg = "arm go trash start has crashed";
      _LOG.fatal(msg, e);
      this.handleException(msg, e);
    }
  }
  
  void carouselGoPosition(int position)
  {
    try
    {
      CarouselThread thread = new CarouselThread(this, this._passeur, position);
      _LOG.debug(String.format("start carousel thread for go to position '%s'", position));
      this.start(thread);
    }
    catch(Exception e)
    {
      String msg = "carousel go position start has crashed";
      _LOG.fatal(msg, e);
      this.handleException(msg, e);
    }
  }
  
  void carouselTurnLeft()
  {
    try
    {
      int nbPosition =  -1 * ParametresSession.NB_POSITION;
      CarouselRelativeThread thread = new CarouselRelativeThread(this, this._passeur, nbPosition);
      _LOG.debug("start carousel thread for turn to left");
      this.start(thread);
    }
    catch(Exception e)
    {
      String msg = "carousel turn left start has crashed";
      _LOG.fatal(msg, e);
      this.handleException(msg, e);
    }
  }
  
  void carouselTurnRight()
  {
    try
    {
      int nbPosition =  ParametresSession.NB_POSITION;
      CarouselRelativeThread thread = new CarouselRelativeThread(this, this._passeur, nbPosition);
      _LOG.debug("start carousel thread for turn to right");
      this.start(thread);
    }
    catch(Exception e)
    {
      String msg = "carousel turn right start has crashed";
      _LOG.fatal(msg, e);
      this.handleException(msg, e);
    }
  }
  
  void carouselFreeMove()
  {
    _LOG.debug("start carousel free move");
    try
    {
      this._passeur.setModeManuel();
      this.displayControlPanelModalMessage("Click on Ok to finish");
      this._passeur.setOrigineCarrousel();
    }
    catch (Exception e)
    {
      String msg = "arm free move has crashed";
      _LOG.fatal(msg);
      this.handleException(msg, e);
    }
  }
  
  void openColumn(Path filePath)
  {
    try
    {
      this.colonne = Colonne.getInstance(filePath);
      this.hasColumn = true;
    }
    catch (Exception e)
    {
      String msg = "error while openning column file";
      _LOG.error(msg, e);
      this.handleException(msg, e);
    }
  }  
}

class CarouselRelativeThread extends AbstractThreadControl
{
  private static final Logger _LOG = LogManager.getLogger(CarouselRelativeThread.class.getName());
  private final int _nbPosition ;
  private final Passeur _passeur ;
  
  public CarouselRelativeThread(ToolControlOperations toolCtrl, Passeur passeur, int nbPosition)
  {
    super(toolCtrl);
    this._passeur  = passeur;
    this._nbPosition = nbPosition;
  }
  
  @Override
  protected void threadLogic() throws InterruptedException,
      CancellationException, InitializationException, Exception
  {
    _LOG.debug("run CarouselRelativeThread");
    Action action = new Action(ActionType.CAROUSEL_RELATIVE_MOVING, this._nbPosition);
    this._toolCtrl.notifyAction(action);
    
    this._passeur.moveButeBras();
    this._passeur.finMoveBras();
    this._passeur.moveCarrouselRelatif(this._nbPosition);
    this._passeur.finMoveCarrousel();
    this._toolCtrl.notifyAction(new Action(ActionType.CAROUSEL_END_MOVING, null)) ;
  }
}

class CarouselThread extends AbstractThreadControl
{
  private static final Logger _LOG = LogManager.getLogger(CarouselThread.class.getName());
  
  private final Passeur _passeur;
  private final int _position;
    
  public CarouselThread(ToolControlOperations toolCtrl, Passeur passeur, int position)
  {
    super(toolCtrl);
    this._passeur  = passeur;
    this._position = position;
  }
  
  @Override
  protected void threadLogic() throws InterruptedException,
                                      InitializationException,
                                      CancellationException,
                                      Exception
  {
    _LOG.debug("run CarouselThread");
    Action action = new Action(ActionType.CAROUSEL_MOVING, this._position);
    this._toolCtrl.notifyAction(action);
    this._passeur.moveCarrousel(this._position);
    this._passeur.finMoveCarrousel();
    this._toolCtrl.notifyAction(new Action(ActionType.CAROUSEL_END_MOVING, null)) ;
  }
}

class ArmThread extends AbstractThreadControl
{
  // coefficient pour la distance plateau porteur tuyau pour la poubelle
  private static final double _COEFF = 2.5 ;
  
  private static final Logger _LOG = LogManager.getLogger(ArmThread.class.getName());
  
  private final Passeur _passeur;
  private int _nbPas ;
  private final int _choix ;
  private Colonne _colonne ;
    
  public ArmThread(ToolControlOperations toolCtrl, Passeur passeur, int nbPas, int choix)
  {
    super(toolCtrl);
    this._passeur  = passeur;
    this._nbPas    = nbPas;
    this._choix    = choix;
  }
  
  public ArmThread(ToolControlOperations toolCtrl, Passeur passeur, Colonne colonne)
  {
    super(toolCtrl);
    this._passeur  = passeur;
    this._colonne  = colonne;
    this._choix    = 2;
  }
  
  @Override
  protected void threadLogic() throws InterruptedException,
                                      CancellationException,
                                      InitializationException,
                                      Exception
  {
    _LOG.debug("run ArmThread") ;
    ParametresSession parametresSession = null ;
    parametresSession = ParametresSession.getInstance() ;

    _LOG.debug(String.format("run ArmThread with order '%s'", this._choix)) ;

    Action action = new Action(ActionType.ARM_MOVING, null) ;
    this._toolCtrl.notifyAction(action) ;

    switch (this._choix)
    {
      case 0:
      {
        this._passeur.moveButeBras() ;
        break ;
      }

      case 1:
      {
        this._passeur.moveBras(this._nbPas) ;
        break ;
      }

      case 2:
      {
        this._passeur.moveButeBras() ;
        this._passeur.finMoveBras() ;
        this._passeur.setOrigineBras() ;
        this._passeur.moveBras(Passeur.convertBras(
            this._colonne.hauteurColonne() + this._colonne.hauteurReservoir()
                - parametresSession.refCarrousel())) ;
        break ;
      }

      case 3:
      {
        this._passeur.moveButeBras() ;
        this._passeur.finMoveBras() ;
        this._passeur.setOrigineBras() ;
        this._passeur
            .moveBras(Passeur.convertBras(_COEFF * parametresSession.epaisseur()
                - parametresSession.refCarrousel())) ;
        break ;
      }

      default:
      {
        String msg = String.format("unsupported order '%s'", this._choix) ;
        _LOG.fatal(msg) ;
        this._toolCtrl.notifyError(msg) ;
        return ; // Terminate the execution of the thread.
      }
    }

    this._passeur.finMoveBras() ;
    this._toolCtrl.notifyAction(new Action(ActionType.ARM_END_MOVING, null)) ;
    this._passeur.setOrigineBras() ;
  }
}