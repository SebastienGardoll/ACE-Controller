package fr.gardoll.ace.controller.core;

public class InitializationException extends RuntimeException
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
  
  public InitializationException(Throwable e)
  {
    super(e) ;
  }
}
