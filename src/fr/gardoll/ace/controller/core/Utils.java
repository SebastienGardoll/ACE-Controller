package fr.gardoll.ace.controller.core;

import java.io.FileNotFoundException ;
import java.net.URISyntaxException ;
import java.net.URL ;
import java.nio.file.Files ;
import java.nio.file.Path ;
import java.nio.file.Paths ;

import javax.swing.JOptionPane ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

public class Utils
{
  private static final Logger _LOG = LogManager.getLogger(Utils.class.getName());
  
  // Must be singleton as getRootDir must fetch the file of this class (or any
  // classes of this Java project).
  private static Utils _INSTANCE = null ;
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
    try
    {
      URL u = this.getClass().getProtectionDomain().getCodeSource().getLocation();
      Path result = Path.of(u.toURI()).getParent() ;
      return result;
    }
    catch (URISyntaxException e)
    {
      String msg = "unable to fetch the path of the application";
      _LOG.fatal(msg, e);
      throw new RuntimeException(e);
    }
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
}
