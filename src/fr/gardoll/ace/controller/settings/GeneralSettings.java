package fr.gardoll.ace.controller.settings;

import java.nio.file.Path ;
import java.nio.file.Paths ;

import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.pump.PousseSeringue ;
import fr.gardoll.ace.controller.pump.PumpController ;

public class GeneralSettings extends Settings
{
  private static GeneralSettings _INSTANCE = null;
  
  private static final Path _PROPERTY_FILE_PATH ;
  
  private static final String SEC_INFO_POUSSE_SERINGUE          = "informationsPousseSeringue";
  private static final String SIPS_CLEF_SERIAL_COM_CLASS_PATH   = "serialComClassPath";
  private static final String SIPS_CLEF_PORT_PATH               = "portPath";
  private static final String SIPS_CLEF_VOL_MAX                 = "volumeMax";
  private static final String SIPS_CLEF_VOL_RINCAGE             = "volumeRincage";
  private static final String SIPS_CLEF_NB_RINCAGE              = "nombreRincage";
  private static final String SIPS_CLEF_DEBIT_MAX               = "debitMax";
  private static final String SIPS_CLEF_NB_SERINGUE             = "nombreSeringue";
  private static final String SIPS_CLEF_DIA_SERINGUE            = "diametreSeringue";
  
  public static final double DEFAULT_MAX_SYRINGE_VOLUME   = 100.; // in mL
  public static final double DEFAULT_MAX_PUMP_MAX_RATE    = 15.;  // in mL/min
  public static final double DEFAULT_MAX_RINSE_VOLUME     = 100.; // in mL
  
  // diamètre de seringue maximum pour le pousse seringue en mm
  public static final double MAX_SYRINGE_DIAMETER = 140.; // in mm
  
  public static final double DEFAULT_MIN_PUMP_MAX_RATE    = 0.25; // in mL/min
  public static final double DEFAULT_MIN_RINSE_VOLUME     = 0.25; // in mL
  public static final double DEFAULT_MIN_SYRINGE_DIAMETER = 1.; // in mm
  
  public static final int DEFAULT_MIN_RINSE_NB = 1;
  public static final int DEFAULT_MAX_RINSE_NB = 10;
  
  public static final double MIN_SYRINGE_VOLUME = 
      PousseSeringue.volumeAjustement() + PousseSeringue.volumeSecurite() ;
  
  
  
  private static final String SEC_ACE_CONTROLLER                = "ace_controller_settings";
  private static final String SAC_IS_DEBUG                      = "is_debug";
  private static final String SAC_IS_FULL_SCREEN                = "is_fullScreen";
  
  
  private static final String SEC_INFO_PARA_COM                 = "informationsParacom";
  private static final String SIPC_CLEF_PARA_COM_CLASS_PATH     = "paraComClassPath";
  private static final String SIPC_CLEF_SERIAL_COM_CLASS_PATH   = "serialComClassPath";
  private static final String SIPC_CLEF_PORT_PATH               = "portPath";
  
  
  private static final String SEC_INFO_CARROUSEL                = "informationsCarrousel";
  private static final String SIC_CLEF_SERIAL_COM_CLASS_PATH    = "serialComClassPath";
  private static final String SIC_CLEF_PORT_PATH                = "portPath";
  private static final String SIC_CLEF_CHEMIN_FICHIER_CARROUSEL = "cheminFichierCarrousel";
  
  static
  {
    _PROPERTY_FILE_PATH = Paths.get(Names.CONFIG_DIRNAME,
                                    Names.CONFIG_FILENAME);
  }

  private CarouselSettings _carouselSettings ;
  
  private GeneralSettings() throws ConfigurationException
  {
    super(_PROPERTY_FILE_PATH);
    String carouselFilePath = this.getCarouselConfFilePath();
    this.init(carouselFilePath);
  }
  
  private void init(String filePath) throws ConfigurationException
  {
    this._carouselSettings = new CarouselSettings(Paths.get(filePath));
  }

  public static GeneralSettings instance() throws ConfigurationException
  {
    if(_INSTANCE == null)
    {
      _INSTANCE = new GeneralSettings();
    }
    
    return _INSTANCE;
  }
  
