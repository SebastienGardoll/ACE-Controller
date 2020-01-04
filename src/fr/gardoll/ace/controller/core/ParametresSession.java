package fr.gardoll.ace.controller.core;

import java.io.Closeable ;
import java.io.FileNotFoundException ;
import java.io.IOException ;
import java.lang.reflect.Constructor ;
import java.nio.file.Files ;
import java.nio.file.Path ;
import java.text.DecimalFormatSymbols ;
import java.util.HashSet ;
import java.util.Locale ;
import java.util.Optional ;
import java.util.Set ;

import org.apache.commons.configuration2.INIConfiguration ;
import org.apache.commons.configuration2.SubnodeConfiguration ;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder ;
import org.apache.commons.configuration2.builder.fluent.Configurations ;
import org.apache.commons.configuration2.ex.ConfigurationException ;
import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import com.fazecast.jSerialComm.SerialPort ;

import fr.gardoll.ace.controller.autosampler.InterfaceMoteur ;
import fr.gardoll.ace.controller.autosampler.MotorController ;
import fr.gardoll.ace.controller.autosampler.MotorControllerStub ;
import fr.gardoll.ace.controller.autosampler.Passeur ;
import fr.gardoll.ace.controller.com.ParaCom ;
import fr.gardoll.ace.controller.com.SerialCom ;
import fr.gardoll.ace.controller.core.Utils.OS ;
import fr.gardoll.ace.controller.pump.InterfacePousseSeringue ;
import fr.gardoll.ace.controller.pump.PousseSeringue ;
import fr.gardoll.ace.controller.pump.PumpController ;
import fr.gardoll.ace.controller.pump.PumpControllerStub ;
import fr.gardoll.ace.controller.valves.ParaComStub ;
import fr.gardoll.ace.controller.valves.Valves ;

// TODO: default "READ ERROR" for any string.
// TODO: check for "READ ERROR" strings then throw exception.
public class ParametresSession implements Closeable
{
  private static final Logger _LOG = LogManager.getLogger(ParametresSession.class.getName());
  
  private static final String DEFAULT_STRING_VALUE = "READ_ERROR";
  
  public final static DecimalFormatSymbols DECIMAL_SYMBOLS =
      new DecimalFormatSymbols(Locale.FRANCE);
  
  public final static int NB_POSITION = 6 ;
  
  public static boolean isAutomatedTest = false;
  
  private static ParametresSession _INSTANCE ;
  
  private Path _configurationFile = null;
  
  // Lazy loading and singleton.
  private PousseSeringue _pump = null;
  private Passeur _autosampler = null;
  private Valves _valves       = null;
  
  private double _volumeMaxSeringue ;  // volume max du type de seringue en mL

  private double _volumeRincage ;  // volume  utilisé pendant un cylce de rinçage  en mL

  private int _nbPasCarrousel ;  // nombre de demi pas entre deux emplacement colonnes

  private int  _refCarrousel; // distance butée haute du bras, plateau supérieur du carrousel en mm

  private int _nbRincage ; //nombre de rinçage à effectuer au moment de changer d'éluant

  private double _debitMaxPousseSeringue ;//débit maximum applicable au pousseSeringue en fonction du diamètre de tuyau utilisé !!!

  private double _diametreSeringue ;//diametre du type de seringue utilisé

  private int _nbSeringue ; //nombre de seringues utilisées

  private int _diametreCarrousel ;//diamètre du carrousel en mm

  private int _epaisseur ;//epaisseur du plateau sup du carrousel

  private int _nbMaxColonne ; //nombre d'emplacement max de colonnes sur le carrousel choisi

  private String _pumpSerialComClassPath ;

  private String _pumpConfPortPath ;

  private String _autosamplerSerialComClassPath ;

  private String _autosamplerConfPortPath ;

  private String _paraComClassPath ;

  private String _paraComSerialComClassPath ;

  private String _paraComConfPortPath ;

  private boolean _isDebug ;
  private boolean _isFullScreen ;
  
  private Optional<String> _autosamplerRuntimePortPath = Optional.empty();
  
  private Optional<String> _pumpRuntimePortPath = Optional.empty();
  
  private Optional<String> _paraComRuntimePortPath = Optional.empty();

