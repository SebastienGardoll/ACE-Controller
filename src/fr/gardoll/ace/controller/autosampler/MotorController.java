package fr.gardoll.ace.controller.autosampler;

import java.io.Closeable ;

import fr.gardoll.ace.controller.com.SerialComException ;

public interface MotorController extends Closeable
{
  public void move(int nbPas1 , int nbPas2) throws SerialComException, InterruptedException;
  
  public boolean moving(TypeAxe axe) throws SerialComException, InterruptedException;

  public void movel(int axe1, int axe2) throws SerialComException, InterruptedException;
  
  public void reset() throws SerialComException, InterruptedException;
  
  public void preSecale(int denominateur) 
      throws SerialComException, InterruptedException;
  
  public void param(TypeAxe axe, int base, int top, int accel)
                                throws SerialComException, InterruptedException;
  
  public void param(TypeAxe axe, int base, int top, int accel, int deaccel)
                                throws SerialComException, InterruptedException;

  public void datum(TypeAxe axe) throws SerialComException, InterruptedException;
  
  public void singleLine(boolean choix) throws SerialComException, InterruptedException;
  
  public void stop() throws SerialComException, InterruptedException;
  
  public void manual() throws SerialComException, InterruptedException;

  public void halt() throws SerialComException, InterruptedException;
                                           
  public int where(TypeAxe axe) throws SerialComException, InterruptedException;

  // 0 <= octet <= 255
  public void out(int octet) throws SerialComException, InterruptedException;

  public void out(int bitPosition, boolean isOn) throws SerialComException, InterruptedException;
}