  public void checkAutosamplerConfPortPath(String path) throws ConfigurationException
  {
    if(path.isBlank())
    {
      String msg = "autosampler port path is missing";
      throw new ConfigurationException(msg);
    }
  }
  
  public String getAutosamplerConfPortPath() throws ConfigurationException
  {
    return (String) this.fetchValue(SEC_INFO_CARROUSEL,
                                    SIC_CLEF_PORT_PATH,
                                    String.class);
  }
  
  public void setAutosamplerConfPortPath(String path) throws ConfigurationException
  {
    this.checkAutosamplerConfPortPath(path);
    this.setValue(SEC_INFO_CARROUSEL,
                  SIC_CLEF_PORT_PATH,
                  path);
  }
  
  public void checkAutosamplerSerialComClassPath(String path) throws ConfigurationException
  {
    if(path.isBlank())
    {
      String msg = "autosampler serial class path is missing";
      throw new ConfigurationException(msg);
    }
  }
  
  public String getAutosamplerSerialComClassPath() throws ConfigurationException
  {
    return (String) this.fetchValue(SEC_INFO_CARROUSEL,
                                    SIC_CLEF_SERIAL_COM_CLASS_PATH,
                                    String.class);
  }
  
  public void setAutosamplerSerialComClassPath(String path) throws ConfigurationException
  {
    this.checkAutosamplerSerialComClassPath(path);
    this.setValue(SEC_INFO_CARROUSEL,
                  SIC_CLEF_SERIAL_COM_CLASS_PATH,
                  path);
  }
  
  public void checkCarouselConfFilePath(String path) throws ConfigurationException
  {
    if(path.isBlank())
    {
      String msg = "carousel conf file path is missing";
      throw new ConfigurationException(msg);
    }
    
    // Check the file existence.
    CarouselSettings tmp = new CarouselSettings(Paths.get(path));
    tmp.close();
  }
  
  public String getCarouselConfFilePath() throws ConfigurationException
  {
    return (String) this.fetchValue(SEC_INFO_CARROUSEL,
                                    SIC_CLEF_CHEMIN_FICHIER_CARROUSEL,
                                    String.class);
  }
  
  public void setCarouselConfFilePath(String path) throws ConfigurationException
  {
    this.checkCarouselConfFilePath(path);
    this.setValue(SEC_INFO_CARROUSEL,
                  SIC_CLEF_CHEMIN_FICHIER_CARROUSEL,
                  path);
    
    this.init(path);
  }  
  
  public void checkParaComConfPortPath(String path) throws ConfigurationException
  {
    if(path.isBlank())
    {
      String msg = "paracom port path is missing";
      throw new ConfigurationException(msg);
    }
  }
  
  public String getParaComConfPortPath() throws ConfigurationException
  {
    return (String) this.fetchValue(SEC_INFO_PARA_COM,
                                    SIPC_CLEF_PORT_PATH,
                                    String.class);
  }
  
  public void setParaComConfPortPath(String path) throws ConfigurationException
  {
    this.checkParaComConfPortPath(path);
    this.setValue(SEC_INFO_PARA_COM,
                  SIPC_CLEF_PORT_PATH,
                  path);
  }
  
  public void checkParaComSerialComClassPath(String path) throws ConfigurationException
  {
    if(path.isBlank())
    {
      String msg = "paracom serial class path is missing";
      throw new ConfigurationException(msg);
    }
  }
  
  public String getParaComSerialComClassPath() throws ConfigurationException
  {
    return (String) this.fetchValue(SEC_INFO_PARA_COM,
                                    SIPC_CLEF_SERIAL_COM_CLASS_PATH,
                                    String.class);
  }
  
  public void setParaComSerialComClassPath(String path) throws ConfigurationException
  {
    this.checkParaComSerialComClassPath(path);
    this.setValue(SEC_INFO_PARA_COM,
                  SIPC_CLEF_SERIAL_COM_CLASS_PATH,
                  path);
  }  
  
  public void checkParaComClassPath(String path) throws ConfigurationException
  {
    if(path.isBlank())
    {
      String msg = " path is missing";
      throw new ConfigurationException(msg);
    }
  }
  
