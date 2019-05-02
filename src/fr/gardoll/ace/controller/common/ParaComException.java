package fr.gardoll.ace.controller.common;

public class ParaComException extends Exception
{
  private static final long serialVersionUID = 2150067698089848112L ;
  
  public ParaComException(String msg)
  {
    super(msg) ;
  }
    
  public ParaComException(String msg, Throwable e)
  {
    super(msg, e) ;
  }
}
