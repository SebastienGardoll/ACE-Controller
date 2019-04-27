package fr.gardoll.ace.controller.common;

import java.net.URISyntaxException ;
import java.nio.file.Files ;
import java.nio.file.Path ;
import java.text.DecimalFormatSymbols ;
import java.util.Locale ;

import org.apache.commons.configuration2.INIConfiguration ;
import org.apache.commons.configuration2.SubnodeConfiguration ;
import org.apache.commons.configuration2.builder.fluent.Configurations ;
import org.apache.commons.configuration2.ex.ConfigurationException ;
import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.pump.PousseSeringue ;
import fr.gardoll.ace.controller.sampler.Passeur ;

// TODO: add logging
public class ParametresSession
{
  private static final Logger _LOG = LogManager.getLogger(ParametresSession.class.getName());
  
  public final static DecimalFormatSymbols DECIMAL_SYMBOLS =
      new DecimalFormatSymbols(Locale.FRANCE);
  
  public final static int NB_POSITION = 6 ;
  
  private static ParametresSession _INSTANCE ;
  
  // Lazy loading.
  private PousseSeringue _ps = null;
  private Passeur _sampler = null;
  
  private final double _volumeMaxSeringue ;  // volume max du type de seringue en mL

  private final double _volumeRincage ;  // volume  utilisé pendant un cylce de rinçage  en mL

  private final int _nbPasCarrousel ;  // nombre de demi pas entre deux emplacement colonnes

  private final int  _refCarrousel; // distance butée haute du bras, plateau supérieur du carrousel en mm

  private final int _nbRincage ; //nombre de rinçage à effectuer au moment de changer d'éluant

  private final double _debitMaxPousseSeringue ;//débit maximum applicable au pousseSeringue en fonction du diamètre de tuyau utilisé !!!

  private final double _diametreSeringue ;//diametre du type de seringue utilisé

  private final int _nbSeringue ; //nombre de seringues utilisées

  private final int _diametreCarrousel ;//diamètre du carrousel en mm

  private final int _epaisseur ;//epaisseur du plateau sup du carrousel

  private final int _nbMaxColonne ; //nombre d'emplacement max de colonnes sur le carrousel choisi
  
  public static ParametresSession getInstance() throws InitializationException
  {
    if (ParametresSession._INSTANCE == null)
    {
      ParametresSession._INSTANCE = new ParametresSession(); 
    }
    
    return ParametresSession._INSTANCE ;
  }
  