  public String getParaComClassPath() throws ConfigurationException
  {
    return (String) this.fetchValue(SEC_INFO_PARA_COM,
                                    SIPC_CLEF_PARA_COM_CLASS_PATH,
                                    String.class);
  }
  
  public void setParaComClassPath(String path) throws ConfigurationException
  {
    this.checkParaComClassPath(path);
    this.setValue(SEC_INFO_PARA_COM,
                  SIPC_CLEF_PARA_COM_CLASS_PATH,
                  path);
  }  
  
  public void checkFullScreenMode(boolean fullScreenMode) throws ConfigurationException
  {
    // Nothing to do.
  }
  
  public boolean isFullScreen() throws ConfigurationException
  {
    String rawValue = (String) this.fetchValue(SEC_ACE_CONTROLLER,
                                               SAC_IS_FULL_SCREEN,
                                               String.class);
    return Names.TRUE.equals(rawValue);
  }
  
  public void setFullScreenMode(boolean fullScreenMode) throws ConfigurationException
  {
    this.checkFullScreenMode(fullScreenMode);
    
    String translation = (fullScreenMode)?Names.TRUE:Names.FALSE;
    
    this.setValue(SEC_ACE_CONTROLLER,
                  SAC_IS_FULL_SCREEN,
                  translation);
  }  
  
  public void checkDebugMode(boolean debugMode) throws ConfigurationException
  {
    // Nothing to do.
  }
  
  public boolean isDebug() throws ConfigurationException
  {
    String rawValue = (String) this.fetchValue(SEC_ACE_CONTROLLER,
                                               SAC_IS_DEBUG,
                                               String.class);
    
    return Names.TRUE.equals(rawValue);
  }
  
  public void setDebugMode(boolean debugMode) throws ConfigurationException
  {
    this.checkDebugMode(debugMode);
    
    String translation = (debugMode)?Names.TRUE:Names.FALSE;
    
    this.setValue(SEC_ACE_CONTROLLER,
                  SAC_IS_DEBUG,
                  translation);
  }  
  
  public void checkNbRincage(int nbRinse) throws ConfigurationException
  {
    if(nbRinse < DEFAULT_MIN_RINSE_NB)
    {
      String msg = String.format("the number of rinses (got '%s') cannot be less than %s",
          nbRinse, DEFAULT_MIN_RINSE_NB);
      throw new ConfigurationException(msg);
    }
    
    if(nbRinse > DEFAULT_MAX_RINSE_NB)
    {
      String msg = String.format("the number of rinses (got '%s') cannot be greater than %s",
          nbRinse, DEFAULT_MAX_RINSE_NB);
      throw new ConfigurationException(msg);
    }
  }
  
  public int getNbRincage() throws ConfigurationException
  {
    return (int) this.fetchValue(SEC_INFO_POUSSE_SERINGUE,
                                 SIPS_CLEF_NB_RINCAGE,
                                 Integer.class);
  }
  
  public void setNbRincage(int nbRinse) throws ConfigurationException
  {
    this.checkNbRincage(nbRinse);
    this.setValue(SEC_INFO_POUSSE_SERINGUE,
                  SIPS_CLEF_NB_RINCAGE,
                  nbRinse);
  }

  public void checkVolumeMaxSeringue(double syringeVolume, double rinseVolume) throws ConfigurationException
  {
    if(syringeVolume <= MIN_SYRINGE_VOLUME)
    {
      String msg = String.format("the volume of the syringe (got '%s') must be greater than %s",
          syringeVolume, MIN_SYRINGE_VOLUME);
      throw new ConfigurationException(msg);
    }
    
    if(syringeVolume > DEFAULT_MAX_SYRINGE_VOLUME)
    {
      String msg = String.format("the volume of the syringe (got '%s') cannot be greater than %s",
          syringeVolume, DEFAULT_MAX_SYRINGE_VOLUME);
      throw new ConfigurationException(msg);
    }
    
    if(syringeVolume < rinseVolume)
    {
      String msg = String.format("the volume of rinse (got '%s') cannot be greater than the volume of the syringe (%s)",
          rinseVolume, syringeVolume);
      throw new ConfigurationException(msg);
    }
  }
  
