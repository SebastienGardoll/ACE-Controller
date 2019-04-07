package fr.gardoll.ace.controller.comm;

import java.io.Closeable ;

public interface SerialCom extends Closeable
{
  public void setVitesse(int vitesse) throws SerialComException ;

  public void setParite(Parity choix) throws SerialComException ;

  public void setStopBit(StopBit choix) throws SerialComException ;

  // Require 1<= nbBit <= 8
  public void setByteSize(short nbBit) throws SerialComException ;

  public void setControlFlux(FlowControl choix) throws SerialComException ;

  public void setTimeOut(int delais) throws SerialComException ;

  public void ecrire(String ordre) throws SerialComException ;

  // Read the serial port until the specified timeout.
  public String lire() throws SerialComException ;

  public void close() ;
  
  public void open(String portPath) throws SerialComException ;
}

enum FlowControl
{
  DISABLE, XON_XOFF, RTS_CTS ;
}

enum StopBit
{
  ONESTOPBIT, TWOSTOPBITS ;
}

enum Parity
{
  NOPARITY, EVENPARITY, ODDPARITY ;
}

enum SerialMode
{
  NON_BLOCKING, FULL_BLOCKING ;
}

class SerialComException extends Exception
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
