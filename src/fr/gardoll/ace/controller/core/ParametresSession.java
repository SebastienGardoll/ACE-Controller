package fr.gardoll.ace.controller.core;

import java.io.Closeable ;
import java.io.FileNotFoundException ;
import java.io.IOException ;
import java.lang.reflect.Constructor ;
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

import fr.gardoll.ace.controller.autosampler.InterfaceMoteur ;
import fr.gardoll.ace.controller.autosampler.Passeur ;
import fr.gardoll.ace.controller.com.ParaCom ;
import fr.gardoll.ace.controller.com.SerialCom ;
import fr.gardoll.ace.controller.pump.InterfacePousseSeringue ;
import fr.gardoll.ace.controller.pump.PousseSeringue ;

// TODO: default "READ ERROR" for any string.
// TODO: check for "READ ERROR" strings then throw exception.
public class ParametresSession implements Closeable
{
  private static final Logger _LOG = LogManager.getLogger(ParametresSession.class.getName());
  
  public final static DecimalFormatSymbols DECIMAL_SYMBOLS =
      new DecimalFormatSymbols(Locale.FRANCE);
  
  public final static int NB_POSITION = 6 ;
  
  private static ParametresSession _INSTANCE ;
  
  // Lazy loading and singleton.
  private PousseSeringue _pump = null;
  private Passeur _autosampler = null;
  
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

  private final String _pumpSerialComClassPath ;

  private final String _pumpPortPath ;

  private final String _autosamplerSerialComClassPath ;

  private final String _autosamplerPortPath ;

  private final String _paraComClassPath ;

  private final String _paraComSerialComClassPath ;

  private final String _paraComPortPath ;
  
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
    _LOG.debug("fetch root dir");
    rootDir = Utils.getInstance().getRootDir();
    
    Path configurationFile = rootDir.resolve(Names.CONFIG_DIRNAME)
                                    .resolve(Names.CONFIG_FILENAME);
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
      _LOG.debug("read pump configuration");
      
      Configurations configs = new Configurations();
      INIConfiguration iniConf = configs.ini(configurationFile.toFile());
      SubnodeConfiguration section = iniConf.getSection(Names.SEC_INFO_POUSSE_SERINGUE);
      
      this._pumpSerialComClassPath = section.getString(Names.SIPS_CLEF_SERIAL_COM_CLASS_PATH);
      this._pumpPortPath           = section.getString(Names.SIPS_CLEF_PORT_PATH);
      
      this._volumeMaxSeringue      = section.getDouble(Names.SIPS_CLEF_VOL_MAX, -1.0) ;
      this._volumeRincage          = section.getDouble(Names.SIPS_CLEF_VOL_RINCAGE, -1.0) ;
      this._nbRincage              = section.getInteger(Names.SIPS_CLEF_NB_RINCAGE, -1) ;
      this._debitMaxPousseSeringue = section.getDouble(Names.SIPS_CLEF_DEBIT_MAX, -1.0) ;
      this._nbSeringue             = section.getInteger(Names.SIPS_CLEF_NB_SERINGUE,-1);
      this._diametreSeringue       = section.getDouble(Names.SIPS_CLEF_DIA_SERINGUE,-1.0);
      