  public double getVolumeMaxSeringue() throws ConfigurationException
  {
    return (double) this.fetchValue(SEC_INFO_POUSSE_SERINGUE,
                                    SIPS_CLEF_VOL_MAX,
                                    Double.class);
  }
  
  public void setVolumeMaxSeringue(double syringeVolume, double rinseVolume) throws ConfigurationException
  {
    this.checkVolumeMaxSeringue(syringeVolume, rinseVolume);
    this.setValue(SEC_INFO_POUSSE_SERINGUE,
                  SIPS_CLEF_VOL_MAX,
                  syringeVolume);
  }
  
  public void checkVolumeRincage(double rinseVolume, double syringeVolume) throws ConfigurationException
  {
    if(rinseVolume < DEFAULT_MIN_RINSE_VOLUME)
    {
      String msg = String.format("the rinse volume (got '%s') cannot be less than %s",
          rinseVolume, DEFAULT_MIN_RINSE_VOLUME);
      throw new ConfigurationException(msg);
    }
    
    if(rinseVolume > DEFAULT_MAX_RINSE_VOLUME)
    {
      String msg = String.format("the rinse volume (got '%s') cannot be greater than %s",
          rinseVolume, DEFAULT_MAX_RINSE_VOLUME);
      throw new ConfigurationException(msg);
    }
    
    if(false == Utils.isDividableBy250(rinseVolume))
    {
      String msg = String.format("the volume of rinse must be dividable by 0.25 (got '%s')", rinseVolume);
      throw new ConfigurationException(msg);
    }
    
    if(syringeVolume < rinseVolume)
    {
      String msg = String.format("the volume of rinse (got '%s') cannot be greater than the volume of the syringe (%s)",
          rinseVolume, syringeVolume);
      throw new ConfigurationException(msg);
    }
  }
  
  public double getVolumeRincage() throws ConfigurationException
  {
    return (double) this.fetchValue(SEC_INFO_POUSSE_SERINGUE,
                                    SIPS_CLEF_VOL_RINCAGE,
                                    Double.class);
  }
  
  public void setVolumeRincage(double rinseVolume, double syringeVolume) throws ConfigurationException
  {
    this.checkVolumeRincage(rinseVolume, syringeVolume);
    this.setValue(SEC_INFO_POUSSE_SERINGUE,
                  SIPS_CLEF_VOL_RINCAGE,
                  rinseVolume);
  }
  
  public void checkDiametreSeringue(double syringeDiameter, double pumpMaxRate) throws ConfigurationException
  {
    if(syringeDiameter < DEFAULT_MIN_SYRINGE_DIAMETER)
    {
      String msg = String.format("the syringe diameter (got '%s') cannot be less than %s",
          syringeDiameter, DEFAULT_MIN_SYRINGE_DIAMETER);
      throw new ConfigurationException(msg);
    }
    
    if(syringeDiameter > GeneralSettings.MAX_SYRINGE_DIAMETER)
    {
      String msg = String.format("the syringe diameter (got '%s') cannot be greate than %s",
          syringeDiameter, GeneralSettings.MAX_SYRINGE_DIAMETER);
      throw new ConfigurationException(msg);
    }
    
    int inherentPumpMaxRate = PumpController.debitMaxIntrinseque(syringeDiameter);
    
    if(pumpMaxRate > inherentPumpMaxRate)
    {
      String msg = String.format("the maximum rate of the pump (got '%s') cannot be greater than %s (computed for a syringe diameter of %s)", 
          pumpMaxRate, inherentPumpMaxRate, syringeDiameter);
      throw new ConfigurationException(msg);
    }
  }
  
  public double getDiametreSeringue() throws ConfigurationException
  {
    return (double) this.fetchValue(SEC_INFO_POUSSE_SERINGUE,
                                    SIPS_CLEF_DIA_SERINGUE,
                                    Double.class);
  }
  
