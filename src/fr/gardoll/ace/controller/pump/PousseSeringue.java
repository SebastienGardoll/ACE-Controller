package fr.gardoll.ace.controller.pump;

import java.io.Closeable ;
import java.io.IOException ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.comm.ParaCom ;
import fr.gardoll.ace.controller.common.InitializationException ;
import fr.gardoll.ace.controller.common.SerialComException ;
import fr.gardoll.ace.controller.common.Utils ;

public class PousseSeringue implements Closeable
{
  // temps d'attente entre la détection d'une fin de pompage
  // et la fermeture des EV <=> équilibre des pressions dans les tuyaux.
  public final static int ATTENTE_FERMETURE_EV = 1000 ;
                                              
  //volume laissé entre le piston et la fin du corps de la seringue par seringue
  //ceci dut à la forme conique de la fin du corps de la seringue
  public final static double VOL_SECU = 0.5 ; 
                                
  //correction en mL dût au jeux mécanique symétrique
  //de la visse d'entrainement du pousse seringue
  //utile seulement lors d'un changement de sens de pompage
  //par la seringue
  public final static double VOL_AJUSTEMENT = 0.5 ;   
                                        
  //Attention : static !!!! volume réel pour une seringue
  private static double _volumeReel ;
  
  private static final Logger _LOG = LogManager.getLogger(PousseSeringue.class.getName());
  
  private final InterfacePousseSeringue interfacePousseSeringue ;

  private final ParaCom para ;

  // débit en cours
  private double debitActuelAspiration ;

  //débit en cours
  private double debitActuelRefoulement ;

  //dépendant de la section de tuyaux utilisé et le diamètre de seringue
  //différent de débitIntrinsèque qui dépend uniquement du diamètre de la seringue
  private double _debitMaxPousseSeringue ; 
                                   
  private double _volumeMaxSeringue ;
  
  //numéro ev actuellement ouverte sert à la reprise d'une pause
  private int numEvActuelle ;  

  //flag de pause
  private boolean _pause ; 

  //info sur l'aspiration ou non du volume de sécurité, true : aspiré, false : refoulé
  private boolean flagVolSecu ; 

  private int _nbSeringue ;
  
  //requires nombresSeringues <= 2
  //requires volumeMaxSeringue > 0
  //requires 0 < debitMaxPousseSeringue <=  debitMaxIntrinseque ( diametreSeringue )
  //requires volumeInitiale >= 0
  public PousseSeringue(InterfacePousseSeringue interfacePousseSeringue,
                        ParaCom paraCom,
                        int nombreSeringue,
                        double diametreSeringue,
                        double volumeMaxSeringue,
                        double debitMaxPousseSeringue,
                        double volumeInitiale) throws InitializationException
  {
    _LOG.info("initializing the pump");
    
    this.interfacePousseSeringue = interfacePousseSeringue ;
    this.para = paraCom ;
    // attention les débits sont par défaut ceux en mémoire du pousse seringue.
    this.debitActuelAspiration = 0. ; 
    // un débit de zero est impossible voir interfacePousseSeringue.
    this.debitActuelRefoulement = 0. ; 
                         
    // ferme ttes les ev
    try
    {
      this.para.toutFermer(); ;
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while closing all the isolation valves: %s",
          e.getMessage());
      _LOG.fatal(msg);
      throw new InitializationException(msg, e);
    }
    
    this.numEvActuelle = 0 ;
    this._pause = false ;

    // ------ Conditions initiales : Seringue vidée manuellement -------

    // le volume de sécurité n'a pas été aspiré
    this.flagVolSecu = false ;

    if (nombreSeringue != 1 && nombreSeringue != 2)
    {
      String msg = String.format("unsupported number of syringe '%s'",
                                 nombreSeringue);
      _LOG.fatal(msg);
      throw new InitializationException(msg);
    }

