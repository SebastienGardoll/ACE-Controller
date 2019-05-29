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
  
  private Utils() {}
  
  public static double EPSILON = 0.00001 ;
  
  public static boolean isNearZero(double value)
  {
    return (value <= EPSILON);
  }
  
  // Resolve the given string parameter to a system dependent absolute path. 
  public static Path resolvePath(String path) throws FileNotFoundException
  {
    Path p = Paths.get(path);
    Path result = null;
    if(p.isAbsolute())
    {
      result = p;
    }
    else
    {
      Path rootDir = Utils.getRootDir(path) ;
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
  public static Path getRootDir(Object obj)
  {
    try
    {
      URL u = obj.getClass().getProtectionDomain().getCodeSource().getLocation();
      Path result = Path.of(u.toURI()).getParent() ;
      return result;
    }
    catch (URISyntaxException e)
    {
      String msg = String.format("unable to fetch the path of the application: %s", e.getMessage());
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
     
    JOptionPane.showMessageDialog(null, displayedMsg, "Error",
        JOptionPane.ERROR_MESSAGE);
  }
}
