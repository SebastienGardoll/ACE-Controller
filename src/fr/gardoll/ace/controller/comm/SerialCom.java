package fr.gardoll.ace.controller.comm;

public interface SerialCom
{
  public void setVitesse(int vitesse) throws SerialComException;

  public void setParite(Parity choix) throws SerialComException;

  public void setStopBit(StopBit choix) throws SerialComException;

  // Require 1<= nbBit <= 8
  public void setByteSize(short nbBit) throws SerialComException;

  public void setControlFlux(FluxControl choix) throws SerialComException;

  public void setTimeOut(int delais) throws SerialComException;

  public void ecrire(String ordre) throws SerialComException;

  public String lire() throws SerialComException;

  public void close() throws SerialComException;
  
  public void open(String portPath) throws SerialComException;
}

enum FluxControl
{
  XON_XOFF, HARDWARE;
}

enum StopBit
{
  ONESTOPBIT, ONE5STOPBITS, TWOSTOPBITS;
}

enum Parity
{
  NOPARITY, EVENPARITY, ODDPARITY;
}

class SerialComException extends Exception
{
  private static final long serialVersionUID = -1260707666262587296L;
  
  public SerialComException(String msg)
  {
    super(msg);
  }
    
  public SerialComException(Exception e)
  {
    super(e);
  }
}