  public void setDiametreSeringue(double syringeDiameter, double pumpMaxRate) throws ConfigurationException
  {
    this.checkDiametreSeringue(syringeDiameter, pumpMaxRate);
    this.setValue(SEC_INFO_POUSSE_SERINGUE,
                  SIPS_CLEF_DIA_SERINGUE,
                  syringeDiameter);
  }
  
  public void checkDebitMaxPousseSeringue(double pumpMaxRate, double syringeDiameter) throws ConfigurationException
  {
    if(pumpMaxRate < DEFAULT_MIN_PUMP_MAX_RATE)
    {
      String msg = String.format("the pump maximum rate (got '%s') cannot be less than %s",
          pumpMaxRate, DEFAULT_MIN_PUMP_MAX_RATE);
      throw new ConfigurationException(msg);
    }
    
    if(pumpMaxRate > DEFAULT_MAX_PUMP_MAX_RATE)
    {
      String msg = String.format("the pump maximum rate (got '%s') cannot be greater than %s",
          pumpMaxRate, DEFAULT_MAX_PUMP_MAX_RATE);
      throw new ConfigurationException(msg);
    }
    
    if(false == Utils.isDividableBy250(pumpMaxRate))
    {
      String msg = String.format("the pump rate must be dividable by 0.25 (got '%s')", pumpMaxRate);
      throw new ConfigurationException(msg);
    }
    
    int inherentPumpMaxRate = PumpController.debitMaxIntrinseque(syringeDiameter);
    
    if(pumpMaxRate > inherentPumpMaxRate)
    {
      String msg = String.format("the maximum rate of the pump (got '%s') cannot be greater than %s (computed for a syringe diameter of %s)", 
          pumpMaxRate, inherentPumpMaxRate, syringeDiameter);
      throw new ConfigurationException(msg);
    }
  }
  
  public double getDebitMaxPousseSeringue() throws ConfigurationException
  {
    return (double) this.fetchValue(SEC_INFO_POUSSE_SERINGUE,
                                    SIPS_CLEF_DEBIT_MAX,
                                    Double.class);
  }
  
  public void setDebitMaxPousseSeringue(double pumpMaxRate, double syringeDiameter) throws ConfigurationException
  {
    this.checkDebitMaxPousseSeringue(pumpMaxRate, syringeDiameter);
    this.setValue(SEC_INFO_POUSSE_SERINGUE,
                  SIPS_CLEF_DEBIT_MAX,
                  pumpMaxRate);  
  }
  
  public void checkNbSeringue(int nbSyringe) throws ConfigurationException
  {
    if(nbSyringe !=1 && nbSyringe != 2)
    {
      String msg = String.format("the number of syringe must be 1 or 2, not '%s'", nbSyringe);
      throw new ConfigurationException(msg);
    }
  }
  
  public int getNbSeringue() throws ConfigurationException
  {
    return (int) this.fetchValue(SEC_INFO_POUSSE_SERINGUE,
                                 SIPS_CLEF_NB_SERINGUE,
                                 Integer.class);
  }
  
  public void setNbSeringue(int nbSyringe) throws ConfigurationException
  {
    this.checkNbSeringue(nbSyringe);
    this.setValue(SEC_INFO_POUSSE_SERINGUE,
                  SIPS_CLEF_NB_SERINGUE,
                  nbSyringe);
  }  
  
  public void checkPumpConfPortPath(String classPath)
      throws ConfigurationException
  {
    if(classPath.isBlank())
    {
      String msg = "pump port path is missing";
      throw new ConfigurationException(msg);
    }
  }
  
  public String getPumpConfPortPath() throws ConfigurationException
  {
    return (String) this.fetchValue(SEC_INFO_POUSSE_SERINGUE,
                                    SIPS_CLEF_PORT_PATH,
                                    String.class);
  }
  
  public void setPumpConfPortPath(String path) throws ConfigurationException
  {
    this.checkPumpConfPortPath(path);
    this.setValue(SEC_INFO_POUSSE_SERINGUE,
                  SIPS_CLEF_PORT_PATH,
                  path);
  }  
  
  public void checkPumpSerialComClassPath(String classPath)
      throws ConfigurationException
  {
    if(classPath.isBlank())
    {
      String msg = "serial com class path is missing";
      throw new ConfigurationException(msg);
    }
  }
  