    if (volumeInitiale < 0.)
    {
      String msg = String.format("the value of the initial volume '%s' cannot be negative or null",
          volumeInitiale);
      _LOG.fatal(msg);
      throw new InitializationException(msg);
    }
    
    if (volumeMaxSeringue <= 0.)
    {
      String msg = String.format("the value of the maximum syringe volume '%s' cannot be negative of null",
          volumeMaxSeringue);
      _LOG.fatal(msg);
      throw new InitializationException(msg);
    }
        
    if (debitMaxPousseSeringue <= 0.)
    {
      String msg = String.format("the value of the maximum rate '%s' cannot be negative of null", debitMaxPousseSeringue);
      _LOG.fatal(msg);
      throw new InitializationException(msg);
    }
    else if (debitMaxPousseSeringue > debitMaxIntrinseque(diametreSeringue))
    {
      String msg = String.format("the value of the maximum rate '%s' cannot be greater than %s ",
                                 debitMaxPousseSeringue,
                                 debitMaxIntrinseque(diametreSeringue));
      _LOG.fatal(msg);
      throw new InitializationException(msg);
    }

    this._nbSeringue = nombreSeringue ;

    PousseSeringue._volumeReel = volumeInitiale ;

    this._volumeMaxSeringue = volumeMaxSeringue ;

