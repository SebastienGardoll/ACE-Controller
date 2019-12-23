package fr.gardoll.ace.controller.autosampler;

import java.io.Closeable ;

import fr.gardoll.ace.controller.com.SerialComException ;

public interface MotorController extends Closeable
{
  public void move(int nbPas1 , int nbPas2) throws SerialComException;
  
  public boolean moving(TypeAxe axe) throws SerialComException;

  public void movel(int axe1, int axe2) throws SerialComException;
  
  public void reset() throws SerialComException;
  
  public void preSecale(int denominateur) 
      throws SerialComException;
  
  public void param(TypeAxe axe, int base, int top, int accel)
                                throws SerialComException;
  
  public void param(TypeAxe axe, int base, int top, int accel, int deaccel)
                                throws SerialComException;

  public void datum(TypeAxe axe) throws SerialComException;
  
  public void singleLine(boolean choix) throws SerialComException;
  
  public void stop() throws SerialComException;
  
  public void manual() throws SerialComException;

  public void halt() throws SerialComException;
                                           
  public int where(TypeAxe axe) throws SerialComException;

  // 0 <= octet <= 255
  public void out(int octet) throws SerialComException;

  public void out(int bitPosition, boolean isOn) throws SerialComException;

  public String getPortPath() ;
}