      section = iniConf.getSection(Names.SEC_INFO_CARROUSEL);
      String plateConfFilePathString      = section.getString(Names.SIC_CLEF_CHEMIN_FICHIER_CARROUSEL, "READ ERROR");
      this._autosamplerSerialComClassPath = section.getString(Names.SIC_CLEF_SERIAL_COM_CLASS_PATH);
      this._autosamplerPortPath           = section.getString(Names.SIC_CLEF_PORT_PATH);
      try
      {
        plateConfFile = Utils.getInstance().resolvePath(plateConfFilePathString);
      }
      catch (FileNotFoundException e)
      {
        String msg = String.format("unable to locate plate conf file '%s'", plateConfFilePathString);
        _LOG.fatal(msg, e);
        throw new InitializationException(msg, e);
      }
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
      String msg = String.format("unable to read the plate configuration file '%s'", plateConfFile);
      _LOG.fatal(msg);
      throw new InitializationException(msg);
    }
    
    try
    {
      _LOG.debug("read autosampler configuration");
      
      Configurations configs = new Configurations();
      INIConfiguration iniConf = configs.ini(plateConfFile.toFile());
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
          plateConfFile.toString(), e.getMessage());
      _LOG.fatal(msg, e);
      throw new InitializationException(msg, e);
    }
    
    try
    {
      _LOG.debug("read paracom configuration");
      
      Configurations configs = new Configurations();
      INIConfiguration iniConf = configs.ini(configurationFile.toFile());
      SubnodeConfiguration section = iniConf.getSection(Names.SEC_INFO_PARA_COM);
      
      this._paraComClassPath = section.getString(Names.SIPC_CLEF_PARA_COM_CLASS_PATH);
      this._paraComSerialComClassPath = section.getString(Names.SIPC_CLEF_SERIAL_COM_CLASS_PATH);
      this._paraComPortPath = section.getString(Names.SIPC_CLEF_PORT_PATH);
    }
    catch (ConfigurationException e)
    {
      String msg = String.format("unable to read the paracom configuration file '%s': %s",
          configurationFile.toString(), e.getMessage());
      _LOG.fatal(msg, e);
      throw new InitializationException(msg, e);
    }
    
    _LOG.debug("performing data checking");
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

  private SerialCom instantiateSerialCom(String classPath, String portPath) throws InitializationException
  {
    try
    {
      @SuppressWarnings("unchecked")
      Class<SerialCom> serialComClass = (Class<SerialCom>) Class.forName(classPath);
      Constructor<SerialCom> constructor = serialComClass.getConstructor(String.class);
      SerialCom port = constructor.newInstance(portPath);
      return port;
    }
    catch(Exception e)
    {
      String msg = String.format("cannot instantiate the SerialCom object '%s' with the given parameter '%s': %s",
          classPath, portPath, e.getMessage());
      _LOG.fatal(msg, e);
      throw new InitializationException(msg, e);
    }
  }
  
  private ParaCom instantiateParaCom(String paraComClassPath, SerialCom paraComPort)
      throws InitializationException
  {
    try
    {
      @SuppressWarnings("unchecked")
      Class<ParaCom> paraComClass = (Class<ParaCom>) Class.forName(paraComClassPath);
      Constructor<ParaCom> constructor = paraComClass.getConstructor(SerialCom.class);
      ParaCom paraCom = constructor.newInstance(paraComPort);
      return paraCom;
    }
    catch(Exception e)
    {
      String msg = String.format("cannot instantiate the ParaCom object '%s' with the given parameter '%s': %s",
          paraComClassPath, paraComPort.getPath(), e.getMessage());
      _LOG.fatal(msg, e);
      throw new InitializationException(msg, e);
    }
  }

  private String getParaComSerialComClassPath()
  {
    return this._paraComSerialComClassPath;
  }

  private String getParaComPortPath()
  {
    return this._paraComPortPath;
  }

  private String getParaComClassPath()
  {
    return this._paraComClassPath;
  }

  private String getPumpPortPath()
  {
    return this._pumpPortPath;
  }

  private String getPumpSerialComClassPath()
  {
    return this._pumpSerialComClassPath;
  }

  private String getAutosamplerSerialComClassPath()
  {
    return this._autosamplerSerialComClassPath;
  }

  private String getAutosamplerPortPath()
  {
    return this._autosamplerPortPath;
  }
  
  // Lazy loading.
  public PousseSeringue getPousseSeringue() throws InitializationException, InterruptedException
  {
    if(this._pump == null)
    {
      String paraComSerialComClassPath = this.getParaComSerialComClassPath();
      String paraComPortPath  = this.getParaComPortPath();
      SerialCom paraComPort   = this.instantiateSerialCom(paraComSerialComClassPath, paraComPortPath);
      
      String paraComClassPath = this.getParaComClassPath();
      ParaCom paraCom = this.instantiateParaCom(paraComClassPath, paraComPort);
      
      String pumpSerialComClassPath = this.getPumpSerialComClassPath();
      String pumpPortPath  = this.getPumpPortPath();
      SerialCom pumpPort   = this.instantiateSerialCom(pumpSerialComClassPath, pumpPortPath);
      InterfacePousseSeringue pumpCommands = new InterfacePousseSeringue(pumpPort,
          this.diametreSeringue());
      this._pump = new PousseSeringue(pumpCommands, paraCom, this.nbSeringue(), 
          this.diametreSeringue(), this.volumeMaxSeringue(),
          this.debitMaxPousseSeringue(), 0.);
    }
    
    return this._pump;
  }
  
  // Lazy loading.
  public Passeur getPasseur() throws InitializationException, InterruptedException
  {
    if(this._autosampler == null)
    {
      String classPath = this.getAutosamplerSerialComClassPath();
      String portPath  = this.getAutosamplerPortPath();
      SerialCom autosamplerPort = this.instantiateSerialCom(classPath, portPath);
      InterfaceMoteur autosamplerCommands = new InterfaceMoteur(autosamplerPort);
      this._autosampler = new Passeur(autosamplerCommands, this.nbPasCarrousel(),
          this.diametreCarrousel());
    }
    
    return this._autosampler;
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
  
  @Override
  public void close()
  {
    try
    {
      if(this._pump != null)
      {
        this._pump.close();
      }
          
      if(this._autosampler != null)
      {
        this._autosampler.close();
      }
    }
    catch (IOException e)
    {
      String msg = String.format(": %s", e.getMessage());
      _LOG.error(msg, e);
    }
  }
}