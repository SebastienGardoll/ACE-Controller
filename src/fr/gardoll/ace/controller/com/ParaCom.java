package fr.gardoll.ace.controller.com;

import java.io.Closeable ;

import fr.gardoll.ace.controller.core.ParaComException ;

public interface ParaCom  extends Closeable
{
  public String getId();
  
  public void send(byte[] order) throws ParaComException, InterruptedException;
  
  @Override // Dodge IOException.
  public void close();
}
