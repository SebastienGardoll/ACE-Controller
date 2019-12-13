package fr.gardoll.ace.controller.tools.extraction;

import java.time.Duration ;
import java.time.Instant ;
import java.util.Optional ;

import org.apache.commons.lang3.tuple.ImmutablePair ;
import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.AbstractThreadControl ;
import fr.gardoll.ace.controller.core.AbstractToolControl ;
import fr.gardoll.ace.controller.core.Action ;
import fr.gardoll.ace.controller.core.ActionType ;
import fr.gardoll.ace.controller.core.CancellationException ;
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.protocol.Protocol ;
import fr.gardoll.ace.controller.protocol.Sequence ;

public class ExtractionThreadControl extends AbstractThreadControl
{
  private static final Logger _LOG = LogManager.getLogger(ExtractionThreadControl.class.getName());
  
  private final InitSession _initSession ;
  private final Protocol _protocol ;
  
  public ExtractionThreadControl(AbstractToolControl toolCtrl,
                                 InitSession initSession) throws InitializationException
  {
    super(toolCtrl) ;
    this._initSession = initSession;
    this._protocol = new Protocol(initSession.cheminFichierProtocole);
  }
  
  @Override
  protected void threadLogic() throws InterruptedException,
      CancellationException, InitializationException, Exception
  {
    {
      _LOG.info("starting session");
      Action action = new Action(ActionType.SESSION_START, Optional.empty()) ;
      this._toolCtrl.notifyAction(action) ;
    }
    
    _LOG.debug("instantiate commandes");
    Commandes commandes = new Commandes(this._toolCtrl, this._protocol.colonne);
    
    // flag pour effectuer les préliminaires dans threadSequence
    boolean preliminaires = true ;
    
    Instant[] tabTemps = new Instant[this._initSession.nbColonne] ;  //ok 10/01/06
    {
      Instant now = Instant.now();
      //initialisation de tabTemps
      for(int i = 0 ; i < this._initSession.nbColonne ; i++)
      {
        tabTemps[i] =  now;
      }
    }
    
    boolean reprise = this._initSession.numColonne != 1;
    
    Optional<Long> tempsPrecedent = Optional.empty();
    
    // attention le <= est très important car sequenceIndex doit être = à nbMaxSequence
    for(int sequenceIndex = this._initSession.numSequence;
            sequenceIndex <= this._protocol.nbMaxSequence;
            sequenceIndex++)
    {
      Sequence currentSequence = this._protocol.sequence(sequenceIndex);
      
      {
        String msg;
        if(reprise)
        {
          msg = String.format("begin sequence %s (extraction resuming): %s",
                              sequenceIndex, currentSequence);
        }
        else
        {
          msg = String.format("begin sequence %s: %s",
                              sequenceIndex, currentSequence);
        }
        
        _LOG.info(msg);
        
        Optional<Object> opt = Optional.of(ImmutablePair.of(sequenceIndex,
            currentSequence));
        Action action = new Action(ActionType.SEQUENCE_START, opt) ;
        this._toolCtrl.notifyAction(action) ;
      }
      
      if(reprise)
      {
        processSequence(currentSequence, commandes, tabTemps, preliminaires,
                        tempsPrecedent,
                        this._initSession.nbColonne,
                        this._initSession.numColonne);
        
        reprise = false ;
      }
      else
      {
        processSequence(currentSequence, commandes, tabTemps, preliminaires,
                        tempsPrecedent, this._initSession.nbColonne);
      }
      
      if(currentSequence.pause || // Pause.
         (sequenceIndex == this._protocol.nbMaxSequence)) // Last sequence.
      {
        // entre maintenant et la dernière colonne.
        long tempsEcoule = Duration.between(Instant.now(),tabTemps[tabTemps.length-1]).toSeconds();
        long tempsAttente = currentSequence.temps - tempsEcoule ;
        
        if(tempsAttente > 0l)
        {
          String msg = String.format("wait %s seconds until the last column percolates",
                                     tempsAttente);
          _LOG.info(msg);
          Action action = new Action(ActionType.SEQUENCE_AWAIT, Optional.of(tempsAttente)) ;
          this._toolCtrl.notifyAction(action) ;
          
          this.await(tempsAttente); // Blocking.
          
          _LOG.info("wait done");
          action = new Action(ActionType.SEQUENCE_AWAIT_DONE, Optional.empty()) ;
          this._toolCtrl.notifyAction(action) ;
        }
        else
        {
          // Nothing to do.
        }
      }
      else  
      {
        // le temps d'attente est effectué dans la séquence suivante voir code Sequence.
        // Nothing to do.
      }
      
      if(sequenceIndex == this._protocol.nbMaxSequence)
      {
        // dernière séquence, normalement la boucle for est finie.
        commandes.rincageH2O();
      }
      else
      {
        // cas ou il y a une séquence suivante même si la séquence courante a une pause
        Sequence nextSequence = this._protocol.sequence(sequenceIndex+1);
        
        if(currentSequence.numEv == nextSequence.numEv)
        {
          // même numEv que la séquence suivante
          commandes.remplissageSeringue(nextSequence.volume * this._initSession.nbColonne,
                                        nextSequence.numEv);
        }
        else  //pas le même numEv que la séquence suivante
        {
          commandes.rincageH2O();
          commandes.rincage(nextSequence.numEv);
          commandes.remplissageSeringue(nextSequence.volume * this._initSession.nbColonne,
                                        nextSequence.numEv);
        }
      }
      
      if(currentSequence.pause)
      {
        // si la séquence se termine par une pause protocole
        _LOG.info("terminate the sequence with a pause");
        Action action = new Action(ActionType.SEQUENCE_PAUSE, Optional.empty()) ;
        this._toolCtrl.notifyAction(action) ;
        
        this.selfTriggerPause();
      }
      
      // effectuer qu'une fois par appel de OrganiseurThreadSequence
      preliminaires = false ;
      
      tempsPrecedent = Optional.of(currentSequence.temps);
      
      {
        _LOG.info(String.format("sequence %s is completed", sequenceIndex));
        Action action = new Action(ActionType.SEQUENCE_DONE, Optional.empty()) ;
        this._toolCtrl.notifyAction(action) ;
      }
    } //fin du for
    
    commandes.finSession();
    
    {
      _LOG.info("session is completed");
      Action action = new Action(ActionType.SESSION_DONE, Optional.empty()) ;
      this._toolCtrl.notifyAction(action) ;
    }
  }

