package fr.gardoll.ace.controller.core;

import java.io.Closeable ;
import java.io.IOException ;
import java.util.HashSet ;
import java.util.Set ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.column.Colonne ;
import fr.gardoll.ace.controller.common.ParametresSession ;
import fr.gardoll.ace.controller.common.SerialComException ;
import fr.gardoll.ace.controller.common.Utils ;
import fr.gardoll.ace.controller.pump.PousseSeringue ;
import fr.gardoll.ace.controller.sampler.Passeur ;
import fr.gardoll.ace.controller.ui.Action ;
import fr.gardoll.ace.controller.ui.ControlPanel ;
import fr.gardoll.ace.controller.ui.Observable ;

//TODO: singleton.
// TODO: add logging
public class Commandes implements Closeable, Observable
{
  private final ParametresSession parametresSession;

  private final Colonne colonne;

  private final Passeur passeur;

  private final PousseSeringue pousseSeringue;
  
  private final Set<ControlPanel> _ctrlPanels = new HashSet<>(); 
  
  private static final Logger _LOG = LogManager.getLogger(Commandes.class.getName());

  //requires & colonne != NULL
  public Commandes (ParametresSession parametresSession)
  {
    this.pousseSeringue = parametresSession.getPousseSeringue();
    this.passeur = parametresSession.getPasseur();
    this.colonne = parametresSession.getColonne();
    this.parametresSession = parametresSession;
    
    //aspiration est toujours au débit max
    this.pousseSeringue.setDebitAspiration(parametresSession.debitMaxPousseSeringue());
  }
  
  //rinçe la tuyauterie selon le volume de rinçage et le nombre de cycle de parametresSession
  //requires numEv <= pousseSeringue.nbEvMax()
  public void rincage(int numEv) throws InterruptedException
  { 
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
    this.rincage(PousseSeringue.numEvH2O()) ;
  }
  
  public void pause(ThreadSession session) throws InterruptedException
  {  
    // attend la fin de l'execution d'un ordre passé sur le port serie.
    // Concurrence entre l'utilisateur, le thread sequence ou
    // thread organiseur
    session.pause();
    
    // A partir de maintenant, il n'y a plus de concurrence.
    this.pousseSeringue.pause();    
    this.passeur.pause();
  }
  
  public void reprise (ThreadSession session) throws InterruptedException
  {  
    this.passeur.reprise(false); 

    //attention la reprise du passeur avant celle du pousse seringue à
    //cause de la manipulation eventuelle de celui ci
    this.pousseSeringue.reprise(); 
    
    session.unPause();
  }
  
  //XXX thread safe ?
  //procédure d'arrêt d'urgence
  public void arretUrgence() throws SerialComException, InterruptedException
  {
    this.passeur.arretUrgence();
    this.pousseSeringue.arretUrgence();
    
    // XXX disable the threadOrganiseur and threadSequence !
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

    this.pousseSeringue.refoulement(vol_delivre, PousseSeringue.numEvRefoulement());

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
  
  public void distribution(int numColonne,
                           double volumeCible,
                           int numEv,
                           int nbColonneRestant) throws InterruptedException
  {
    this.distribution(numColonne, volumeCible, numEv, nbColonneRestant, null);
  }
  
  //requires volumeCible > 0
  //requires numEV <= pousseSeringue.nbEvMax()
  public void distribution(int numColonne,
                           double volumeCible,
                           int numEv,
                           int nbColonneRestant,
                           ControlPanel panel) throws InterruptedException
  {
    double vol_deja_delivre = 0. ;

    double vol_delivre ;

    this.notifyObserver(Action.PLATE_MOVING);
    this.deplacementPasseur(numColonne , this.calculsDeplacement(volumeCible)) ;

    int nbPasBrasAtteindre = this.calculsHauteur(volumeCible) + Passeur.convertBras(this.colonne.hauteurMenisque() - this.colonne.hauteurReservoir());
    // calculs de nombre de pas à descendre cad hauteur max du liquide dans un réservoir cônique ou cylindrique

    this.notifyObserver(Action.HARM_MOVING);
    this.passeur.moveBras(nbPasBrasAtteindre);

    this.passeur.finMoveBras();

    //évite volumeCible == 0
    //évite problème de comparaison de réels
    while ( ! Utils.isNearZero( volumeCible - vol_deja_delivre))
    {
      if (Utils.isNearZero(pousseSeringue.volumeRestant()))
      { 
        this.notifyObserver(Action.WITHDRAWING);

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
        this.notifyObserver(Action.INFUSING);

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

  @Override
  public void addObserver(ControlPanel panel)
  {
    this._ctrlPanels.add(panel);
  }

  @Override
  public void removeObserver(ControlPanel panel)
  {
    this._ctrlPanels.remove(panel);
  }

  @Override
  public void notifyObserver(Action action)
  {
    for(ControlPanel panel: this._ctrlPanels)
    {
      panel.majActionActuelle(action);
    }
  }

  @Override
  public void close() throws IOException
  {
    this.passeur.close();
    this.pousseSeringue.close();
  }
}
