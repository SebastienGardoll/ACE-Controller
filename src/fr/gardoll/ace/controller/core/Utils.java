package fr.gardoll.ace.controller.core;

import java.io.FileNotFoundException ;
import java.net.URISyntaxException ;
import java.net.URL ;
import java.nio.file.Files ;
import java.nio.file.Path ;
import java.nio.file.Paths ;
import java.util.List ;
import java.util.Map ;
import java.util.Map.Entry ;

import javax.swing.JOptionPane ;

import org.apache.commons.configuration2.INIConfiguration ;
import org.apache.commons.configuration2.SubnodeConfiguration ;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder ;
import org.apache.commons.configuration2.builder.fluent.Configurations ;
import org.apache.commons.lang3.tuple.Pair ;
import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

public class Utils
{
  private static final Logger _LOG = LogManager.getLogger(Utils.class.getName());
  
  // Must be singleton as getRootDir must fetch the file of this class (or any
  // classes of this Java project).
  private static Utils _INSTANCE = null ;
  
  private static Path _ROOT_DIR = null;
  
  private Utils() {}
  
  public static double EPSILON = 0.00001 ;
  
  public static boolean isNearZero(double value)
  {
    return (value <= EPSILON);
  }
  
  public static Utils getInstance()
  {
    if(_INSTANCE == null)
    {
      _INSTANCE = new Utils();
    }
    
    return _INSTANCE;
  }
  
  // Resolve the given string parameter to a system dependent absolute path. 
  public Path resolvePath(String path) throws FileNotFoundException
  {
    Path p = Paths.get(path);
    Path result = null;
    if(p.isAbsolute())
    {
      result = p;
    }
    else
    {
      Path rootDir = this.getRootDir() ;
      result = rootDir.resolve(p);
    }
    
    if(false == Files.exists(result))
    {
      throw new FileNotFoundException(String.format("'%s' does not exist", result));
    }
    
    return result;
  }
  
  // Return the path of the directory of the application (not the current
  // directory !).
  public Path getRootDir()
  {
    if(_ROOT_DIR == null)
    {
      try
      {
        URL u = this.getClass().getProtectionDomain().getCodeSource().getLocation();
        _ROOT_DIR = Path.of(u.toURI()).getParent() ;
      }
      catch (URISyntaxException e)
      {
        String msg = "unable to fetch the path of the application";
        throw new RuntimeException(msg, e);
      }
    }
    
    return _ROOT_DIR;
  }
  
  public static String toString(byte[] value, String separator)
  {
    StringBuilder sb = new StringBuilder();
    
    for(byte b: value)
    {
      sb.append(b);
      sb.append(separator);
    }
    
    // Remove the last separator.
    sb.setLength(sb.length()-separator.length());
    
    return sb.toString();
  }
   
  // May return empty String object.
  public static String getFileExtention(String fileName)
  {
    int index = fileName.lastIndexOf('.');
    if(index > 0 &&  index < fileName.length() - 1)
    {
      return fileName.substring(index+1).toLowerCase();
    }
    else
    {
      return "";
    }
  }
  
