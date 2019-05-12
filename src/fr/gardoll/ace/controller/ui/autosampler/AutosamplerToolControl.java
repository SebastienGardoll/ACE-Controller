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
  private final Passeur _passeur ;
  private Colonne colonne = null;
  private boolean flagGo = false ;
  // autorise la fermeture de la fenêtre ou non à cause des threads
  private AbstractThreadControl _currentThread = null;
  
  public AutosamplerToolControl() throws InitializationException
  {
    // Initialization checking.
    ParametresSession parametresSession = null;
    try
    {
      parametresSession = ParametresSession.getInstance() ;
    }
    catch (InitializationException e)
    {
      String msg = e.getMessage();
      _LOG.fatal(msg, e);
      throw e;
    }
    
    this._passeur = parametresSession.getPasseur();
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
      return ;
    }
  }
  
  void armFreeMove(int value)
  {
    int nbPas = Passeur.convertBras(value) ;
    this.enableControlPanel(false);
    ArmThread thread = new ArmThread(this, _passeur, nbPas, 1);
    _LOG.debug(String.format("start arm thread for free move '%s'", value));
    thread.start();
  }
  
  void cancel()
  {
    if (this._currentThread != null && this._currentThread.isAlive())
    {
      this._currentThread.cancel();
    }
    
    try
    {
      _LOG.info("cancelling all operations");
      this.notifyAction(new Action(ActionType.CANCEL, null));
      this._passeur.cancel();
    }
    catch (InterruptedException e)
    {
      String msg = "cancellation has been interrupted";
      _LOG.fatal(msg);
      return ;
    }
  }
  
  void close()
  {
    this.cancel();
  }
}

/*


//---------------------------------------------------------------------------
void __fastcall TF_ControlesPasseur::BitBtnGoButeeClick(TObject *Sender)
{
   enableControl ( false ) ;

   threadBras = new ThreadBras ( true ,
                                 *this,
                                 passeur,
                                 parametresSession,
                                 0,
                                 0 );// zero pour le choix fin de butée

   threadBras->FreeOnTerminate = false ;
   threadBras->Resume();        
}
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
//---------------------------------------------------------------------------
void __fastcall TF_ControlesPasseur::BitBtnGoColonneClick(TObject *Sender)
{
   if ( ! flagGo ) return ;

   ChoixColonne  choix ( nomFichierColonne ) ;
   colonne = choix.instanciation(); //initialisation de _colonne
   
   enableControl ( false ) ;

   threadBras = new ThreadBras ( true,
                                 * this,
                                 passeur,
                                 * colonne,
                                 parametresSession  );

   threadBras->FreeOnTerminate = false ;  //c'est le thread organiseur qui le delete 
   threadBras->Resume();        
}
//---------------------------------------------------------------------------
void __fastcall TF_ControlesPasseur::BitBtnPoubelleClick(TObject *Sender)
{
   enableControl ( false ) ;

   threadBras = new ThreadBras ( true,
                                 *this,
                                 passeur,
                                 parametresSession,
                                 0,
                                 3 );// zero pour le choix poubelle
                                 
   threadBras->FreeOnTerminate = false ;
   threadBras->Resume();
}
//---------------------------------------------------------------------------
void __fastcall TF_ControlesPasseur::BitBtnGoClick(TObject *Sender)
{
  enableControl ( false ) ;

  threadCarrousel = new ThreadCarrousel ( true ,
                                          * this,
                                          passeur,
                                          AdvSpinEditPosition->Value );
  threadCarrousel->FreeOnTerminate = false ;
  threadCarrousel->Resume();
}
//---------------------------------------------------------------------------
void __fastcall TF_ControlesPasseur::BitBtnGaucheClick(TObject *Sender)
{
   enableControl (false);

   threadCarrouselRelatif = new ThreadCarrouselRelatif ( true ,
                                                         * this,
                                                         passeur,
                                                         -1 * NB_POSITION  );
   threadCarrouselRelatif->FreeOnTerminate = false ;
   threadCarrouselRelatif->Resume();        
}
//---------------------------------------------------------------------------
void __fastcall TF_ControlesPasseur::BitBtnDroiteClick(TObject *Sender)
{
   enableControl (false);

   threadCarrouselRelatif = new ThreadCarrouselRelatif ( true ,
                                                         * this,
                                                         passeur,
                                                         NB_POSITION );
   threadCarrouselRelatif->FreeOnTerminate = false ;
   threadCarrouselRelatif->Resume();
}
//---------------------------------------------------------------------------
void __fastcall TF_ControlesPasseur::BitBtnArretUrgenceClick(
      TObject *Sender)
{
   try {

         try {
               try { TerminateThread( (void*) threadCarrousel->Handle , 0 )  ; }
               catch ( EAccessViolation  &e ) {;}  //protection si le thread n'existe pas ou plus

               try { TerminateThread( (void*) threadCarrouselRelatif->Handle , 0 )  ;  }
               catch ( EAccessViolation  &e ) {;}

               try {  TerminateThread( (void*) threadBras->Handle , 0 )  ;  }
               catch ( EAccessViolation  &e ) {;}

             }   __finally {  passeur.arretUrgence();
                              enableControl ( true );
                           }

        } catch ( Exception &e ) { MessageDlg (e.Message + ", arret d'urgence impossible, veuillez actionner le bouton coup de poing", mtError, TMsgDlgButtons() << mbOK , 0 );
                                   abort();
                                 }
}
//---------------------------------------------------------------------------
void __fastcall TF_ControlesPasseur::BitBtnRefClick(TObject *Sender)
{
   passeur.setModeManuel();
   MessageDlg (FP_VISUEL_CARROUSEL, mtInformation, TMsgDlgButtons() << mbOK , 0 );
   passeur.setOrigineCarrousel ();        
}
//---------------------------------------------------------------------------
void __fastcall TF_ControlesPasseur::BitBtnFermerClick(TObject *Sender)
{
   if ( fermeture ) this->ModalResult = mrOk ;
   else  MessageDlg (C_FERMETURE, mtInformation, TMsgDlgButtons() << mbOK , 0 ) ;        
}
//---------------------------------------------------------------------------
void __fastcall TF_ControlesPasseur::FormCloseQuery(TObject *Sender,
      bool &CanClose)
{
    if ( ! fermeture ) MessageDlg (C_FERMETURE, mtInformation, TMsgDlgButtons() << mbOK , 0 );

   CanClose = fermeture ;        
}

*/
class CarouselThread extends AbstractThreadControl
{
  private static final Logger _LOG = LogManager.getLogger(CarouselThread.class.getName());
  
