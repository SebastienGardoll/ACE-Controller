package fr.gardoll.ace.controller.core;

public class ConfigurationException extends RuntimeException
{
  private static final long serialVersionUID = 4841464094924669527L ;
  
  public ConfigurationException(String msg)
  {
    super(msg) ;
  }
  
  public ConfigurationException(String msg, Throwable e)
  {
    super(msg, e) ;
  }

  public ConfigurationException(Throwable e)
  {
    super(e);
  }
}