  // Throwable can be null.
  public static void reportError(String msg, Throwable e)
  {
    String displayedMsg = null;
    if (e != null)
    {
      displayedMsg = String.format("%s: %s", msg, e.getMessage());
    }
    else
    {
      displayedMsg = msg;
    }
     
    _LOG.debug(String.format("displaying this message: '%s'", msg));
    
    if(false == ParametresSession.isAutomatedTest)
    {
      JOptionPane.showMessageDialog(null, displayedMsg, "Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }
  
  public static OS getOs()
  {
    String rawOsName = System.getProperty("os.name").toLowerCase();
    
    OS result = OS.UNKNOWN;
    
    if(rawOsName.contains("mac"))
    {
      result = OS.MACOS;
    }
    else if(rawOsName.contains("win"))
    {
      result = OS.WINDOWS;
    }
    else if(rawOsName.contains("nix") ||
            rawOsName.contains("nux") ||
            rawOsName.contains("aix"))
    {
      result = OS.UNIX;
    }
    else
    {
      result = OS.UNKNOWN;
    }
    
    return result;
  }
  
  public enum OS
  {
    MACOS,
    WINDOWS,
    UNIX,
    UNKNOWN;
  }
  
  public static void persistPropertyData(Path propertyFilePath,
                                         String section, String key,
                                         String value)
      throws ConfigurationException
  {
    try
    {
      Configurations configs = new Configurations();
      FileBasedConfigurationBuilder<INIConfiguration> iniBuilder = configs.iniBuilder(propertyFilePath.toFile());
      INIConfiguration iniConf = iniBuilder.getConfiguration();  
      SubnodeConfiguration sectionNode = iniConf.getSection(section);
      sectionNode.setProperty(key, value);
      sectionNode.close();
      iniBuilder.save();
    }
    catch(Exception e)
    {
      String msg = String.format("unable to persist section '%s' ; key '%s' ; value '%s' in '%s'",
                                  section, key, value, propertyFilePath);
      throw new ConfigurationException(msg, e);
    }
  }
  
  public static void persistPropertyData(Path propertyFilePath,
                       Map<String, List<Pair<String, String>>> sectionKeyValues)
                                  throws ConfigurationException
  {
    INIConfiguration iniConf = null;
    FileBasedConfigurationBuilder<INIConfiguration> iniBuilder = null;
    
    try
    {
      Configurations configs = new Configurations();
      iniBuilder = configs.iniBuilder(propertyFilePath.toFile());
      iniConf = iniBuilder.getConfiguration();  
    }
    catch(Exception e)
    {
      String msg = String.format("unable to open property file '%s'", propertyFilePath);
      throw new ConfigurationException(msg, e);
    }
    
    for(Entry<String, List<Pair<String, String>>> sectionData: sectionKeyValues.entrySet())
    {
      String section = sectionData.getKey();
      SubnodeConfiguration sectionNode = null; 
      
      try
      {
        sectionNode = iniConf.getSection(section);
      }
      catch(Exception e)
      {
        String msg = String.format("unable to open section '%s' in '%s'",
                                    section, propertyFilePath);
        throw new ConfigurationException(msg, e);
      }
      
      for(Pair<String, String> keyValue: sectionData.getValue())
      {
        try
        {
          sectionNode.setProperty(keyValue.getKey(), keyValue.getValue());
        }
        catch (Exception e)
        {
          String msg = String.format("unable to set value '%s' for key '%s' in section '%s' for '%s'",
              keyValue.getValue(), keyValue.getKey(), section, propertyFilePath);
          throw new ConfigurationException(msg, e);
        }
      }
      
      try
      {
        sectionNode.close();
      }
      catch(Exception e)
      {
        String msg = String.format("unable to close section '%s' in '%s'",
            section, propertyFilePath);
        throw new ConfigurationException(msg, e);
      }
    }
    
    try
    {
      iniBuilder.save();
    }
    catch(Exception e)
    {
      String msg = String.format("unable to persist data in '%s'",
                                  propertyFilePath);
      throw new ConfigurationException(msg, e);
    }
  }
  
  public static boolean isDividableBy250(double value)
  {
    boolean result = false;
    
    String[] decomposition = String.valueOf(value).split("\\.");
    
    if(decomposition.length > 1)
    {
      String fractionalLiteral = decomposition[1];
      
      if(fractionalLiteral.length() > 3)
      {
        result = false;
      }
      else
      {
        int fractionalPart = Integer.valueOf(fractionalLiteral);
        
        if(fractionalLiteral.length() == 1)
        {
          fractionalPart *= 100;
        }
        else if(fractionalLiteral.length() == 2)
        {
          fractionalPart *= 10;
        }
        
        int remaining = fractionalPart % 250 ;
        
        result = remaining == 0;
      }
    }
    else
    {
      result = true;
    }
    
    return result;
  }
}