  private final Passeur _passeur;
  private final int _position;
  private final ToolControl _toolCtrl ;
    
  public CarouselThread(ToolControl toolCtrl, Passeur passeur, int position)
  {
    super();
    this._passeur  = passeur;
    this._position = position;
    this._toolCtrl = toolCtrl;
  }
  
  //TODO: report actions.
  @Override
  public void run()
  {
    try
    {
      _LOG.debug("run CarouselThread");
      Action action = new Action(ActionType.CAROUSEL_MOVING, this._position);
      this._toolCtrl.notifyAction(action);
      this._passeur.moveCarrousel(this._position);
      this._passeur.finMoveCarrousel();
    }
    catch(InterruptedException e)
    {
      String msg = "carousel thread has been interrupted";
      _LOG.fatal(msg);
      this.interrupt(); // Reset the interruption state of this thread.
      return ; // Terminate the execution of the thread.
    }
    catch(CancellationException e)
    {
      _LOG.info("operations have been canceled");
      return; // Terminate the execution of the thread.
    }
    catch(Exception e)
    {
      String msg = String.format("carousel thread has crashed: %s", e);
      _LOG.fatal(msg, e);
      this._toolCtrl.notifyError(msg, e);
      return ; // Terminate the execution of the thread.
    }
    finally
    {
      this._toolCtrl.enableControlPanel(true);
    }
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
  private final ToolControl _toolCtrl ;
  private Colonne _colonne ;
    
  public ArmThread(ToolControl toolCtrl, Passeur passeur, int nbPas, int choix)
  {
    super();
    this._passeur  = passeur;
    this._nbPas    = nbPas;
    this._choix    = choix;
    this._toolCtrl = toolCtrl;
  }
  
  public ArmThread(ToolControl toolCtrl, Passeur passeur, Colonne colonne)
  {
    super();
    this._passeur  = passeur;
    this._colonne  = colonne;
    this._choix    = 2;
    this._toolCtrl = toolCtrl;
  }
  
  // TODO: report actions.
  @Override
  public void run()
  {
    try
    {
      _LOG.debug("run ArmThread");
            
      ParametresSession parametresSession = null;
      try
      {
        parametresSession = ParametresSession.getInstance() ;
      }
      catch (InitializationException e)
      {
        String msg = e.getMessage();
        _LOG.fatal(msg, e);
        this._toolCtrl.notifyError(msg, e);
        return; // Terminate the execution of the thread.
      }
      
      try
      {
        _LOG.debug(String.format("run ArmThread with order '%s'", this._choix));
        
        Action action = new Action(ActionType.ARM_MOVING, null);
        this._toolCtrl.notifyAction(action);
        
        switch (this._choix)
        { 
          case 0:
          {
            this._passeur.moveButeBras();
            break ; 
          }

          case 1:
          {
            this._passeur.moveBras(this._nbPas);
            break ;
          }

          case 2:
          {
            this._passeur.moveButeBras();
            this._passeur.finMoveBras() ;
            this._passeur.setOrigineBras();
            this._passeur.moveBras(Passeur.convertBras(
            this._colonne.hauteurColonne() + this._colonne.hauteurReservoir()  -
                parametresSession.refCarrousel()));
            break ;
          }

          case 3:
          { 
            this._passeur.moveButeBras();
            this._passeur.finMoveBras() ;
            this._passeur.setOrigineBras(); 
            this._passeur.moveBras(Passeur.convertBras(
                _COEFF * parametresSession.epaisseur() - 
                parametresSession.refCarrousel()));
            break ;
          }

          default:
          { 
            String msg = String.format("unsupported order '%s'", this._choix);
            _LOG.fatal(msg);
            this._toolCtrl.notifyError(msg, null);
            return; // Terminate the execution of the thread.
          }
        }

        this._passeur.finMoveBras() ;
        this._passeur.setOrigineBras();
      }
      catch(InterruptedException e)
      {
        String msg = "arm thread has been interrupted";
        _LOG.fatal(msg);
        this.interrupt(); // Reset the interruption state of this thread.
        return ; // Terminate the execution of the thread.
      }
    }
    catch(CancellationException e)
    {
      _LOG.info("operations have been canceled");
      return; // Terminate the execution of the thread.
    }
    catch(Exception e)
    {
      String msg = String.format("arm thread has crashed: %s", e);
      _LOG.fatal(msg, e);
      this._toolCtrl.notifyError(msg, e);
      return ; // Terminate the execution of the thread.
    }
    finally
    {
      this._toolCtrl.enableControlPanel(true);
    }
  }
}