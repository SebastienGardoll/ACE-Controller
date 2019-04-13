package fr.gardoll.ace.controller.common;

public class InitializationException extends Exception
{
  private static final long serialVersionUID = -7247356875898233009L ;

  public InitializationException(String msg)
  {
    super(msg) ;
  }
    
  public InitializationException(String msg, Throwable e)
  {
    super(msg, e) ;
  }
}
