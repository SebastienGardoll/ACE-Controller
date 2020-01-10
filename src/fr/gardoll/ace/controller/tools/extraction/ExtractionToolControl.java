package fr.gardoll.ace.controller.tools.extraction;

import java.util.Optional ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.autosampler.Passeur ;
import fr.gardoll.ace.controller.core.AbstractPausableToolControl ;
import fr.gardoll.ace.controller.core.Action ;
import fr.gardoll.ace.controller.core.ActionType ;
import fr.gardoll.ace.controller.core.ControlPanel ;
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.settings.ParametresSession ;

public class ExtractionToolControl extends AbstractPausableToolControl
{
  private static final Logger _LOG = LogManager.getLogger(ExtractionToolControl.class.getName());
  
  public ExtractionToolControl(ParametresSession parametresSession)
                            throws InitializationException
  {
    super(parametresSession, true, true, true) ;
  }

  void start(InitSession initSession)
  {
    Commandes commandes = null;
    
    try
    {
      _LOG.debug("instantiate commandes");
      commandes = new Commandes(this, initSession.protocol.colonne);
    }
    catch(Exception e)
    {
      String msg = "initialization of commandes has crashed";
      _LOG.fatal(msg, e);
      this.handleException(msg, e);
    }
    
    try
    {
      ExtractionThreadControl thread = new ExtractionThreadControl(this,
          initSession, commandes);
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
  protected void closeOperations()
  {
    _LOG.debug("controller has nothing to do while closing the tool");
    this.notifyAction(new Action(ActionType.CLOSING, Optional.empty()));
  }
  
  private void enableCarouselControl(boolean isEnable)
  {
    for(ControlPanel panel : this.getCtrlPanels())
    {
      panel.enableResume(isEnable);
      panel.enableCarousel(isEnable);
    }
  }
  
  void turnCarouselToRight()
  {
    _LOG.info("turning the carousel to the right");
    this.notifyAction(new Action(ActionType.CAROUSEL_TURN_RIGHT, Optional.empty()));
    this.turnCarousel(1);
  }
  
  void turnCarouselToLeft()
  {
    _LOG.info("turning the carousel to the left");
    this.notifyAction(new Action(ActionType.CAROUSEL_TURN_LEFT, Optional.empty()));
    this.turnCarousel(-1);
  }
  
  private void turnCarousel(int direction)
  {
    Runnable threadLogic = () -> 
    {
      ExtractionToolControl.this.enableCarouselControl(false);
      ExtractionToolControl.this.presentationPasseur(direction);
      ExtractionToolControl.this.enableCarouselControl(true);
    };
    
    new Thread(threadLogic).start();
  }
  
  //déplace le carrousel pour rendre accessible les côté du carrousel
  //qui ne le sont pas
  //attention si un thread est sur pause, l'appel par un autre thread
  //de cette fonction sera piégé dans la boucle de finMoveBras !!!
  private void presentationPasseur(int sens)
  {
    try
    {
      Passeur passeur = ParametresSession.getInstance().getPasseur();
      
      passeur.moveButeBras();
      passeur.finMoveBras();
      // Don't call setArmOrigin as the arm reference (above the column) is lost.
      
      if (sens >= 0)
      {
        //par la droite
        passeur.moveCarrouselRelatif(Passeur.NB_POSITION) ;
      }
      else
      {
        //par la gauche
        passeur.moveCarrouselRelatif( -1 * Passeur.NB_POSITION) ;
      }

      passeur.finMoveCarrousel();
    }
    catch (Exception e)
    {
      String msg = String.format("turning to %s has crashed", sens);
      _LOG.fatal(msg, e);
      this.handleException(msg, e);
    }
  }
}
