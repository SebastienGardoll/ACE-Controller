package fr.gardoll.ace.controller.common;

import java.net.URISyntaxException ;
import java.net.URL ;
import java.nio.file.Path ;

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
  
}
