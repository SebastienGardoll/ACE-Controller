package fr.gardoll.ace.controller.pump;

import java.io.Closeable ;
import java.text.DecimalFormatSymbols ;
import java.util.Locale ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.ConfigurationException ;
import fr.gardoll.ace.controller.core.SerialComException ;
import fr.gardoll.ace.controller.core.ThreadControl ;

public interface PumpController extends Closeable
{
  public static final Logger _LOG = LogManager.getLogger(PumpController.class.getName());
  
  // caractéristique du pousse seringue en m/min
  public final static double COURCE_LINEAIRE_MAX = 0.1269 ;
  // diamètre de seringue maximum pour le pousse seringue en mm
  public final static int DIAMETRE_MAX = 140 ;
  
  public final static DecimalFormatSymbols DECIMAL_SYMBOLS =
      new DecimalFormatSymbols(Locale.US);
  
  public void setThreadControl(ThreadControl threadCtrl);
  
  public void run() throws SerialComException, InterruptedException;
  
  // pause or cancel
  public void stop() throws SerialComException, InterruptedException;
  
  // en mm requires diametre > 0
  public void dia(double diametre) throws SerialComException, InterruptedException;
  
  public boolean running() throws SerialComException, InterruptedException;
  
  // en mL
  //Attention ne revoie un réel que si le volume à délivré en est un.
  // Ainsi : 1. ou 1.0 ne donnera pas de réponse en réel donc la réponse sera
  // 0 puis 1 à la fin !!!! Il n'y a donc aucun intérêt.
  // Parade : passer en micro litre quand < 10 mL.
  public double deliver() throws SerialComException, InterruptedException;
  
  // en mL/min   requires 0 < debit <= _debitMaxIntrinseque
  public void ratei(double debit) throws SerialComException, InterruptedException;
  
  //en mL/min   requires 0 < debit <= _debitMaxIntrinseque
  public void ratew(double debit) throws SerialComException, InterruptedException;
  
  // en mL seulement 4 caractères sans compter la virgule.
  // requires volume > 0
  public void voli(double volume) throws SerialComException, InterruptedException;
  
  // en mL seulement 4 caractères sans compter la virgule.
  // requires volume > 0
  public void volw(double volume) throws SerialComException, InterruptedException;
  
  public void modeI() throws SerialComException, InterruptedException;

  public void modeW() throws SerialComException, InterruptedException;
  
  public static int debitMaxIntrinseque(double diametreSeringue)
  {
    _LOG.debug(String.format("computing the maxium rate based on the syringe diameter '%s'",
        diametreSeringue));
    if (diametreSeringue <= 0)
    {
      String msg = String.format("the value of the syringe diameter '%s' cannot be negative or null",
                                 diametreSeringue) ;
      _LOG.fatal(msg);
      throw new ConfigurationException(msg);
    }
    else if (diametreSeringue > DIAMETRE_MAX)
    {
      String msg = String.format("the value of the syringe diameter '%s' cannot be greater than %s",
                                 diametreSeringue, DIAMETRE_MAX);
      _LOG.fatal(msg);
      throw new ConfigurationException (msg);
    }
    
    double result = Math.pow(diametreSeringue/2. , 2.) * Math.PI * COURCE_LINEAIRE_MAX  ;
    // arrondi par à l'entier inférieur à cause spec du pousse seringue .
    
    _LOG.debug(String.format("getting '%s' rounded to '%s'", result, (int)result));
    
    return (int) (result) ;
  }
}
