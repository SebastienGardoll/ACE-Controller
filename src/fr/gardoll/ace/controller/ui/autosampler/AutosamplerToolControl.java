package fr.gardoll.ace.controller.ui.autosampler;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.autosampler.Passeur ;
import fr.gardoll.ace.controller.column.Colonne ;
import fr.gardoll.ace.controller.common.CancellationException ;
import fr.gardoll.ace.controller.common.InitializationException ;
import fr.gardoll.ace.controller.common.ParametresSession ;
import fr.gardoll.ace.controller.core.AbstractThreadControl ;
import fr.gardoll.ace.controller.ui.AbstractToolControl ;
import fr.gardoll.ace.controller.ui.Action ;
import fr.gardoll.ace.controller.ui.ActionType ;
import fr.gardoll.ace.controller.ui.ToolControl ;

public class AutosamplerToolControl extends AbstractToolControl
{
  private static final Logger _LOG = LogManager.getLogger(AutosamplerToolControl.class.getName());
  private Colonne colonne = null;
  private boolean hasColumn = false ;
  // autorise la fermeture de la fenêtre ou non à cause des threads
  
  public AutosamplerToolControl() throws InitializationException
  {
    super(false, true);
  }
  
  void vibrate()
  {
    try
    {
      _LOG.debug("vibrate arm");
      this._passeur.vibration();
    }
    catch (InterruptedException e)
    {
      String msg = "vibrating has been interrupted";
      _LOG.fatal(msg);
      this.notifyError(msg, e);
      return ;
    }
  }
  
  void armFreeMove(int value)
  {
    int nbPas = Passeur.convertBras(value) ;
    this.enableControlPanel(false);
    ArmThread thread = new ArmThread(this, this._passeur, nbPas, 1);
    _LOG.debug(String.format("start arm thread for free move '%s'", value));
    thread.start();
  }
  
  void armGoButee()
  {
    this.enableControlPanel(false);
    // zero pour le choix fin de butée
    ArmThread thread = new ArmThread(this, this._passeur, 0, 0);
    _LOG.debug("start arm thread for go butée");
    thread.start();        
  }
  
  void armGoColonne()
  {
    if (this.hasColumn == false)
    {
      _LOG.debug("colonne not set");
      return ;
    }
    else
    {
      this.enableControlPanel(false);
      ArmThread thread = new ArmThread(this, this._passeur, this.colonne);
      _LOG.debug("start arm thread for go to column");
      thread.start();
    }
  }
  
  void armGoTrash()
  {
    this.enableControlPanel(false);
    ArmThread thread = new ArmThread(this, this._passeur, 0, 3);
    _LOG.debug("start arm thread for go to trash can");
    thread.start(); 
  }
  
  void carouselGoPosition(int position)
  {
    this.enableControlPanel(false);
    CarouselThread thread = new CarouselThread(this, this._passeur, position);
    _LOG.debug(String.format("start carousel thread for go to position '%s'", position));
    thread.start();
  }
  
  void carouselTurnLeft()
  {
    this.enableControlPanel(false);
    
    int nbPosition =  -1 * ParametresSession.NB_POSITION;
    CarouselRelativeThread thread = new CarouselRelativeThread(this, this._passeur, nbPosition);
    _LOG.debug("start carousel thread for turn to left");
    thread.start();        
  }
  
  void carouselTurnRight()
  {
    this.enableControlPanel(false);
    
    int nbPosition =  ParametresSession.NB_POSITION;
    CarouselRelativeThread thread = new CarouselRelativeThread(this, this._passeur, nbPosition);
    _LOG.debug("start carousel thread for turn to right");
    thread.start();        
  }
  
  void carouselFreeMove()
  {
    _LOG.debug("start carousel free move");
    try
    {
      this._passeur.setModeManuel();
      this.displayControlPanelModalMessage("Click on Ok to finish");
      this._passeur.setOrigineCarrousel ();
    }
    catch (InterruptedException e)
    {
      String msg = "arm free move has been interrupted";
      _LOG.fatal(msg);
      this.notifyError(msg, e);
      return ;
    }
  }
  
  
}

/*


//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
void __fastcall TF_ControlesPasseur::BitBtnOuvertureClick(TObject *Sender)
{
  if ( ! OpenDialog->Execute() ) return  ;

  TIniFile * fichier = new TIniFile ( OpenDialog->FileName ) ;

  EditColonne->Text = fichier->ReadString(SEC_INFO_COL , SICOL_NOM_COL , C_ERREUR_LECTURE );

  nomFichierColonne = OpenDialog->FileName;

  flagGo = true ;

  delete fichier ;
}

*/

class CarouselRelativeThread extends AbstractThreadControl
{
  private static final Logger _LOG = LogManager.getLogger(CarouselRelativeThread.class.getName());
  private final int _nbPosition ;
  private final Passeur _passeur ;
  
  public CarouselRelativeThread(ToolControl toolCtrl, Passeur passeur, int nbPosition)
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
    Action action = new Action(ActionType.CAROUSEL_MOVING, this._nbPosition);
    this._toolCtrl.notifyAction(action);
    
    this._passeur.moveButeBras();
    this._passeur.finMoveBras();
    this._passeur.moveCarrouselRelatif(this._nbPosition);
    this._passeur.finMoveCarrousel();
  }
}

class CarouselThread extends AbstractThreadControl
{
  private static final Logger _LOG = LogManager.getLogger(CarouselThread.class.getName());
  
  private final Passeur _passeur;
  private final int _position;
    
  public CarouselThread(ToolControl toolCtrl, Passeur passeur, int position)
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
    
  public ArmThread(ToolControl toolCtrl, Passeur passeur, int nbPas, int choix)
  {
    super(toolCtrl);
    this._passeur  = passeur;
    this._nbPas    = nbPas;
    this._choix    = choix;
  }
  
  public ArmThread(ToolControl toolCtrl, Passeur passeur, Colonne colonne)
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
        this._toolCtrl.notifyError(msg, null) ;
        return ; // Terminate the execution of the thread.
      }
    }

    this._passeur.finMoveBras() ;
    this._passeur.setOrigineBras() ;
  }
}