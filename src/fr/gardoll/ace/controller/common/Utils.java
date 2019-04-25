package fr.gardoll.ace.controller.common;

public class Utils
{
  private Utils() {}
  
  public static double EPSILON = 0.00001 ;
  
  public static boolean isNearZero(double value)
  {
    return (value <= EPSILON);
  }
  
}
