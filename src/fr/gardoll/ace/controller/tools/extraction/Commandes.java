package fr.gardoll.ace.controller.tools.extraction;

import java.util.Optional ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.autosampler.Passeur ;
import fr.gardoll.ace.controller.column.Colonne ;
import fr.gardoll.ace.controller.core.Action ;
import fr.gardoll.ace.controller.core.ActionType ;
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.core.ToolControl ;
import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.pump.PousseSeringue ;
import fr.gardoll.ace.controller.valves.Valves ;

// TODO: singleton.
// TODO: add logging
// TODO: add user logging (high level)
public class Commandes
{
  private final ParametresSession parametresSession;

  private final Colonne colonne;

  private final Passeur passeur;

  private final PousseSeringue pousseSeringue;
  
  private final ToolControl _toolCtrl ; 
  
  private static final Logger _LOG = LogManager.getLogger(Commandes.class.getName());

  //requires colonne != NULL
  public Commandes (ToolControl toolCtrl, Colonne colonne)
      throws InitializationException, InterruptedException
  {
    this.colonne   = colonne;
    this._toolCtrl = toolCtrl;
    ParametresSession parametresSession = ParametresSession.getInstance();
    
    this.pousseSeringue = parametresSession.getPousseSeringue();
    this.passeur = parametresSession.getPasseur();
    this.parametresSession = parametresSession;
    
    //aspiration est toujours au débit max
    this.pousseSeringue.setDebitAspiration(parametresSession.debitMaxPousseSeringue());
  }
  
  //rinçe la tuyauterie selon le volume de rinçage et le nombre de cycle de parametresSession
  //requires numEv <= pousseSeringue.nbEvMax()
  public void rincage(int numEv) throws InterruptedException
  { 
    _LOG.info(String.format("rincing with valve %s", numEv));
    //le refoulement pour les rinçage se fait toujours au débit max.
    this.pousseSeringue.setDebitRefoulement(this.parametresSession.debitMaxPousseSeringue()); 
                                                                                    
    //perte du volume de sécurité
    this.pousseSeringue.vidange(); 

    this.pousseSeringue.finPompage();                                                   

    for(int i = 0 ; i < this.parametresSession.nbRincage() ; i++)
    {
      // appel aspiration dédié au rinçage à cause
      // ok pour deux seringue 07/10/05 //de la gestion du volume de sécu voir
      // aussi vidange
      this.pousseSeringue.rincageAspiration(this.parametresSession.volumeRincage(),
                                            numEv) ; 
      this.pousseSeringue.finPompage() ;

      this.pousseSeringue.vidange() ;

      this.pousseSeringue.finPompage() ;
    }

    this.passeur.vibration();
  }

  public void rincageH2O() throws InterruptedException
  { 
    this.rincage(Valves.NUM_EV_H2O) ;
  }
    
  //à la position de carrousel donnée
  public void deplacementPasseur(int position) throws InterruptedException
  {
    this.deplacementPasseur(position, 0);
  }
  
  public void deplacementPasseur(int position, int modificateur) throws InterruptedException
  {  
    //le bras est juste au dessus du réservoir de la colonne
    this.passeur.moveOrigineBras();
    this.passeur.finMoveBras() ;
    this.passeur.moveCarrousel( position, modificateur );
    this.passeur.finMoveCarrousel();
  }
  
  //à la position de la poubelle , bras à l'intérieur de poubelle
  //le bras se retrouve dans la poubelle
  public void deplacementPasseurPoubelle() throws InterruptedException
  {
    int correction = 0 ;

    this.deplacementPasseur(0) ;

    if (this.colonne.hauteurReservoir() > this.parametresSession.epaisseur())
    {
      correction = this.parametresSession.epaisseur() ;
    }

    this.passeur.moveBras(- Passeur.convertBras(this.colonne.hauteurReservoir()
        + this.colonne.hauteurColonne() - correction)) ;
    
    this.passeur.finMoveBras() ;
  }
  
  //fixe l'origine du bras juste au dessus des colonne
  public void referencementBras() throws InterruptedException
  { //sans setOrigineBras() inclus ! 
    this.passeur.moveButeBras();
    this.passeur.finMoveBras() ;
    this.passeur.setOrigineBras();
    this.passeur.moveBras(Passeur.convertBras(this.colonne.hauteurColonne() + this.colonne.hauteurReservoir()  - this.parametresSession.refCarrousel()));
    this.passeur.finMoveBras() ;
    this.passeur.setOrigineBras();
  }
  
  //rempli la seringue de l'acide à quantité demandée
  //sans dépasser la limite de la capacité de la seringue
  //fait en sorte que le vol ds la seringue soit : limite seringue utile
  // valeur demandée - vol restant
  //volume en mL                              
  //requires vol_demande > 0                  
  //requires numEv <= pousseSeringue.nbEvMax()                                                 
  public void remplissageSeringue(double vol_demande, int numEv)
      throws InterruptedException
  {
    if (vol_demande <= this.pousseSeringue.volumeRestant())
    {
      return ;
    }
    else
    {
      //doit considéré le volume max utile !!
      if (vol_demande + this.pousseSeringue.volumeRestant() > this.pousseSeringue
          .volumeMaxSeringueUtile()) 
      {
        vol_demande = this.pousseSeringue.volumeMaxSeringueUtile()
                      - this.pousseSeringue.volumeRestant() ;
      }

      this.pousseSeringue.aspiration(vol_demande, numEv) ;
      this.pousseSeringue.finPompage() ;
    }
  }
  
