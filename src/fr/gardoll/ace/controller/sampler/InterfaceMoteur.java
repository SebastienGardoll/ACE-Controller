package fr.gardoll.ace.controller.sampler;

import java.io.Closeable;
import java.io.IOException;

import fr.gardoll.ace.controller.comm.SerialCom;

public class InterfaceMoteur implements Closeable
{
  private final SerialCom _port;
  
  public InterfaceMoteur(SerialCom port)
  {
    this._port = port ;
  }
  
  @Override
  public void close() throws IOException
  {
    this._port.close() ;
  }
  
  public static void main(String[] args)
  {
    // TODO Auto-generated method stub

  }
}
