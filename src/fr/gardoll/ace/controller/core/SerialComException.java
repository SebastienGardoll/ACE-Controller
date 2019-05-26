package fr.gardoll.ace.controller.core;

public class SerialComException extends Exception
{
  private static final long serialVersionUID = -1260707666262587296L ;
  
  public SerialComException(String msg)
  {
    super(msg) ;
  }
    
  public SerialComException(String msg, Throwable e)
  {
    super(msg, e) ;
  }
}