  private void processSequence(Sequence currentSequence, Commandes commandes,
                               Instant[] tabTemps, boolean preliminaires,
                               Optional<Long> tempsPrecedent, int nbColonne)
                                   throws InterruptedException
  {
    processSequence(currentSequence, commandes, tabTemps, preliminaires,
                    tempsPrecedent, this._initSession.nbColonne, 1);
  }

  private void processSequence(Sequence sequence, Commandes commandes,
                               Instant[] tabTemps, boolean preliminaires,
                               Optional<Long> tempsPrecedent,
                               int nbColonne, int numColonne) throws InterruptedException
  {
    //--------------------------------------------------------------------------
    // Conditions initiales : tuyauterie purgée d'air
    //                         seringue vide et purgée d'air
    //                         bras n'import où
    //                         carrousel sur la position de la poubelle et référencé
    //--------------------------------------------------------------------------

    /************ INTERVALES ************

     numcolonnes : 1 à nbColonnes inclus

     tabTemps : 0 à nbColonnes-1 inclus

    *************            *************/
    
    /* DEBUT PRELIMINAIRES */
    if(preliminaires)
    {
      // maintenant l'origine du bras correspond à au dessus de la colonne
      commandes.referencementBras();
      
      commandes.deplacementPasseurPoubelle();
      
      commandes.rincageH2O();
      
      commandes.rincage(sequence.numEv) ;
    }
    /* FIN PRELIMINAIRES */
    
    //bras dans la poubelle, carrousel position poubelle
    //seringues vides et purgées d'air
    
    for(; numColonne <= nbColonne ; numColonne++)       // algo testé le 12/01/05
    {
      int nbColonneRestant = nbColonne - numColonne ;
      
      //mettre indication d'attente !!
      
      //ok 10/01/06
      long tempsEcoule = Duration.between(Instant.now(),tabTemps[numColonne-1]).toSeconds();
      
      if(false == tempsPrecedent.isEmpty() &&
         tempsEcoule < tempsPrecedent.get())
      {
        long timeToWait = tempsPrecedent.get() - tempsEcoule + 1;
        
        String msg = String.format("wait %s seconds until the next column percolates",
                                   timeToWait);
        _LOG.info(msg);
        Action action = new Action(ActionType.SEQUENCE_AWAIT, Optional.of(timeToWait)) ;
        this._toolCtrl.notifyAction(action) ;
        
        this.await(timeToWait); // Blocking.
        
        _LOG.info("wait done, processing next column");
        action = new Action(ActionType.SEQUENCE_AWAIT_DONE, Optional.empty()) ;
        this._toolCtrl.notifyAction(action) ;
      }
      
      //1ère sequence => pas d'attente
      
      commandes.distribution(numColonne, sequence.volume, sequence.numEv, nbColonneRestant);
      tabTemps[numColonne-1] = Instant.now() ; //ok 10/01/06

      // Must keep the sleep even after C++ translation because elution times
      // are computed with this wait in all the protocols.
      Thread.sleep(2000) ;    //pourquoi Sleep ?
      
    } //fin du for
    
    commandes.deplacementPasseurPoubelle();//retour à la poubelle
  }
}