  //opérations de distribution du volume de liquide
  //volume en mL
  //requires vol_deja_delivre >= 0
  //requires vol_delivre > 0
  private void algoDistribution(double vol_delivre, double vol_deja_delivre)
      throws InterruptedException
  {
    double vol_total = vol_delivre + vol_deja_delivre ;

    if (vol_deja_delivre < this.colonne.volumeCritique1())
    { 
      this.pousseSeringue.setDebitRefoulement(this.colonne.pousseSeringueDebitMin());
    }
    else if (vol_deja_delivre < this.colonne.volumeCritique2())
    { 
      this.pousseSeringue.setDebitRefoulement(this.colonne.pousseSeringueDebitInter());
    }
    else
    { 
      this.pousseSeringue.setDebitRefoulement(this.colonne.pousseSeringueDebitMax());
    }

    this.pousseSeringue.refoulement(vol_delivre, Valves.NUM_EV_REFOULEMENT);

    if ((vol_total > this.colonne.volumeCritique1()) &&
        (vol_deja_delivre < this.colonne.volumeCritique1()))
    {  
      while ((this.pousseSeringue.volumeDelivre() + vol_deja_delivre) < this.colonne.volumeCritique1())
      { 
        Thread.sleep(100);
      }
      
      this.pousseSeringue.setDebitRefoulement(this.colonne.pousseSeringueDebitInter());
    }

    if ((vol_total > this.colonne.volumeCritique2() ) && (vol_deja_delivre < this.colonne.volumeCritique2()))
    {  
      while ((this.pousseSeringue.volumeDelivre() + vol_deja_delivre) < this.colonne.volumeCritique2())
      { 
        Thread.sleep(100) ;
      }
      
      this.pousseSeringue.setDebitRefoulement(this.colonne.pousseSeringueDebitMax());
    }

    this.pousseSeringue.finPompage() ;
  }
  
  //requires volumeCible > 0
  //requires numEV <= pousseSeringue.nbEvMax()
  public void distribution(int numColonne,
                           double volumeCible,
                           int numEv,
                           int nbColonneRestant) throws InterruptedException
  {
    double vol_deja_delivre = 0. ;

    double vol_delivre ;

    this._toolCtrl.notifyAction(new Action(ActionType.CAROUSEL_MOVING,
        Optional.of(Integer.valueOf(numColonne))));
    this.deplacementPasseur(numColonne , this.calculsDeplacement(volumeCible)) ;

    int nbPasBrasAtteindre = this.calculsHauteur(volumeCible) + Passeur.convertBras(this.colonne.hauteurMenisque() - this.colonne.hauteurReservoir());
    // calculs de nombre de pas à descendre cad hauteur max du liquide dans un réservoir cônique ou cylindrique

    this._toolCtrl.notifyAction(new Action(ActionType.ARM_MOVING, Optional.empty()));
    this.passeur.moveBras(nbPasBrasAtteindre);

    this.passeur.finMoveBras();

    //évite volumeCible == 0
    //évite problème de comparaison de réels
    while ( ! Utils.isNearZero( volumeCible - vol_deja_delivre))
    {
      if (Utils.isNearZero(pousseSeringue.volumeRestant()))
      { 
        this._toolCtrl.notifyAction(new Action(ActionType.WITHDRAWING, Optional.empty()));

        if (nbColonneRestant == 0) 
        {
          this.remplissageSeringue(volumeCible - vol_deja_delivre , numEv);
        }
        else 
        {
          this.remplissageSeringue((nbColonneRestant + 1 ) * volumeCible - vol_deja_delivre, numEv) ;
        }
      }
      else
      {  
        this._toolCtrl.notifyAction(new Action(ActionType.INFUSING, Optional.empty()));

        if (this.pousseSeringue.volumeRestant() < volumeCible - vol_deja_delivre)
        { 
          vol_delivre = this.pousseSeringue.volumeRestant()  ;
          // avec attente de fin de distribution
          this.algoDistribution(vol_delivre, vol_deja_delivre);
          vol_deja_delivre += vol_delivre ;
        }
        else 
        { 
          vol_delivre = volumeCible - vol_deja_delivre ;
          this.algoDistribution(vol_delivre, vol_deja_delivre) ;
          vol_deja_delivre += vol_delivre ;
        }
      }
    }//fin du while

    this.passeur.vibration(); 
  }
  
  // revoye le nombre de pas à descendre dans la colonne pour le bras
  // en fonction du volume d'éluant donné
  // volume en microLitre        
  // requires volume > 0
  private int calculsHauteur (double volume) //V doit être en mili litre !!!
  {  
    return Passeur.convertBras(this.colonne.calculsHauteur(volume)) ;
  }
  
  //revoye le nombre de pas à deplacer dans la colonne pour le carrousel
  //en fonction du volume d'acide donné
  // volume en microLitre                       
  //requires volume > 0
  public int calculsDeplacement(double volume) //V doit être en mili litre !!!
  {  
    return this.passeur.convertCarrousel(this.colonne.calculsDeplacementCarrousel(volume)) ;
  }
  
  //procédures de fin de session.
  public void finSession() throws InterruptedException
  {  
    this.pousseSeringue.fermetureEv() ;
    this.passeur.moveButeBras();
    this.passeur.finMoveBras();
  }

  //déplace le carrousel pour rendre accessible les côté du carrousel
  //qui ne le sont pas
  //attention si un thread est sur pause, l'appel par un autre thread
  //de cette fonction sera piégé dans la boucle de finMoveBras !!!
  public void presentationPasseur(int sens) throws InterruptedException
  {
    this.passeur.moveButeBras();
    this.passeur.finMoveBras();

    if (sens >= 0)
    {
      this.passeur.moveCarrouselRelatif(ParametresSession.NB_POSITION) ; //par la droite
    }
    else
    {
      this.passeur.moveCarrouselRelatif( -1 * ParametresSession.NB_POSITION) ; //par la gauche
    }

    this.passeur.finMoveCarrousel();
  }
}
