package fr.gardoll.ace.controller.core;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.column.Colonne ;
import fr.gardoll.ace.controller.common.ParametresSession ;
import fr.gardoll.ace.controller.common.SerialComException ;
import fr.gardoll.ace.controller.common.Utils ;
import fr.gardoll.ace.controller.pump.PousseSeringue ;
import fr.gardoll.ace.controller.sampler.Passeur ;
import fr.gardoll.ace.controller.ui.ControlPanel ;

public class Commandes
{
  private ParametresSession parametresSession = null;

  private Colonne colonne = null;

  private Passeur passeur = null;

  private PousseSeringue pousseSeringue = null;
  
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
  
  public void rincage(int numEv)
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


  public void rincageH2O()
  { 
    rincage(PousseSeringue.numEvH2O()) ;
  }
  
  //XXX thread safe ?
  // évite l'interblocage. attendant la fin de l'execution d'un ordre passé sur
  // le port serie. concurrence entre l'utilisateur, le thread sequence ou
  // thread organiseur
  public synchronized void pause(OrganiseurThreadSequence threadOrganiseur)
  {  
    ThreadSequence threadSequence = threadOrganiseur.adresseThreadSequence();
    
    threadOrganiseur.pause();
    threadSequence.pause();
    
    this.pousseSeringue.pause();    
    this.passeur.pause();
  }
  
  // XXX thread safe ?
  public void reprise (OrganiseurThreadSequence threadOrganiseur)
  {  
    this.passeur.reprise(false); 

    //attention la reprise du passeur avant celle du pousse seringue à
    //cause de la manipulation eventuelle de celui ci
    this.pousseSeringue.reprise(); 
                               
    ThreadSequence threadSequence = threadOrganiseur.adresseThreadSequence();
    
    //relance le thread Sequence s'il existe encore
    threadOrganiseur.unPause();
    threadSequence.unPause();
  }
  
  //XXX thread safe ?
  public void arretUrgence() throws SerialComException
  {
    this.passeur.arretUrgence();
    this.pousseSeringue.arretUrgence();
    
    // XXX disable the threadOrganiseur and threadSequence !
  }
  
  public void deplacementPasseur(int position)
  {
    this.deplacementPasseur(position, 0);
  }
  
  public void deplacementPasseur(int position, int modificateur)
  {  
    //le bras est juste au dessus du réservoir de la colonne
    this.passeur.moveOrigineBras();
    this.passeur.finMoveBras() ;
    this.passeur.moveCarrousel( position, modificateur );
    this.passeur.finMoveCarrousel();
  }

  //le bras se retrouve dans la poubelle
  public void deplacementPasseurPoubelle()
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
  public void referencementBras()
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
  public void algoDistribution(double vol_delivre, double vol_deja_delivre)
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
        try
        {
          Thread.sleep(100);
        }
        catch (InterruptedException e)
        {
          String msg = String.format("interrupted while waiting for distribution: %s", e.getMessage());
          _LOG.debug(msg);
        }
      }
      
      this.pousseSeringue.setDebitRefoulement(this.colonne.pousseSeringueDebitInter());
    }

    if ((vol_total > this.colonne.volumeCritique2() ) && (vol_deja_delivre < this.colonne.volumeCritique2()))
    {  
      while ((this.pousseSeringue.volumeDelivre() + vol_deja_delivre) < this.colonne.volumeCritique2())
      { 
        try
        {
          Thread.sleep(100) ;
        }
        catch (InterruptedException e)
        {
          String msg = String.format("interrupted while waiting for distribution: %s", e.getMessage());
          _LOG.debug(msg);
        }
      }
      
      this.pousseSeringue.setDebitRefoulement(this.colonne.pousseSeringueDebitMax());
    }

    this.pousseSeringue.finPompage() ;
  }
  
  public void distribution(int numColonne,
                           double volumeCible,
                           int numEv,
                           int nbColonneRestant)
  {
    this.distribution(numColonne, volumeCible, numEv, nbColonneRestant, null);
  }
  
  public void distribution(int numColonne,
                           double volumeCible,
                           int numEv,
                           int nbColonneRestant,
                           ControlPanel panel)
  {
    double vol_deja_delivre = 0. ;

    double vol_delivre ;

    panel.majActionActuelle("deplacement");

    this.deplacementPasseur(numColonne , this.calculsDeplacement(volumeCible)) ;

    int nbPasBrasAtteindre = this.calculsHauteur(volumeCible) + Passeur.convertBras(this.colonne.hauteurMenisque() - this.colonne.hauteurReservoir());
    // calculs de nombre de pas à descendre cad hauteur max du liquide dans un réservoir cônique ou cylindrique

    this.passeur.moveBras(nbPasBrasAtteindre);

    this.passeur.finMoveBras();

    //évite volumeCible == 0
    //évite problème de comparaison de réels
    while ( ! Utils.isNearZero( volumeCible - vol_deja_delivre))
    {
      if (Utils.isNearZero(pousseSeringue.volumeRestant()))
      { 
        panel.majActionActuelle("remplissage");

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
        panel.majActionActuelle("distributionEluant");

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
  
  //revoye le nombre de pas à descendre dans la colonne pour le bras
  // en fonction du volume d'éluant donné
  // volume en microLitre        
  //requires volume > 0
  public int calculsHauteur (double volume) //V doit être en mili litre !!!
  {  
    return Passeur.convertBras(this.colonne.calculsHauteur(volume)) ;
  }
  
  public int calculsDeplacement(double volume) //V doit être en mili litre !!!
  {  
    return this.passeur.convertCarrousel(this.colonne.calculsDeplacementCarrousel(volume)) ;
  }
  
  public void finSession()
  {  
    this.pousseSeringue.fermetureEv() ;
    this.passeur.moveButeBras();
    this.passeur.finMoveBras();
  }

  public void presentationPasseur(int sens)
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