    this._debitMaxPousseSeringue = debitMaxPousseSeringue ;
  }

  public int nbSeringue()
  {
    return this._nbSeringue ;
  }
  
  public double debitMaxPousseSeringue()
  { 
    return this._debitMaxPousseSeringue ; 
  } 
  
  // précise si le pousse seringue est pausé
  public boolean isPaused()
  { 
    return this._pause;
  }

  // aspiration d'un volume exprimé en en mL.
  // requires 0 <= numEv <= NBEV_MAX
  public void aspiration(double volume, int numEv)
  { 
    _LOG.debug(String.format("withdrawing volume '%s' from isolation valve '%s'",
        volume, numEv));
    // amélioration de l'algo pour éviter les pertes !!!
    // considère le volume d'une seringue
    volume /= _nbSeringue ;   

    if (! this.flagVolSecu)
    { 
      // le volume de sécurité n'a été aspiré  => aspiration du vol
      volume += VOL_SECU  ; 
      // vol aspiré ;
      this.flagVolSecu = true ;
    }
    
    volume += VOL_AJUSTEMENT ;

    this.algoAspiration(volume, numEv) ;
    this.finPompage(false);
    
    // ajustement à pleine vitesse
    this.setDebitRefoulement(_debitMaxPousseSeringue) ;

    this.algoRefoulement(VOL_AJUSTEMENT, numEv) ; 
  }

  //refoulement d'un volume exprimé en en mL.
  //requires 0 <= numEv <= NBEV_MAX
  public void refoulement(double volume , int numEv)
  {
    _LOG.debug(String.format("infusing volume '%s' to the isolation valve '%s'",
        volume, numEv));
    volume /= _nbSeringue ;
    this.algoRefoulement(volume, numEv) ;
  }

  public void algoAspiration(double volume, int numEv)
  { 
    _LOG.debug(String.format("running withdrawing routine for volume '%s' from isolation valve '%s'",
        volume, numEv));
    //attention un ordre comme voli ou volw 0 correspond à une aspi/infusion sans fin.
    if (Utils.isNearZero(volume)) return ;

    if (numEv > ParaCom.NB_EV_MAX)
    {
      String msg = String.format("the value of the isolation valve '%s' cannot be greater than %s",
                                 numEv, ParaCom.NB_EV_MAX);
      _LOG.fatal(msg);
      throw new RuntimeException(msg);
    }

    if (this._volumeMaxSeringue < volume)
    {
      String msg = String.format("the volume '%s' to be pumped, cannot be greater than the maximum volume %s",
          volume, this._volumeMaxSeringue);
      _LOG.fatal(msg);
      throw new RuntimeException(msg);
    }

    PousseSeringue._volumeReel += volume ;
    
    try
    {
      // ouverture de l'ev numEv.
      para.ouvrir(numEv);

      this.numEvActuelle = numEv ;

      this.interfacePousseSeringue.modeW();

      this.interfacePousseSeringue.volw(volume);

      this.interfacePousseSeringue.run();
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while withdrawing the volume '%s' from isolation valve '%s': %s",
          volume, numEv, e.getMessage());
      _LOG.fatal(msg);
      throw new RuntimeException(msg, e);
    }
  }

  public void algoRefoulement(double volume, int numEv)

  { 
    _LOG.debug(String.format("running the infusion routine for volume '%s' to the isolation valve '%s'",
        volume, numEv));
    // attention un ordre comme voli ou volw 0 correspond à une aspi/infusion sans fin.
    // si y a un problème , il faut pouvoir le détecter.
    if (Utils.isNearZero(volume)) return ; 
                                              
    if (numEv > ParaCom.NB_EV_MAX)
    {
      String msg = String.format("the value of the isolation valve '%s' cannot be greater than %s",
                                 numEv, ParaCom.NB_EV_MAX);
      _LOG.fatal(msg);
      throw new RuntimeException(msg);
    }

    if (PousseSeringue._volumeReel < volume )
    {
      String msg = String.format("the volume '%s' to be delivered cannot be greater than the maximum volume %s",
          volume, PousseSeringue._volumeReel);
      _LOG.fatal(msg);
      throw new RuntimeException(msg);
    }

    PousseSeringue._volumeReel -= volume ;
    
    try
    {
      // ouverture de l'ev numEv
      para.ouvrir(numEv);

      this.numEvActuelle = numEv ;

      this.interfacePousseSeringue.modeI();

      this.interfacePousseSeringue.voli(volume);

      this.interfacePousseSeringue.run();
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while infusing the volume '%s' to isolation valve '%s': %s",
          volume, numEv, e.getMessage());
      _LOG.fatal(msg);
      throw new RuntimeException(msg, e);
    }
  }

  // ne rajoute pas de VOL_SECU mais ajoute VOL_AJUSTEMENT et
  // volume n'est pas divisé par nbSeringue !!!!!!!! 
  // spécialement pour l'aspiration d'un cycle de rinçage
  //volume n'est pas divisé par nbSeringue !!!
  public void rincageAspiration(double volume, int numEv)
  {
    _LOG.debug(String.format("rinsing with volume '%s' from isolation valve '%s'",
        volume, numEv));
    // attention si volume = _volumeMaxSeringue
    // volume + VOL_AJUSTEMENT > _volumeMaxSeringue
    // voir algoAspiration
    if ((volume + VOL_AJUSTEMENT) <= this._volumeMaxSeringue)
    {
      volume += VOL_AJUSTEMENT ;
    }

    this.algoAspiration(volume, numEv) ;

    this.flagVolSecu = false ;
  }

  //réglage du débit à l'aspiration en ml / min
  //requires debit <= _debitMaxPousseSeringue
  public void setDebitAspiration(double debit)

  { 
    _LOG.debug(String.format("setting the withdrawing rate to '%s'", debit)); 
    if (debit > this._debitMaxPousseSeringue )
    {
      String msg = String.format("the withdrawing rate '%s' cannot be greater than the maximum rate %s",
          debit, this._debitMaxPousseSeringue);
      _LOG.fatal(msg);
      throw new RuntimeException(msg);
    }

    // tient compte du nombre de seringue.
    debit = debit/_nbSeringue ;

    if (debit != debitActuelAspiration)
    {
      try
      {
        this.interfacePousseSeringue.ratew(debit) ;
      }
      catch(SerialComException e)
      {
        String msg = String.format("error while configuring the withdrawing rate '%s': %s",
            debit, e.getMessage());
        _LOG.fatal(msg);
        throw new RuntimeException(msg, e);
      }
      
      this.debitActuelAspiration = debit ;
    }
  }

  // réglage du débit au refoulement en ml / min.
  // requires debit <= _debitMaxPousseSeringue
  public void setDebitRefoulement(double debit)
  {  
    _LOG.debug(String.format("setting the infusion rate to '%s'", debit));
    if(debit > this._debitMaxPousseSeringue)
    {
      String msg = String.format("the value of the infusion rate '%s' cannot be greater than the maximum rate %s",
                                 debit, this._debitMaxPousseSeringue);
      _LOG.fatal(msg);
      throw new RuntimeException(msg);
    }

    debit = debit/_nbSeringue ;

    if (debit != this.debitActuelRefoulement)
    {  
      try
      {
        this.interfacePousseSeringue.ratei(debit) ;
      }
      catch(SerialComException e)
      {
        String msg = String.format("error while configuring the infusion rate '%s': %s",
            debit, e.getMessage());
        _LOG.fatal(msg);
        throw new RuntimeException(msg, e);
      }
      
      this.debitActuelRefoulement = debit ;
    }
  }
  
  public void finPompage()
  {
    this.finPompage(false);
  }
  
  // attente de la fin de l'aspiration ou refoulement
  public void finPompage(boolean fermeture)
  {
    _LOG.debug(String.format("waiting for the pump (close flag is '%s')", fermeture));
    boolean has_to_continue = false;
    do
    {
      try
      {
        Thread.sleep(100) ;
      }
      catch (InterruptedException e)
      {
        String msg = String.format("error while waiting for the pump: %s",
            e.getMessage());
        _LOG.error(msg);
      }
      
      try
      {
        has_to_continue = this.interfacePousseSeringue.running();
      }
      catch(SerialComException e)
      {
        String msg = String.format("error while waiting for the pump: %s",
            e.getMessage());
        _LOG.fatal(msg);
        throw new RuntimeException(msg, e);
      }
    }
    while (has_to_continue) ;

    // équilibre de la pression dans les tuyaux.
    try
    {
      Thread.sleep(ATTENTE_FERMETURE_EV) ;
    }
    catch (InterruptedException e)
    {
      String msg = String.format("error while waiting for the pump: %s",
          e.getMessage());
      _LOG.error(msg);
    }

    if (fermeture)
    {
      // ferme ttes les ev  désactivation pour éviter depression dans le tube.
      this.fermetureEv();
      // pendant distribution <=> perte précison
      numEvActuelle = 0 ;
      // équilibre de la pression dans les tuyaux
      try
      {
        Thread.sleep(ATTENTE_FERMETURE_EV) ;
      }
      catch (InterruptedException e)
      {
        String msg = String.format("error while waiting for the pump: %s",
            e.getMessage());
        _LOG.error(msg);
      }
    }
  }

  // Arrête de la pompe
  public void pause()
  {
    try
    {
      _LOG.debug("stopping the pump (pause)");
      if (this.interfacePousseSeringue.running())
      {  
        this.interfacePousseSeringue.stop();
        //ferme ttes les ev
        this.fermetureEv();
        this._pause = true ;
      }
      else
      {
        this._pause = false ;
      }
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while stopping (pause) the pump: %s",
          e.getMessage());
      _LOG.fatal(msg);
      throw new RuntimeException(msg, e);
    }
  }

  // reprise après pause
  public void reprise()
  {  
    if (this._pause)
    { 
      try
      {
        _LOG.debug("resuming the pump");
        this.para.ouvrir(this.numEvActuelle);
        this.interfacePousseSeringue.run();
        this._pause = false ;
      }
      catch(SerialComException e)
      {
        String msg = String.format("error while resuming the pump: %s",
            e.getMessage());
        _LOG.fatal(msg);
        throw new RuntimeException(msg, e);
      }
    }
    else
    {
      _LOG.debug("ignore resuming the pump");
    }
  }

  // règle le diamètre de la seringue en mm.
  public void setDiametreSeringue(double diametre)
  {
    try
    {
      _LOG.debug(String.format("setting the syringe diameter to '%s'", diametre));
      this.interfacePousseSeringue.dia(diametre);
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while setting the diameter of the seringe '%s': %s",
          diametre, e.getMessage());
      _LOG.fatal(msg);
      throw new RuntimeException(msg, e);
    }
  }

  // retourne le volume déjà délivré.
  public double volumeDelivre() 
  { 
    try
    {
      double result = this.interfacePousseSeringue.deliver() * this._nbSeringue ;
      _LOG.debug(String.format("getting '%s' of delivered volume", result));
      return result ;
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while getting the delivered volume: %s",
          e.getMessage());
      _LOG.fatal(msg);
      throw new RuntimeException(msg, e);
    }
  }

  // précondition : le threadSequence doit être détruit ( pthread_cancel ) ou inexistant
  // précondition non vérifiée !!!
  public void arretUrgence()
  {
    _LOG.debug("pump emergency shutdown");
    //précondition : le threadSequence doit être détruit ( pthread_cancel ) ou inexistant
    this.interfacePousseSeringue.arretUrgence();
    // ferme ttes les ev
    this.fermetureEv();
  }

  //renvoie le débit max arrondi à l'inférieur.
  //ce débit est indépendant des caractéristiques de la tuyauterie
  //dépend uniquement du diamètre de la seringue
  public static int debitMaxIntrinseque ( double diametreSeringue )
  {  
    return  InterfacePousseSeringue.debitMaxIntrinseque(diametreSeringue);
  }

  // volume utile total des seringues.
  public double volumeRestant()
  { 
    double volumeUtile = PousseSeringue._volumeReel ;

    if (flagVolSecu) 
    {
      volumeUtile -= VOL_SECU  ;
    }

    double result = (volumeUtile  * _nbSeringue) ;
    _LOG.debug(String.format("total raiming volume is '%s'", result));
    return result;                      
  }

  // vide la seringue entièrement même le volume de sécurité dans le tuyau de refoulement.
  // vide entièrement la seringue, volume de sécurité compris.
  public void vidange()  
  { 
    _LOG.debug("draining the pump");
    // retour aux conditions initiales.
    this.flagVolSecu = false ;
    
    // vidange.
    this.algoRefoulement(PousseSeringue._volumeReel, ParaCom.NUM_EV_REFOULEMENT);
  }

  public static int numEvRefoulement()
  { 
    return ParaCom.NUM_EV_REFOULEMENT ;
  }

  public static int numEvH2O()
  { 
    return ParaCom.NUM_EV_H2O ;
  }

  // renvoie le volume utile des n seringues moins VOL_SECU et VOL-AJUSTEMENT
  // par seringues.
  public double volumeMaxSeringueUtile()
  {  
    //volume Max utile pour les n seringues !
    double result = (this._nbSeringue * (this._volumeMaxSeringue - VOL_SECU - VOL_AJUSTEMENT ) );
    _LOG.debug(String.format("computed the maximum util volume is '%s'", result));
    return  result ;
  }

  public double volumeAjustement()
  {  
    return VOL_AJUSTEMENT ;
  }

  public double volumeSecurite()
  {  
    return VOL_SECU ;
  }

  // fermeture des Ev Commandées.
  public void fermetureEv()
  {  
    try
    {
      this.para.toutFermer() ;
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while closing all the isolation valves: %s",
          e.getMessage());
      _LOG.fatal(msg);
      throw new RuntimeException(msg);
    }
  }  
  
  @Override
  public void close() throws IOException
  {
    this.interfacePousseSeringue.close();
    this.para.close();
  }
  
  public static void main(String[] args)
  {

  }
}