  private ParametresSession() throws InitializationException
  {
    _LOG.info("fetch the configuration");
    
    Path rootDir = null ;
    try
    {
      rootDir = Utils.getRootDir(this);
    }
    catch (URISyntaxException e)
    {
      String msg = String.format("unable to fetch the path of the application: %s", e.getMessage());
      _LOG.fatal(msg, e);
      throw new InitializationException(msg, e);
    }
    
    Path configurationFile = rootDir.resolve(Names.CONFIG_FILENAME);
    Path plateConfFile = null;
    
    if (Files.isReadable(configurationFile)    == false ||
        Files.isRegularFile(configurationFile) == false)
    {
      String msg = String.format("unable to read the configuration file '%s'", configurationFile);
      _LOG.fatal(msg);
      throw new InitializationException(msg);
    }
    
    try
    {
      Configurations configs = new Configurations();
      INIConfiguration iniConf = configs.ini(configurationFile.toFile());
      SubnodeConfiguration section = iniConf.getSection(Names.SEC_INFO_POUSSE_SERINGUE);

      this._volumeMaxSeringue      = section.getDouble(Names.SIPS_CLEF_VOL_MAX, -1.0) ;
      this._volumeRincage          = section.getDouble(Names.SIPS_CLEF_VOL_RINCAGE, -1.0) ;
      this._nbRincage              = section.getInteger(Names.SIPS_CLEF_NB_RINCAGE, -1) ;
      this._debitMaxPousseSeringue = section.getDouble(Names.SIPS_CLEF_DEBIT_MAX, -1.0) ;
      this._nbSeringue             = section.getInteger(Names.SIPS_CLEF_NB_SERINGUE,-1);
      this._diametreSeringue       = section.getDouble(Names.SIPS_CLEF_DIA_SERINGUE,-1.0);
      
      section = iniConf.getSection(Names.SEC_INFO_CARROUSEL);
      plateConfFile = Path.of(section.getString(Names.SIC_CLEF_CHEMIN_FICHIER_CARROUSEL, "READ ERROR"));
    }
    catch (ConfigurationException e)
    {
      String msg = String.format("unable to read the configuration file '%s': %s",
          configurationFile.toString(), e.getMessage());
      _LOG.fatal(msg, e);
      throw new InitializationException(msg, e);
    }
    
    if (Files.isReadable(plateConfFile)    == false ||
        Files.isRegularFile(plateConfFile) == false)
    {
      String msg = String.format("unable to read the plate configuration file '%s'", configurationFile);
      _LOG.fatal(msg);
      throw new InitializationException(msg);
    }
    
    try
    {
      Configurations configs = new Configurations();
      INIConfiguration iniConf = configs.ini(configurationFile.toFile());
      SubnodeConfiguration section = iniConf.getSection(Names.SEC_INFO_CARROUSEL);
      
      this._nbPasCarrousel    = section.getInteger(Names.SIC_CLEF_NB_DEMI_PAS, -1) ;
      this._refCarrousel      = section.getInteger(Names.SIC_CLEF_REF_CARROUSEL, -1);
      this._diametreCarrousel = section.getInteger(Names.SIC_CLEF_DIA, -1);
      this._epaisseur         = section.getInteger(Names.SIC_CLEF_EPAISSEUR, -1);
      this._nbMaxColonne      = section.getInteger(Names.SIC_CLEF_NB_COL , -1) ;
    }
    catch (ConfigurationException e)
    {
      String msg = String.format("unable to read the plate configuration file '%s': %s",
          configurationFile.toString(), e.getMessage());
      _LOG.fatal(msg, e);
      throw new InitializationException(msg, e);
    }
    
    if (this._volumeMaxSeringue      < 0 ||
        this._volumeRincage          < 0 ||
        this._nbRincage              < 0 ||
        this._debitMaxPousseSeringue < 0 ||
        this._nbSeringue             < 0 ||
        this._diametreSeringue       < 0 ||
        this._nbPasCarrousel         < 0 ||
        this._refCarrousel           < 0 ||
        this._diametreCarrousel      < 0 ||
        this._epaisseur              < 0 ||
        this._nbMaxColonne           < 0    )
    {
      String msg = String.format("corrupted configuration file or plate configuration file");
      _LOG.fatal(msg);
      throw new InitializationException(msg);
    }
  }

  // Lazy loading.
  public PousseSeringue getPousseSeringue()
  {
    // TODO Auto-generated method stub
    return null ;
  }
  
  // Lazy loading.
  public Passeur getPasseur()
  {
    // TODO Auto-generated method stub
    return null ;
  }

  public double volumeMaxSeringue()
  {
    return this._volumeMaxSeringue ;
  }

  public double volumeRincage()
  {
    return this._volumeRincage ;
  }

  public int nbPasCarrousel()
  {
    return this._nbPasCarrousel ;
  }

  public int refCarrousel()
  {
    return this._refCarrousel ;
  }

  public int nbRincage()
  {
    return this._nbRincage ;
  }

  public double debitMaxPousseSeringue()
  {
    return this._debitMaxPousseSeringue ;
  }

  public double diametreSeringue()
  {
    return this._diametreSeringue ;
  }

  public int nbSeringue()
  {
    return this._nbSeringue ;
  }

  public int diametreCarrousel()
  {
    return this._diametreCarrousel ;
  }

  public int epaisseur()
  {
    return this._epaisseur ;
  }

  public int nbMaxColonne()
  {
    return this._nbMaxColonne ;
  }
}


/*

  : pousseSeringue (parametresSession.nbSeringue() , parametresSession.diametreSeringue(),
                    parametresSession.volumeMaxSeringue() , parametresSession.debitMaxPousseSeringue() )  ,

    passeur (parametresSession.nbPasCarrousel(), parametresSession.diametreCarrousel())


*/