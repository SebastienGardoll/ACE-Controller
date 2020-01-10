package fr.gardoll.ace.controller.pump;

import java.io.Closeable ;
import java.text.DecimalFormatSymbols ;
import java.util.Locale ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.com.SerialComException ;
import fr.gardoll.ace.controller.core.ConfigurationException ;

public interface PumpController extends Closeable
{
  public static final Logger _LOG = LogManager.getLogger(PumpController.class.getName());
  
  // caractéristique du pousse seringue en m/min
  public final static double COURCE_LINEAIRE_MAX = 0.1269 ;
  // diamètre de seringue maximum pour le pousse seringue en mm
  public final static int DIAMETRE_MAX = 140 ;
  
  public final static DecimalFormatSymbols DECIMAL_SYMBOLS =
      new DecimalFormatSymbols(Locale.US);
  
  public void run() throws SerialComException;
  
  // pause or cancel
  public void stop() throws SerialComException;
  
  // en mm requires diametre > 0
  public void dia(double diametre) throws SerialComException;
  
  public boolean running() throws SerialComException;
  
  // en mL
  //Attention ne revoie un réel que si le volume à délivré en est un.
  // Ainsi : 1. ou 1.0 ne donnera pas de réponse en réel donc la réponse sera
  // 0 puis 1 à la fin !!!! Il n'y a donc aucun intérêt.
  // Parade : passer en micro litre quand < 10 mL.
  public double deliver() throws SerialComException;
  
  // en mL/min   requires 0 < debit <= _debitMaxIntrinseque
  public void ratei(double debit) throws SerialComException;
  
  //en mL/min   requires 0 < debit <= _debitMaxIntrinseque
  public void ratew(double debit) throws SerialComException;
  
  // en mL seulement 4 caractères sans compter la virgule.
  // requires volume > 0
  public void voli(double volume) throws SerialComException;
  
  // en mL seulement 4 caractères sans compter la virgule.
  // requires volume > 0
  public void volw(double volume) throws SerialComException;
  
  public void modeI() throws SerialComException;

  public void modeW() throws SerialComException;
  
  public static int debitMaxIntrinseque(double diametreSeringue)
    throws ConfigurationException
  {
    _LOG.debug(String.format("computing the maxium rate based on the syringe diameter '%s'",
        diametreSeringue));
    if (diametreSeringue <= 0.)
    {
      String msg = String.format("the value of the syringe diameter '%s' cannot be negative or null",
                                 diametreSeringue) ;
      throw new ConfigurationException(msg);
    }
    else if (diametreSeringue > DIAMETRE_MAX)
    {
      String msg = String.format("the value of the syringe diameter '%s' cannot be greater than %s",
                                 diametreSeringue, DIAMETRE_MAX);
      throw new ConfigurationException (msg);
    }
    
    double result = Math.pow(diametreSeringue/2. , 2.) * Math.PI * COURCE_LINEAIRE_MAX  ;
    // arrondi par à l'entier inférieur à cause spec du pousse seringue .
    
    _LOG.debug(String.format("getting '%s' rounded to '%s'", result, (int)result));
    
    return (int) (result) ;
  }

  public String getPortPath() ;
}
