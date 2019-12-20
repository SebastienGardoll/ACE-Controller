package fr.gardoll.ace.controller.com;

import java.io.Closeable ;
import java.nio.charset.Charset ;

public interface SerialCom extends Closeable
{
  public String getId();
  
  public String getPath();
  
  public void setVitesse(int vitesse) throws SerialComException ;

  public void setParite(Parity choix) throws SerialComException ;

  public void setStopBit(StopBit choix) throws SerialComException ;

  // Require 1<= nbBit <= 8
  public void setByteSize(int nbBit) throws SerialComException ;

  public void setControlFlux(FlowControl choix) throws SerialComException ;

  public void setTimeOut(int delais) throws SerialComException ;

  public void ecrire(String ordre) throws SerialComException ;
  
  public void write(byte[] ordre) throws SerialComException ;

  // Read the serial port until the specified timeout.
  public String lire() throws SerialComException ;

  @Override
  public void close() ;
  
  // Time (milliseconds) to wait after opening the port.
  public void open(int openingDelay) throws SerialComException, InterruptedException ;
  
  public void setMode(SerialMode readMode, SerialMode writeMode);
  
  public void setCharset(Charset charset);
  
  public void setReadBufferSize(int nbOfBytes);
}