  public static ParametresSession getInstance()
  {
    try
    {
      if (ParametresSession._INSTANCE == null)
      {
        ParametresSession._INSTANCE = new ParametresSession(); 
      }
      
      return ParametresSession._INSTANCE ;
    }
    catch (InitializationException e)
    {
      String msg = "error while initializing the session";
      throw new RuntimeException(msg, e);
    }
  }
  
  private ParametresSession() throws InitializationException
  {
    this.loadConf();
  }
  
  // Reload conf but not Autosampler, Pump and Valves.
  public void loadConf() throws InitializationException
  {
    _LOG.info("load the basic configuration");
    
    Path rootDir = null ;
    _LOG.debug("fetch root dir");
    rootDir = Utils.getInstance().getRootDir();
    
    this._configurationFile = rootDir.resolve(Names.CONFIG_DIRNAME)
                                     .resolve(Names.CONFIG_FILENAME);
    Path carouselConfFile = null;
    
    if (Files.isReadable(this._configurationFile)    == false ||
        Files.isRegularFile(this._configurationFile) == false)
    {
      String msg = String.format("unable to read the configuration file '%s'",
          this._configurationFile);
      throw new InitializationException(msg);
    }
    
    try
    {
      _LOG.debug("read ace controller configuration");
      
      Configurations configs = new Configurations();
      INIConfiguration iniConf = configs.ini(this._configurationFile.toFile());
      SubnodeConfiguration section = iniConf.getSection(Names.SEC_ACE_CONTROLLER);
      
      this._isDebug = Names.TRUE.equals(section.getString(Names.SAC_IS_DEBUG, DEFAULT_STRING_VALUE));
      _LOG.debug(String.format("isDebug returns %s", this._isDebug));
      
      
      this._isFullScreen = Names.TRUE.equals(section.getString(Names.SAC_IS_FULL_SCREEN, DEFAULT_STRING_VALUE));
      _LOG.debug(String.format("isFullScreen returns %s", this._isFullScreen));
      
      section.close();
    }
    catch (ConfigurationException e)
    {
      String msg = String.format("unable to read the ace controller configuration '%s'",
          this._configurationFile.toString());
      throw new InitializationException(msg, e);
    }
    
    try
    {
      _LOG.debug("read pump configuration");
      
      Configurations configs = new Configurations();
      INIConfiguration iniConf = configs.ini(this._configurationFile.toFile());
      SubnodeConfiguration section = iniConf.getSection(Names.SEC_INFO_POUSSE_SERINGUE);
      
      this._pumpSerialComClassPath = section.getString(Names.SIPS_CLEF_SERIAL_COM_CLASS_PATH);
      this._pumpConfPortPath           = section.getString(Names.SIPS_CLEF_PORT_PATH);
      
      this._volumeMaxSeringue      = section.getDouble(Names.SIPS_CLEF_VOL_MAX, -1.0) ;
      this._volumeRincage          = section.getDouble(Names.SIPS_CLEF_VOL_RINCAGE, -1.0) ;
      this._nbRincage              = section.getInteger(Names.SIPS_CLEF_NB_RINCAGE, -1) ;
      this._debitMaxPousseSeringue = section.getDouble(Names.SIPS_CLEF_DEBIT_MAX, -1.0) ;
      this._nbSeringue             = section.getInteger(Names.SIPS_CLEF_NB_SERINGUE,-1);
      this._diametreSeringue       = section.getDouble(Names.SIPS_CLEF_DIA_SERINGUE,-1.0);
      
      section = iniConf.getSection(Names.SEC_INFO_CARROUSEL);
      String plateConfFilePathString      = section.getString(Names.SIC_CLEF_CHEMIN_FICHIER_CARROUSEL, "READ ERROR");
      this._autosamplerSerialComClassPath = section.getString(Names.SIC_CLEF_SERIAL_COM_CLASS_PATH);
      this._autosamplerConfPortPath       = section.getString(Names.SIC_CLEF_PORT_PATH);
      
      section.close();
      
      try
      {
        carouselConfFile = Utils.getInstance().resolvePath(plateConfFilePathString);
      }
      catch (FileNotFoundException e)
      {
        String msg = String.format("unable to locate carousel conf file '%s'", plateConfFilePathString);
        throw new InitializationException(msg, e);
      }
    }
    catch (ConfigurationException e)
    {
      String msg = String.format("unable to read the pump configuration '%s'",
          this._configurationFile.toString());
      throw new InitializationException(msg, e);
    }
    
    if (Files.isReadable(carouselConfFile)    == false ||
        Files.isRegularFile(carouselConfFile) == false)
    {
      String msg = String.format("unable to read the carousel configuration file '%s'", carouselConfFile);
      throw new InitializationException(msg);
    }
    
    try
    {
      _LOG.debug("read autosampler configuration");
      
      Configurations configs = new Configurations();
      INIConfiguration iniConf = configs.ini(carouselConfFile.toFile());
      SubnodeConfiguration section = iniConf.getSection(Names.SEC_INFO_CARROUSEL);
      
      this._nbPasCarrousel    = section.getInteger(Names.SIC_CLEF_NB_DEMI_PAS, -1) ;
      this._refCarrousel      = section.getInteger(Names.SIC_CLEF_REF_CARROUSEL, -1);
      this._diametreCarrousel = section.getInteger(Names.SIC_CLEF_DIA, -1);
      this._epaisseur         = section.getInteger(Names.SIC_CLEF_EPAISSEUR, -1);
      this._nbMaxColonne      = section.getInteger(Names.SIC_CLEF_NB_COL , -1) ;
      
      section.close();
    }
    catch (ConfigurationException e)
    {
      String msg = String.format("unable to read the carousel configuration file '%s'",
          carouselConfFile.toString());
      throw new InitializationException(msg, e);
    }
    
    try
    {
      _LOG.debug("read paracom configuration");
      
      Configurations configs = new Configurations();
      INIConfiguration iniConf = configs.ini(this._configurationFile.toFile());
      SubnodeConfiguration section = iniConf.getSection(Names.SEC_INFO_PARA_COM);
      
      this._paraComClassPath = section.getString(Names.SIPC_CLEF_PARA_COM_CLASS_PATH);
      this._paraComSerialComClassPath = section.getString(Names.SIPC_CLEF_SERIAL_COM_CLASS_PATH);
      this._paraComConfPortPath = section.getString(Names.SIPC_CLEF_PORT_PATH);
      
      section.close();
    }
    catch (ConfigurationException e)
    {
      String msg = String.format("unable to read the paracom configuration file '%s'",
          this._configurationFile.toString());
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
      throw new InitializationException(msg);
    }
  }

