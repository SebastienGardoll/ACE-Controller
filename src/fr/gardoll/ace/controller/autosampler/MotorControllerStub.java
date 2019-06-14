package fr.gardoll.ace.controller.autosampler;

import java.io.Closeable ;
import java.io.IOException ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.SerialComException ;
import fr.gardoll.ace.controller.core.ThreadControl ;

public class MotorControllerStub implements MotorController, Closeable
{
  private static final Logger _LOG = LogManager.getLogger(MotorControllerStub.class.getName());
  
  private int _currentCarouselPosition = 0;
  private int _currentArmPosition      = 0;
  
  // Number of steps per period.
  // Moving to 1 position in 0.2 seconds considering period of 0.1 second.
  private static double _CAROUSEL_TIME_FACTOR = 5. ;
  
  // Number of steps per period.
  // Moving 10 mm in 0.2 seconds considering period of 0.1 second.
  private static double _ARM_TIME_FACTOR = Passeur.convertBras(10.) / 0.2; 
  
  private boolean _isCarouselMoving = false ;
  private boolean _isArmMoving      = false ;

  private final int _nbStepPosition ;

  public MotorControllerStub(int nbStepPosition)
  {
    this._nbStepPosition = nbStepPosition;
  }
  
  @Override
  public void close() throws IOException
  {
    _LOG.debug("stubbing method close");
  }

  @Override
  public void setThreadControl(ThreadControl threadCtrl)
  {
    _LOG.debug("stubbing command setThreadControl");
  }

  @Override
  public void move(int nbPas1, int nbPas2)
      throws SerialComException, InterruptedException
  {
    _LOG.debug(String.format("stubbing command move(%s, %s)", nbPas1, nbPas2));
  }

  @Override
  public boolean moving(TypeAxe axe)
      throws SerialComException, InterruptedException
  {
    // TODO Auto-generated method stub
    return false ;
  }

  @Override
  public void movel(int axe1, int axe2)
      throws SerialComException, InterruptedException
  {
    _LOG.debug(String.format("stubbing command movel(%s, %s)", axe1, axe2));
    
    if(axe1 != 0)
    {
      throw new RuntimeException("unexpected moving to the limit of the carousel");
    }
    
    if(axe2 != 1)
    {
      throw new RuntimeException("unexpected moving to the limit of the arm");
    }
  }

  @Override
  public void reset() throws SerialComException, InterruptedException
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void preSecale(int denominateur)
      throws SerialComException, InterruptedException
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void param(TypeAxe axe, int base, int top, int accel)
      throws SerialComException, InterruptedException
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void param(TypeAxe axe, int base, int top, int accel, int deaccel)
      throws SerialComException, InterruptedException
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void datum(TypeAxe axe) throws SerialComException, InterruptedException
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void singleLine(boolean choix)
      throws SerialComException, InterruptedException
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void stop() throws SerialComException, InterruptedException
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void manual() throws SerialComException, InterruptedException
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void halt() throws SerialComException, InterruptedException
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public int where(TypeAxe axe) throws SerialComException, InterruptedException
  {
    // TODO Auto-generated method stub
    return 0 ;
  }

  @Override
  public void out(int octet) throws SerialComException, InterruptedException
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void out(int bitPosition, boolean isOn)
      throws SerialComException, InterruptedException
  {
    // TODO Auto-generated method stub
    
  }
}