  public String getPumpSerialComClassPath() throws ConfigurationException
  {
    return (String) this.fetchValue(SEC_INFO_POUSSE_SERINGUE,
                                    SIPS_CLEF_SERIAL_COM_CLASS_PATH,
                                    String.class);
  }
  
  public void setPumpSerialComClassPath(String classPath) throws ConfigurationException
  {
    this.checkPumpSerialComClassPath(classPath);
    this.setValue(SEC_INFO_POUSSE_SERINGUE,
                  SIPS_CLEF_SERIAL_COM_CLASS_PATH,
                  classPath);
  }
  
  public void checkCarouselTweaked(boolean isTweaked) throws ConfigurationException
  {
    this._carouselSettings.checkCarouselTweaked(isTweaked);
  }
  
  public boolean isCarouselTweaked() throws ConfigurationException
  {
    return this._carouselSettings.isCarouselTweaked();
  }
  
  public void setCarouselTweaked(boolean isTweaked) throws ConfigurationException
  {
    this._carouselSettings.setCarouselTweaked(isTweaked);
  }
  
  public void checkCarouselName(String name) throws ConfigurationException
  {
    this._carouselSettings.checkCarouselName(name);
  }
  
  public String getCarouselName() throws ConfigurationException
  {
    return this._carouselSettings.getCarouselName();
  }
  
  public void setCarouselName(String name) throws ConfigurationException
  {
    this._carouselSettings.setCarouselName(name);
  }
  
  public void checkNbMaxColonne(int nbMaxColonne) throws ConfigurationException
  {
    this._carouselSettings.checkNbMaxColonne(nbMaxColonne);
  }
  
  public int getNbMaxColonne() throws ConfigurationException
  {
    return this._carouselSettings.getNbMaxColonne();
  }
  
  public void setNbMaxColonne(int nbMaxColonne) throws ConfigurationException
  {
    this._carouselSettings.setNbMaxColonne(nbMaxColonne);
  }
  
  public void checkEpaisseur(int epaisseur) throws ConfigurationException
  {
    this._carouselSettings.checkEpaisseur(epaisseur);
  }
  
  public int getEpaisseur() throws ConfigurationException
  {
    return this._carouselSettings.getEpaisseur();
  }
  
  public void setEpaisseur(int epaisseur) throws ConfigurationException
  {
    this._carouselSettings.setEpaisseur(epaisseur);
  }
  
  public void checkDiametreCarrousel(int diametreCarrousel) throws ConfigurationException
  {
    this._carouselSettings.checkDiametreCarrousel(diametreCarrousel);
  }
  
  public int getDiametreCarrousel() throws ConfigurationException
  {
    return this._carouselSettings.getDiametreCarrousel();
  }
  
  public void setDiametreCarrousel(int diametreCarrousel) throws ConfigurationException
  {
    this._carouselSettings.setDiametreCarrousel(diametreCarrousel);
  }
  
  public void checkRefCarrousel(int refCarrousel) throws ConfigurationException
  {
    this._carouselSettings.checkRefCarrousel(refCarrousel);
  }
  
  public int getRefCarrousel() throws ConfigurationException
  {
    return this._carouselSettings.getRefCarrousel();
  }
  
  public void setRefCarrousel(int refCarrousel) throws ConfigurationException
  {
    this._carouselSettings.setRefCarrousel(refCarrousel);
  }
  
  public void checkNbPasCarrousel(int nbPasCarrousel) throws ConfigurationException
  {
    this._carouselSettings.checkNbPasCarrousel(nbPasCarrousel);
  }
  
  public int getNbPasCarrousel() throws ConfigurationException
  {
    return this._carouselSettings.getNbPasCarrousel();
  }
  
  public void setNbPasCarrousel(int nbPasCarrousel) throws ConfigurationException
  {
    this._carouselSettings.setNbPasCarrousel(nbPasCarrousel);
  }
  
  @Override
  public void close()
  {
    this._carouselSettings.close();
    super.close();
    GeneralSettings._INSTANCE = null;
  }
}
