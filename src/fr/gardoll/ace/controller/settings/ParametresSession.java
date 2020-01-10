package fr.gardoll.ace.controller.settings;

import java.io.Closeable ;
import java.io.IOException ;
import java.lang.reflect.Constructor ;
import java.util.HashSet ;
import java.util.Optional ;
import java.util.Set ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import com.fazecast.jSerialComm.SerialPort ;

import fr.gardoll.ace.controller.autosampler.InterfaceMoteur ;
import fr.gardoll.ace.controller.autosampler.MotorController ;
import fr.gardoll.ace.controller.autosampler.MotorControllerStub ;
import fr.gardoll.ace.controller.autosampler.Passeur ;
import fr.gardoll.ace.controller.com.ParaCom ;
import fr.gardoll.ace.controller.com.SerialCom ;
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.core.Utils.OS ;
import fr.gardoll.ace.controller.pump.InterfacePousseSeringue ;
import fr.gardoll.ace.controller.pump.PousseSeringue ;
import fr.gardoll.ace.controller.pump.PumpController ;
import fr.gardoll.ace.controller.pump.PumpControllerStub ;
import fr.gardoll.ace.controller.valves.ParaComStub ;
import fr.gardoll.ace.controller.valves.Valves ;

public class ParametresSession implements Closeable
{
  private static final Logger _LOG = LogManager.getLogger(ParametresSession.class.getName());
  
  public static boolean isAutomatedTest = false;
  
  private static ParametresSession _INSTANCE ;
  
  // Lazy loading and singleton.
  private PousseSeringue _pump = null;
  private Passeur _autosampler = null;
  private Valves _valves       = null;
  
  private Optional<String> _autosamplerRuntimePortPath = Optional.empty();
  
  private Optional<String> _pumpRuntimePortPath = Optional.empty();
  
  private Optional<String> _paraComRuntimePortPath = Optional.empty();

  private final GeneralSettings _settings ;

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
  
  private ParametresSession()
  {
    this._settings = GeneralSettings.instance();
    _LOG.debug(String.format("isDebug returns %s", this._settings.isDebug()));
    _LOG.debug(String.format("isFullScreen returns %s", this._settings.isFullScreen()));
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

  // Lazy loading.
  public PousseSeringue getPousseSeringue() throws InitializationException
  {
    if(this._pump == null)
    {
      Valves valves = this.getValves();
      
      _LOG.info("instantiating the pump");
      
      PumpController pumpCtrl = null;
      
      if(this._settings.isDebug())
      {
        pumpCtrl = new PumpControllerStub();
      }
      else
      {
        Instantiator instantiator = new PumpControllerInstatiator();
        Optional<Object> instance = null;
        instance = this.discoverCom(instantiator,
                                    this._pumpRuntimePortPath,
                                    this._settings.getPumpConfPortPath());
        if(instance.isPresent())
        {
          pumpCtrl = (PumpController) instance.get();
          String actualPortPath = pumpCtrl.getPortPath();
          this._pumpRuntimePortPath = Optional.of(actualPortPath);
          
          this._settings.setPumpConfPortPath(actualPortPath);
        }
        else
        {
          String msg = "unable to discover the pump";
          throw new InitializationException(msg);
        }
      }
      
      this._pump = new PousseSeringue(pumpCtrl, valves, 0.);
    }
    
    return this._pump;
  }
  
  private class PumpControllerInstatiator implements Instantiator
  {
    @Override
    public Object instantiate(String portPath) throws Exception
    {
      String pumpSerialComClassPath = ParametresSession.this._settings.getPumpSerialComClassPath();
      _LOG.debug(String.format("instantiating pump port (%s, %s)", portPath, pumpSerialComClassPath)) ;
      SerialCom pumpPort = ParametresSession.this.instantiateSerialCom(pumpSerialComClassPath, portPath);
      PumpController pumpCtrl = new InterfacePousseSeringue(pumpPort);
      return pumpCtrl;
    }
  }

  public Valves getValves()  throws InitializationException
  {
    if(this._valves == null)
    {
      ParaCom paraCom = null;
      
      _LOG.info("instantiating the valves");
      
      if(this._settings.isDebug())
      {
        paraCom = new ParaComStub();
      }
      else
      {
        Instantiator instantiator = new ParaComInstatiator();
        Optional<Object> instance = null;
        instance = this.discoverCom(instantiator,
                                    this._paraComRuntimePortPath,
                                    this._settings.getParaComConfPortPath());
        if(instance.isPresent())
        {
          paraCom = (ParaCom) instance.get();
          String actualPortPath = paraCom.getPortPath();
          this._paraComRuntimePortPath = Optional.of(actualPortPath);
          
          this._settings.setParaComConfPortPath(actualPortPath);
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
  
  private class ParaComInstatiator implements Instantiator
  {
    @Override
    public Object instantiate(String portPath) throws Exception
    {
      String paraComSerialComClassPath = ParametresSession.this._settings.getParaComSerialComClassPath();
      _LOG.debug(String.format("instantiating paracom port (%s, %s)", portPath, paraComSerialComClassPath)) ;
      SerialCom paraComPort = ParametresSession.this.instantiateSerialCom(paraComSerialComClassPath, portPath);
      
      String paraComClassPath = ParametresSession.this._settings.getParaComClassPath();
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
      
      if(this._settings.isDebug())
      {
        motorCtrl = new MotorControllerStub(this._settings.getNbPasCarrousel());
      }
      else
      {
        Instantiator instantiator = new MotorControllerInstatiator();
        Optional<Object> instance = null;
        instance = this. discoverCom(instantiator,
                                     this._autosamplerRuntimePortPath,
                                     this._settings.getAutosamplerConfPortPath());
        if(instance.isPresent())
        {
          motorCtrl = (MotorController) instance.get();
          String actualPortPath = motorCtrl.getPortPath();
          this._autosamplerRuntimePortPath = Optional.of(actualPortPath);
          
          this._settings.setAutosamplerConfPortPath(actualPortPath);
        }
        else
        {
          String msg = "unable to discover the autosampler";
          throw new InitializationException(msg);
        }
      }
      
      this._autosampler = new Passeur(motorCtrl);
    }
    
    return this._autosampler;
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
      String classPath = ParametresSession.this._settings.getAutosamplerSerialComClassPath();
      _LOG.debug(String.format("instantiating autosampler port (%s, %s)", portPath, classPath));
      SerialCom autosamplerPort = ParametresSession.this.instantiateSerialCom(classPath, portPath);
      MotorController motorCtrl = new InterfaceMoteur(autosamplerPort);
      return motorCtrl;
    }
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
      
      GeneralSettings.instance().close();
      
      ParametresSession._INSTANCE = null;
    }
    catch (IOException e)
    {
      String msg = "error while closing the pump or the autosampler";
      _LOG.error(msg, e);
    }
  }

  public void reset()
  {
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
    }
    catch (IOException e)
    {
      String msg = "error while closing the pump or the autosampler";
      _LOG.error(msg, e);
    }
  }
}