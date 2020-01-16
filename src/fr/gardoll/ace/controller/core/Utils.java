package fr.gardoll.ace.controller.core;

import java.math.BigDecimal ;
import java.math.RoundingMode ;
import java.net.URISyntaxException ;
import java.net.URL ;
import java.nio.file.Path ;
import java.util.Map ;
import java.util.Map.Entry ;

import javax.swing.JOptionPane ;

import fr.gardoll.ace.controller.settings.ParametresSession ;

public class Utils
{
  // Must be singleton as getRootDir must fetch the file of this class (or any
  // classes of this Java project).
  private static Utils _INSTANCE = null ;
  
  private static Path _ROOT_DIR = null;
  
  private Utils() {}
  
  // Enough precision for volume in milliliter, rate in milliliter/min
  // distance in millimeter and time in seconds.
  public static final double EPSILON = 0.000001 ;
  public static final int DOUBLE_SCALE = 6;
  
  public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP ;
  
  /*
  About double precision and BigDecimal:
  
  double d = 5.6 + 5.8;
  System.out.println(d) ; // returns 11.399999999999999 instead of 11.4
    
  BigDecimal d1 = new BigDecimal(5.6); // sysout d1.doubleValue() returns 5.6: ok
  BigDecimal d2 = new BigDecimal(5.8); // sysout d2.doubleValue() returns 5.8: ok
  BigDecimal d3 = d1.add(d2); // sysout d3.doubleValue() returns 11.399999999999999 !!!
    
  // Nevertheless  
  
  BigDecimal d4 = new BigDecimal("5.6");
  BigDecimal d5 = new BigDecimal("5.8");
  BigDecimal d6 = d4.add(d5); // sysout d6.doubleValue() returns 11.4 :)
  */
  // This is quite the same implementation as Apache common's math.
  public static double round(double value)
  {
    // It is absolutely necessary that the value is translated into string so as
    // to get literal representation, not a double precision approximation.
    return new BigDecimal(Double.toString(value)).setScale(DOUBLE_SCALE, ROUNDING_MODE)
                                                 .doubleValue();
  }
  
  // Warning: this is not thread safe !!!
  public static void round(Map<?, Double> map)
  {
    for(Entry<?, Double> entry: map.entrySet())
    {
      entry.setValue(Utils.round(entry.getValue()));
    }
  }
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
  
  public static Path rootDirRelativize(Path p)
  {
    Path rootDir = Utils.getInstance().getRootDir();
    return rootDir.relativize(p);
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
     
    Log.UI.info(String.format("display '%s'", msg));
    
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
