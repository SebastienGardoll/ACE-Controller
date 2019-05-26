package fr.gardoll.ace.controller.core;

import java.net.URISyntaxException ;
import java.net.URL ;
import java.nio.file.Path ;

import javax.swing.JOptionPane ;

public class Utils
{
  private Utils() {}
  
  public static double EPSILON = 0.00001 ;
  
  public static boolean isNearZero(double value)
  {
    return (value <= EPSILON);
  }
  
  // Return the path of the directory of the application (not the current
  // directory !).
  public static Path getRootDir(Object obj) throws URISyntaxException
  {
    URL u = obj.getClass().getProtectionDomain().getCodeSource().getLocation();
    Path result = Path.of(u.toURI()).getParent();
    return result;
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
