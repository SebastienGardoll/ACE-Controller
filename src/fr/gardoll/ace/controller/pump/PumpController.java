package fr.gardoll.ace.controller.pump;

import java.io.Closeable ;
import java.text.DecimalFormatSymbols ;
import java.util.Locale ;

import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.com.SerialComException ;
import fr.gardoll.ace.controller.core.Log ;
import fr.gardoll.ace.controller.settings.ConfigurationException ;
import fr.gardoll.ace.controller.settings.GeneralSettings ;

public interface PumpController extends Closeable
{
  public static final Logger _LOG = Log.HIGH_LEVEL;
  
  // caractéristique du pousse seringue en m/min
  public final static double COURCE_LINEAIRE_MAX = 0.1269 ;
  
  public final static DecimalFormatSymbols DECIMAL_SYMBOLS =
      new DecimalFormatSymbols(Locale.US);
  
  public void run() throws SerialComException;
  
  // pause or cancel
  public void stop() throws SerialComException;
  
  // en mm requires diametre > 0
  public void dia(double diametre) throws SerialComException, ConfigurationException;
  
  public boolean running() throws SerialComException;
  
  // en mL
  //Attention ne revoie un réel que si le volume à délivré en est un.
  // Ainsi : 1. ou 1.0 ne donnera pas de réponse en réel donc la réponse sera
  // 0 puis 1 à la fin !!!! Il n'y a donc aucun intérêt.
  // Parade : passer en micro litre quand < 10 mL.
  public double deliver() throws SerialComException;
  
  // en mL/min   requires 0 < debit <= _debitMaxIntrinseque
  public void ratei(double debit) throws SerialComException, ConfigurationException;
  
  //en mL/min   requires 0 < debit <= _debitMaxIntrinseque
  public void ratew(double debit) throws SerialComException, ConfigurationException;
  
  // en mL seulement 4 caractères sans compter la virgule.
  // requires volume > 0
  public void voli(double volume) throws SerialComException, ConfigurationException;
  
  // en mL seulement 4 caractères sans compter la virgule.
  // requires volume > 0
  public void volw(double volume) throws SerialComException, ConfigurationException;
  
  public void modeI() throws SerialComException;

  public void modeW() throws SerialComException;
  
  // renvoie le débit max arrondi à l'inférieur.
  // ce débit est indépendant des caractéristiques de la tuyauterie
  // dépend uniquement du diamètre de la seringue
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
    else if (diametreSeringue > GeneralSettings.MAX_SYRINGE_DIAMETER)
    {
      String msg = String.format("the value of the syringe diameter '%s' cannot be greater than %s",
                                 diametreSeringue, GeneralSettings.MAX_SYRINGE_DIAMETER);
      throw new ConfigurationException (msg);
    }
    
    double rawResult = Math.pow(diametreSeringue/2. , 2.) * Math.PI * COURCE_LINEAIRE_MAX  ;
    // arrondi par à l'entier inférieur à cause spec du pousse seringue .
    int result = (int) (rawResult);
    
    _LOG.debug(String.format("computed max rate is '%s' rounded to '%s'", rawResult, result));
    
    return result ;
  }

  public String getPortPath() ;
}
