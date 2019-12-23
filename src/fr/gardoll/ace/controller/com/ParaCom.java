package fr.gardoll.ace.controller.com;

import java.io.Closeable ;

public interface ParaCom  extends Closeable
{
  public String getId();
  
  public void send(byte[] order) throws ParaComException;
  
  @Override // Dodge IOException.
  public void close();

  public String getPortPath() ;
}
