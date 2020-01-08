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

  private final Commandes _cmd ;
  
  public ExtractionThreadControl(AbstractToolControl toolCtrl,
                                 InitSession initSession,
                                 Commandes commandes) throws InitializationException
  {
    super(toolCtrl, false) ;
    this._initSession = initSession;
    this._cmd = commandes;
  }
  
  @Override
  protected void threadLogic() throws CancellationException,
                                      InitializationException, Exception
  {
    {
      _LOG.info("starting session");
      Action action = new Action(ActionType.SESSION_START, Optional.empty()) ;
      this._toolCtrl.notifyAction(action) ;
      
      _LOG.info(this._initSession.toString());
    }
    
    // Shortcut.
    Protocol protocol = this._initSession.protocol;
    
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
    
    boolean reprise = this._initSession.numColonne  != 1 ||
                      this._initSession.numSequence != 1;
    
    Optional<Long> tempsPrecedent = Optional.empty();
    
    // attention le <= est très important car sequenceIndex doit être = à nbMaxSequence
    for(int sequenceIndex = this._initSession.numSequence;
            sequenceIndex <= protocol.nbMaxSequence;
            sequenceIndex++)
    {
      Sequence currentSequence = protocol.sequence(sequenceIndex);
      
      {
        String msg;
        if(reprise)
        {
          msg = String.format("*** begin sequence %s (resuming): %s ***",
                              sequenceIndex, currentSequence);
        }
        else
        {
          msg = String.format("*** begin sequence %s: %s ***",
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
        processSequence(currentSequence, this._cmd, tabTemps, preliminaires,
                        tempsPrecedent,
                        this._initSession.nbColonne,
                        this._initSession.numColonne);
        
        reprise = false ;
      }
      else
      {
        processSequence(currentSequence, this._cmd, tabTemps, preliminaires,
                        tempsPrecedent, this._initSession.nbColonne);
      }
      
      if(sequenceIndex == protocol.nbMaxSequence)
      {
        _LOG.debug("last sequence post operations: rince with H20");
        
        Action action = new Action(ActionType.POST_LAST_SEQ, Optional.empty()) ;
        this._toolCtrl.notifyAction(action) ;
        
        // dernière séquence, normalement la boucle for est finie.
        this._cmd.rincageH2O();
      }
      else
      {
        _LOG.debug("preparing the next sequence");
        Action action = new Action(ActionType.NEXT_SEQUENCE_PREP, Optional.empty()) ;
        this._toolCtrl.notifyAction(action) ;
        
        // cas ou il y a une séquence suivante même si la séquence courante a une pause
        Sequence nextSequence = protocol.sequence(sequenceIndex+1);
        
        if(currentSequence.numEv == nextSequence.numEv)
        {
          _LOG.info(String.format("refill from valve %s", nextSequence.numEv));
          // même numEv que la séquence suivante
          this._cmd.remplissageSeringue(nextSequence.volume * this._initSession.nbColonne,
                                        nextSequence.numEv);
        }
        else  //pas le même numEv que la séquence suivante
        {
          this._cmd.rincageH2O();
          this._cmd.rincage(nextSequence.numEv);
          _LOG.info(String.format("refill from valve %s", nextSequence.numEv));
          this._cmd.remplissageSeringue(nextSequence.volume * this._initSession.nbColonne,
                                        nextSequence.numEv);
        }
      }
      
      if(currentSequence.pause || // Pause.
         (sequenceIndex == protocol.nbMaxSequence)) // Last sequence.
      {
        // entre maintenant et la dernière colonne.
        // +1 because between definition.
        long tempsEcoule = Duration.between(tabTemps[tabTemps.length-1], Instant.now()).toMillis() + 1l;
        long tempsAttente = currentSequence.temps * 1000l - tempsEcoule ;

        if(tempsAttente > 0l)
        {
          String msg = String.format("wait %s ms until the last column percolates",
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
          // Nothing todo.
          
          String msg = "";
          if(currentSequence.pause)
          {
            msg = "sequence with pause doesn't have to wait";
          }
          else
          {
            msg = "last sequence doesn't have to wait";
          }
          
          _LOG.debug(msg);
        }
      }
      else  
      {
        // le temps d'attente est effectué dans la séquence suivante voir code Sequence.
        // Nothing to do.
      }
      
      {
        _LOG.info(String.format("sequence %s is completed", sequenceIndex));
        Action action = new Action(ActionType.SEQUENCE_DONE, Optional.of(sequenceIndex)) ;
        this._toolCtrl.notifyAction(action) ;
      }
      
      if(currentSequence.pause)
      {
        // si la séquence se termine par une pause protocole
        _LOG.info("terminate the current sequence with a pause");
        Action action = new Action(ActionType.SEQUENCE_PAUSE_START, Optional.empty()) ;
        this._toolCtrl.notifyAction(action) ;
        
        this.selfTriggerPause();
        
        _LOG.info("resuming from a sequence pause");
        action = new Action(ActionType.SEQUENCE_PAUSE_DONE, Optional.empty()) ;
        this._toolCtrl.notifyAction(action) ;
      }
      else
      {
        _LOG.debug("the current sequence has not pause");
      }
      
      // Only one time.
      preliminaires = false ;
      
      tempsPrecedent = Optional.of(currentSequence.temps);
    } //fin du for
    
    this._cmd.finSession();
    
    {
      _LOG.info("session is completed");
      Action action = new Action(ActionType.SESSION_DONE, Optional.empty()) ;
      this._toolCtrl.notifyAction(action) ;
    }
  }

  private void processSequence(Sequence currentSequence, Commandes commandes,
                               Instant[] tabTemps, boolean preliminaires,
                               Optional<Long> tempsPrecedent, int nbColonne)
  {
    processSequence(currentSequence, commandes, tabTemps, preliminaires,
                    tempsPrecedent, this._initSession.nbColonne, 1);
  }

  private void processSequence(Sequence sequence, Commandes commandes,
                               Instant[] tabTemps, boolean preliminaires,
                               Optional<Long> tempsPrecedent,
                               int nbColonne, int numColonne)
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
      _LOG.info("preparing the extraction");
      Action action = new Action(ActionType.PREPARING, Optional.empty()) ;
      this._toolCtrl.notifyAction(action) ;
      
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
      // +1 because between definition.
      long tempsEcoule = Duration.between(tabTemps[numColonne-1], Instant.now()).toMillis() + 1l;
      
      if(false == tempsPrecedent.isEmpty() &&
         tempsEcoule < (tempsPrecedent.get() * 1000l))
      {
        long timeToWait = tempsPrecedent.get() * 1000l - tempsEcoule;
        
        String msg = String.format("wait %s ms until the next column percolates",
                                   timeToWait);
        _LOG.info(msg);
        Action action = new Action(ActionType.SEQUENCE_AWAIT, Optional.of(timeToWait)) ;
        this._toolCtrl.notifyAction(action) ;
        
        this.await(timeToWait); // Blocking.
        
        _LOG.info("wait done, processing the next column");
        action = new Action(ActionType.SEQUENCE_AWAIT_DONE, Optional.empty()) ;
        this._toolCtrl.notifyAction(action) ;
      }
      else
      {
        _LOG.debug("don't need to wait the next column");
      }
      
      //1ère sequence => pas d'attente
      
      commandes.distribution(numColonne, sequence.volume, sequence.numEv, nbColonneRestant);
      tabTemps[numColonne-1] = Instant.now() ; //ok 10/01/06

      // Must keep the sleep even after C++ translation because elution times
      // are computed with this wait in all the protocols.
      try
      {
        Thread.sleep(2000l) ; //pourquoi Sleep ?
      }
      catch (InterruptedException e)
      {
        throw new RuntimeException(e);
      }
      
    } //fin du for
    
    commandes.deplacementPasseurPoubelle();//retour à la poubelle
  }
}