  public boolean isDebug()
  {
    return this._isDebug;
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
      String msg = String.format("cannot instantiate the SerialCom object '%s' with the given parameter '%s'",
          classPath, portPath);
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
      String msg = String.format("cannot instantiate the ParaCom object '%s' with the given parameter '%s'",
          paraComClassPath, paraComPort.getPath());
      throw new InitializationException(msg, e);
    }
  }

  private String getParaComSerialComClassPath()
  {
    return this._paraComSerialComClassPath;
  }

  private String getParaComConfPortPath()
  {
    return this._paraComConfPortPath;
  }

  private String getParaComClassPath()
  {
    return this._paraComClassPath;
  }

  private String getPumpConfPortPath()
  {
    return this._pumpConfPortPath;
  }

  private String getPumpSerialComClassPath()
  {
    return this._pumpSerialComClassPath;
  }

  private String getAutosamplerSerialComClassPath()
  {
    return this._autosamplerSerialComClassPath;
  }

  private String getAutosamplerConfPortPath()
  {
    return this._autosamplerConfPortPath;
  }
  
  // Lazy loading.
  public PousseSeringue getPousseSeringue() throws InitializationException
  {
    if(this._pump == null)
    {
      Valves valves = this.getValves();
      
      _LOG.info("instantiating the pump");
      
      PumpController pumpCtrl = null;
      
      if(this.isDebug())
      {
        pumpCtrl = new PumpControllerStub();
      }
      else
      {
        Instantiator instantiator = new PumpControllerInstatiator();
        Optional<Object> instance = null;
        instance = this.discoverCom(instantiator,
                                    this._pumpRuntimePortPath,
                                    this.getPumpConfPortPath());
        if(instance.isPresent())
        {
          pumpCtrl = (PumpController) instance.get();
          String actualPortPath = pumpCtrl.getPortPath();
          this._pumpRuntimePortPath = Optional.of(actualPortPath);
          this.persistPumpPortPath(actualPortPath);
        }
        else
        {
          String msg = "unable to discover the pump";
          throw new InitializationException(msg);
        }
      }
      
      this._pump = new PousseSeringue(pumpCtrl, valves, this.nbSeringue(), 
          this.diametreSeringue(), this.volumeMaxSeringue(),
          this.debitMaxPousseSeringue(), 0.);
    }
    
    return this._pump;
  }
  
  private void persistPumpPortPath(String portPath)
      throws InitializationException
  {
    String section = Names.SEC_INFO_POUSSE_SERINGUE;
    String key = Names.SIPS_CLEF_PORT_PATH ;
    String value = portPath;
    this.persistData(section, key, value);
  }

  private class PumpControllerInstatiator implements Instantiator
  {
    @Override
    public Object instantiate(String portPath) throws Exception
    {
      String pumpSerialComClassPath = ParametresSession.this.getPumpSerialComClassPath();
      _LOG.debug(String.format("instantiating pump port (%s, %s)", portPath, pumpSerialComClassPath)) ;
      SerialCom pumpPort = ParametresSession.this.instantiateSerialCom(pumpSerialComClassPath, portPath);
      PumpController pumpCtrl = new InterfacePousseSeringue(pumpPort, ParametresSession.this.diametreSeringue());
      return pumpCtrl;
    }
  }

  public Valves getValves()  throws InitializationException
  {
    if(this._valves == null)
    {
      ParaCom paraCom = null;
      
      _LOG.info("instantiating the valves");
      
      if(this.isDebug())
      {
        paraCom = new ParaComStub();
      }
      else
      {
        Instantiator instantiator = new ParaComInstatiator();
        Optional<Object> instance = null;
        instance = this.discoverCom(instantiator,
                                    this._paraComRuntimePortPath,
                                    this.getParaComConfPortPath());
        if(instance.isPresent())
        {
          paraCom = (ParaCom) instance.get();
          String actualPortPath = paraCom.getPortPath();
          this._paraComRuntimePortPath = Optional.of(actualPortPath);
          this.persistParaComPortPath(actualPortPath);
        }
        else
        {
          String msg = "unable to discover the paracom";
          throw new InitializationException(msg);
        }
      }
      
      _LOG.debug("instantiating valves") ;
      this._valves = new Valves(paraCom);
    }
    
    return this._valves;
  }
  
  private void persistParaComPortPath(String portPath)
      throws InitializationException
  {
    String section = Names.SEC_INFO_PARA_COM;
    String key = Names.SIPC_CLEF_PORT_PATH;
    String value = portPath;
    this.persistData(section, key, value);
  }

  private class ParaComInstatiator implements Instantiator
  {
    @Override
    public Object instantiate(String portPath) throws Exception
    {
      String paraComSerialComClassPath = ParametresSession.this.getParaComSerialComClassPath();
      _LOG.debug(String.format("instantiating paracom port (%s, %s)", portPath, paraComSerialComClassPath)) ;
      SerialCom paraComPort = ParametresSession.this.instantiateSerialCom(paraComSerialComClassPath, portPath);
      
      String paraComClassPath = ParametresSession.this.getParaComClassPath();
      _LOG.debug(String.format("instantiating paracom (%s)", paraComClassPath)) ;
      ParaCom paraCom = ParametresSession.this.instantiateParaCom(paraComClassPath, paraComPort);
      return paraCom;
    }
  }
  
  // Lazy loading.
  public Passeur getPasseur() throws InitializationException
  {
    if(this._autosampler == null)
    {
      MotorController motorCtrl = null;
      
      _LOG.info("instantiating the autosampler");
      
      if(this.isDebug())
      {
        motorCtrl = new MotorControllerStub(this.nbPasCarrousel());
      }
      else
      {
        Instantiator instantiator = new MotorControllerInstatiator();
        Optional<Object> instance = null;
        instance = this. discoverCom(instantiator,
                                     this._autosamplerRuntimePortPath,
                                     this.getAutosamplerConfPortPath());
        if(instance.isPresent())
        {
          motorCtrl = (MotorController) instance.get();
          String actualPortPath = motorCtrl.getPortPath();
          this._autosamplerRuntimePortPath = Optional.of(actualPortPath);
          this.persistAutosamplerPortPath(actualPortPath);
        }
        else
        {
          String msg = "unable to discover the autosampler";
          throw new InitializationException(msg);
        }
      }
      
      this._autosampler = new Passeur(motorCtrl, this.nbPasCarrousel(),
                            this.diametreCarrousel(), this.epaisseur(),
                            this.refCarrousel());
    }
    
    return this._autosampler;
  }
  
  private void persistAutosamplerPortPath(String portPath)
      throws InitializationException
  {
    String section = Names.SEC_INFO_CARROUSEL;
    String key = Names.SIC_CLEF_PORT_PATH ;
    String value = portPath;
    this.persistData(section, key, value);
  }
  
  private void persistData(String section, String key, String value)
      throws InitializationException
  {
    try
    {
      Configurations configs = new Configurations();
      FileBasedConfigurationBuilder<INIConfiguration> iniBuilder = configs.iniBuilder(this._configurationFile.toFile());
      INIConfiguration iniConf = iniBuilder.getConfiguration();  
      SubnodeConfiguration sectionNode = iniConf.getSection(section);
      sectionNode.setProperty(key, value);
      sectionNode.close();
      iniBuilder.save();
    }
    catch(Exception e)
    {
      String msg = String.format("unable to persist section '%s' ; key '%s' ; value '%s'",
                                  section, key, value);
      throw new InitializationException(msg, e);
    }
  }

  private Optional<Object> tryInstantiate(Instantiator instantiator,
                                          String portPath)
  {
    Optional<Object> result = Optional.empty();
    
    try
    {
      Object instance = instantiator.instantiate(portPath);
      result = Optional.of(instance);
      String msg = String.format("instantiation successed with port path '%s'", portPath);
      _LOG.debug(msg);
    }
    catch(Exception e)
    {
      String msg = String.format("failed to instantiate with port path '%s'", portPath);
      _LOG.debug(msg);
      _LOG.trace("print stack trace", e);
    }
    
    return result;
  }
  
  // Caller must set runtimePort and configPort after a successful call.
  private Optional<Object> discoverCom(Instantiator instantiator,
                                       Optional<String> runtimePort,
                                       String configPort)
  {
    Optional<Object> result = Optional.empty();
    
    Set<String> portPathTries = new HashSet<>();
    
    // Try runtime setting.
    if(runtimePort.isPresent())
    {
      result = this.tryInstantiate(instantiator, runtimePort.get());
      if(result.isPresent())
      {
        return result;
      }
      else
      {
        portPathTries.add(runtimePort.get());
      }
    }
    
    // Else try the path set in the configuration file.
    result = this.tryInstantiate(instantiator, configPort);
    if(result.isPresent())
    {
      return result;
    }
    else
    {
      portPathTries.add(configPort);
    }
    
    // Otherwise try every remaining path.
    Set<String> systemPortPaths = ParametresSession.getSystemPortPaths();
    systemPortPaths.removeAll(portPathTries);
    
    for(String portPath: systemPortPaths)
    {
      result = this.tryInstantiate(instantiator, portPath);
      if(result.isPresent())
      {
        return result;
      }
      else
      {
        continue;
      }
    }
    
    return result;
  }
  
  private static Set<String> getSystemPortPaths()
  {
    Set<String> result = new HashSet<>();
    OS currentOS = Utils.getOs();
    
    for(SerialPort port: SerialPort.getCommPorts())
    {
      String portName = port.getSystemPortName();
      if(currentOS == OS.MACOS ||
         currentOS == OS.UNIX)
      {
        portName = String.format("/dev/%s", portName);
      }
      
      result.add(portName);
    }
    
    return result ;
  }

  private interface Instantiator
  {
    public Object instantiate(String portPath) throws Exception;
  }
  
  private class MotorControllerInstatiator implements Instantiator
  {
    @Override
    public Object instantiate(String portPath) throws Exception
    {
      String classPath = ParametresSession.this.getAutosamplerSerialComClassPath();
      _LOG.debug(String.format("instantiating autosampler port (%s, %s)", portPath, classPath));
      SerialCom autosamplerPort = ParametresSession.this.instantiateSerialCom(classPath, portPath);
      MotorController motorCtrl = new InterfaceMoteur(autosamplerPort);
      return motorCtrl;
    }
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
    _LOG.debug("closing parameter session"); 
    try
    {
      if(this._pump != null)
      {
        this._pump.close();
        this._pump = null;
      }
          
      if(this._autosampler != null)
      {
        this._autosampler.close();
        this._autosampler = null;
      }
      
      if(this._valves != null)
      {
        this._valves.close();
        this._valves = null;
      }
      
      ParametresSession._INSTANCE = null;
    }
    catch (IOException e)
    {
      String msg = "error while closing the pump or the autosampler";
      _LOG.error(msg, e);
    }
  }

  public boolean isFullScreen()
  {
    return this._isFullScreen;
  }
}