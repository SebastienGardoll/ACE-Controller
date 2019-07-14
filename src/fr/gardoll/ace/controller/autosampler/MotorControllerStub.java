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
  
  private int _currentCarouselPosition  = 0;
  private int _targetedCarouselPosition = 0;
  private int _carouselDirection        = 0;
  private boolean _isCarouselMoving     = false ;

  private int _currentArmPosition       = 0;
  private int _targetedArmPosition      = 0;
  private int _armDirection             = 0;
  private boolean _isArmMoving          = false ;
  
  // Number of steps per period.
  // Moving to 1 position in 0.2 seconds considering period of 0.1 second.
  private static int _CAROUSEL_TIME_INC_FACTOR = 5;
  private int _carouselTimeInc = 1 ;
  
  // Number of steps per period.
  // Moving 10 mm in 0.5 seconds considering period of 0.1 second.
  private static int _ARM_TIME_INC = Passeur.convertBras(10.) / 10; 

  public MotorControllerStub(int nbStepPosition)
  {
    _LOG.debug(String.format("instanciating motor controller stub with %s number of steps by carousel position", nbStepPosition));
    this._carouselTimeInc = nbStepPosition/_CAROUSEL_TIME_INC_FACTOR;
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
    
    if(nbPas1 != this._currentCarouselPosition)
    {
      this._targetedCarouselPosition = nbPas1;
      this._carouselDirection = this._currentCarouselPosition < nbPas1 ? 1 : -1 ;
      this._isCarouselMoving = true;
    }
    else
    {
      _LOG.debug("carousel didn't move");
    }
    
    if(nbPas2 != this._currentArmPosition)
    {
      this._targetedArmPosition = nbPas2;
      this._armDirection = this._currentArmPosition < nbPas2 ? 1 : -1 ;
      this._isArmMoving = true;
    }
    else
    {
      _LOG.debug("arm didn't move");
    }
  }

  @Override
  public boolean moving(TypeAxe axe)
      throws SerialComException, InterruptedException
  {
    boolean result = false;

    switch(axe)
    {
      case bras:
      {
        if(this._isArmMoving)
        {
          if(this._armDirection > 0)
          {
            this._currentArmPosition += _ARM_TIME_INC;
            if(this._currentArmPosition >= this._targetedArmPosition)
            {
              this._currentArmPosition = this._targetedArmPosition;
              this._isArmMoving = false;
            }
          }
          else
          {
            this._currentArmPosition -= _ARM_TIME_INC;
            if(this._currentArmPosition <= this._targetedArmPosition)
            {
              this._currentArmPosition = this._targetedArmPosition;
              this._isArmMoving = false;
            }
          }
        }
        
        result = this._isArmMoving;
        break ;
      }
      
      case carrousel:
      {
        if(this._isCarouselMoving)
        {
          if(this._carouselDirection > 0)
          {
            this._currentCarouselPosition += this._carouselTimeInc;
            if(this._currentCarouselPosition >= this._targetedCarouselPosition)
            {
              this._currentCarouselPosition = this._targetedCarouselPosition;
              this._isCarouselMoving = false;
            }
          }
          else
          {
            this._currentCarouselPosition -= this._carouselTimeInc;
            if(this._currentCarouselPosition <= this._targetedCarouselPosition)
            {
              this._currentCarouselPosition = this._targetedCarouselPosition;
              this._isCarouselMoving = false;
            }
          }
        }
        
        result = this._isCarouselMoving;
        break ;
      }
    }
    
    _LOG.trace(String.format("stubbing command moving for %s: %s", axe.name(), result));
    return result;
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
    
    this._targetedArmPosition = 0;
    this._armDirection = this._currentArmPosition < 0 ? 1 : -1 ;
    this._isArmMoving = true;
  }

  @Override
  public void reset() throws SerialComException, InterruptedException
  {
    _LOG.debug("stubbing command reset");
    this._currentCarouselPosition  = 0;
    this._targetedCarouselPosition = 0;
    this._currentArmPosition       = 0;
    this._targetedArmPosition      = 0;
    this._carouselDirection        = 0;
    this._armDirection             = 0;
  }

  @Override
  public void preSecale(int denominateur)
      throws SerialComException, InterruptedException
  {
    _LOG.debug(String.format("stubbing command preSecale %s", denominateur));
  }

  @Override
  public void param(TypeAxe axe, int base, int top, int accel)
      throws SerialComException, InterruptedException
  {
    this.param(axe, base, top, accel, 0);
  }

  @Override
  public void param(TypeAxe axe, int base, int top, int accel, int deaccel)
      throws SerialComException, InterruptedException
  {
    _LOG.debug(String.format("stubbing command param(%s, %s, %s, %s, %s)",
        axe, base, top, accel, deaccel));
  }

  @Override
  public void datum(TypeAxe axe) throws SerialComException, InterruptedException
  {
    _LOG.debug(String.format("stubbing command datum for %s", axe.name()));
    switch(axe)
    {
      case bras:
      {
        this._currentArmPosition = 0;
        break ;
      }
      
      case carrousel:
      {
        this._currentCarouselPosition = 0;
        break ;
      }
    }
  }

  @Override
  public void singleLine(boolean choix)
      throws SerialComException, InterruptedException
  {
    _LOG.debug(String.format("stubbing command singleline %s", choix));
  }

  @Override
  public void stop() throws SerialComException, InterruptedException
  {
    _LOG.debug("stubbing command stop");
    this._isArmMoving      = false;
    this._isCarouselMoving = false;
  }

  @Override
  public void manual() throws SerialComException, InterruptedException
  {
    _LOG.debug("stubbing command manual");
  }

  @Override
  public void halt() throws SerialComException, InterruptedException
  {
    _LOG.debug("stubbing command halt");
    this._isArmMoving      = false;
    this._isCarouselMoving = false;
  }

  @Override
  public int where(TypeAxe axe) throws SerialComException, InterruptedException
  {
    int result = 0;
    
    switch(axe)
    {
      case bras:
      {
        result = this._currentArmPosition;
        break ;
      }
      
      case carrousel:
      {
        result = this._currentCarouselPosition;
        break ;
      }
    }
    _LOG.debug(String.format("stubbing command where for %s: %s", axe.name(), result));
    
    return result;
  }

  @Override
  public void out(int octet) throws SerialComException, InterruptedException
  {
    _LOG.debug(String.format("stubbing command out %s", octet));
  }

  @Override
  public void out(int bitPosition, boolean isOn)
      throws SerialComException, InterruptedException
  {
    _LOG.debug(String.format("stubbing command out(%s, %s)", bitPosition, isOn));
  }
}
