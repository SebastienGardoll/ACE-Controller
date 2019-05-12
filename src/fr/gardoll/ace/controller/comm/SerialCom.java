package fr.gardoll.ace.controller.comm;

import java.io.Closeable ;

import fr.gardoll.ace.controller.common.SerialComException ;

public interface SerialCom extends Closeable
{
  public String getId();
  
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
  
  public void open() throws SerialComException, InterruptedException ;
}
