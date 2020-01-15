package fr.gardoll.ace.controller.autosampler;

import java.io.Closeable ;
import java.io.IOException ;

import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.com.SerialComException ;
import fr.gardoll.ace.controller.core.Log ;

public class MotorControllerStub implements MotorController, Closeable
{
  private static final Logger _LOG = Log.STUB;
  
  private int _currentCarouselPosition  = 0;
  private int _targetedCarouselPosition = 0;
  private int _carouselDirection        = 0;
  private boolean _isCarouselMoving     = false ;
  private int _carouselOrigin           = 0;

  private int _currentArmPosition       = 0;
  private int _targetedArmPosition      = 0;
  private int _armDirection             = 0;
  private boolean _isArmMoving          = false ;
  private int _armOrigin                = 0;
  
  // Number of steps per period.
  // Moving to 1 position in 0.2 seconds considering period of 0.1 second.
  public static double CAROUSEL_TIME_INC_FACTOR = 5.;
  private double _carouselTimeInc = 1. ;
  
  // Number of steps per period.
  // Moving 10 mm in 0.5 seconds considering period of 0.1 second.
  public static int ARM_TIME_INC = Passeur.convertBras(10.) / 6; 

  public MotorControllerStub(int nbStepPosition)
  {
    _LOG.debug(String.format("instanciating motor controller stub with %s number of steps by carousel position", nbStepPosition));
    this._carouselTimeInc = nbStepPosition/CAROUSEL_TIME_INC_FACTOR;
  }
  
  @Override
  public void close() throws IOException
  {
    _LOG.debug("stubbing 'close'");
  }

  @Override
  public void move(int nbPas1, int nbPas2)
      throws SerialComException
  {
    _LOG.trace(String.format("stubbing command 'move(%s, %s)'", nbPas1, nbPas2));
    
    // Translate from relative coordinates to absolute coordinates.
    nbPas1 = nbPas1 + this._carouselOrigin;
    nbPas2 = nbPas2 + this._armOrigin;
    
    if(nbPas1 != this._currentCarouselPosition)
    {
      this._targetedCarouselPosition = nbPas1;
      this._carouselDirection = this._currentCarouselPosition < nbPas1 ? 1 : -1 ;
      this._isCarouselMoving = true;
    }
    else
    {
      Log.HIGH_LEVEL.debug("% carousel doesn't have to move %");
      this._carouselDirection = 0;
      this._isCarouselMoving = false;
    }
    
    if(nbPas2 != this._currentArmPosition)
    {
      this._targetedArmPosition = nbPas2;
      this._armDirection = this._currentArmPosition < nbPas2 ? 1 : -1 ;
      this._isArmMoving = true;
    }
    else
    {
      Log.HIGH_LEVEL.debug("% arm doesn't have to move %");
      this._armDirection = 0;
      this._isArmMoving = false;
    }
  }

  @Override
  public boolean moving(TypeAxe axe)
      throws SerialComException
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
            this._currentArmPosition += ARM_TIME_INC;
            if(this._currentArmPosition >= this._targetedArmPosition)
            {
              this._currentArmPosition = this._targetedArmPosition;
              this._isArmMoving = false;
            }
          }
          else
          {
            this._currentArmPosition -= ARM_TIME_INC;
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
    
    return result;
  }

  @Override
  public void movel(int axe1, int axe2)
      throws SerialComException
  {
    _LOG.trace(String.format("stubbing command 'movel(%s, %s)'", axe1, axe2));
    
    if(axe1 != 0)
    {
      throw new RuntimeException("unexpected moving to the limit of the carousel");
    }
    
    if(axe2 != 1)
    {
      throw new RuntimeException("unexpected moving to the limit of the arm");
    }
    
    if(this._currentArmPosition == 0)
    {
      this._isArmMoving = false;
      this._armDirection = 0 ;
      Log.HIGH_LEVEL.debug("% arm already on top %");
    }
    else
    {
      this._isArmMoving = true;
      this._armDirection = this._currentArmPosition < 0 ? 1 : -1 ;
    }
    
    this._targetedArmPosition = 0;
  }

  @Override
  public void reset() throws SerialComException
  {
    _LOG.debug("stubbing command 'reset'");
    this._armOrigin                = 0;
    this._carouselOrigin           = 0;
    this._currentCarouselPosition  = 0;
    this._targetedCarouselPosition = 0;
    this._currentArmPosition       = 0;
    this._targetedArmPosition      = 0;
    this._carouselDirection        = 0;
    this._armDirection             = 0;
  }

  @Override
  public void preSecale(int denominateur)
      throws SerialComException
  {
    _LOG.trace(String.format("stubbing command 'preSecale %s'", denominateur));
  }

  @Override
  public void param(TypeAxe axe, int base, int top, int accel)
      throws SerialComException
  {
    this.param(axe, base, top, accel, 0);
  }

  @Override
  public void param(TypeAxe axe, int base, int top, int accel, int deaccel)
      throws SerialComException
  {
    _LOG.trace(String.format("stubbing command 'param(%s, %s, %s, %s, %s)'",
        axe, base, top, accel, deaccel));
  }

  @Override
  public void datum(TypeAxe axe) throws SerialComException
  {
    _LOG.trace(String.format("stubbing command 'datum (%s)' for %s", axe,
        axe.name()));
    switch(axe)
    {
      case bras:
      {
        this._armOrigin = this._currentArmPosition;
        break ;
      }
      
      case carrousel:
      {
        this._carouselOrigin = this._currentCarouselPosition;
        break ;
      }
    }
  }

  @Override
  public void singleLine(boolean choix)
      throws SerialComException
  {
    char convertion = (choix) ? '1':'2';
    _LOG.trace(String.format("stubbing command 'singleline (%s)'", convertion,
        choix));
  }

  @Override
  public void stop() throws SerialComException
  {
    _LOG.trace("stubbing command 'stop'");
    this._isArmMoving      = false;
    this._isCarouselMoving = false;
  }

  @Override
  public void manual() throws SerialComException
  {
    _LOG.trace("stubbing command 'manual'");
  }

  @Override
  public void halt() throws SerialComException
  {
    _LOG.trace("stubbing command 'halt'");
    this._isArmMoving      = false;
    this._isCarouselMoving = false;
  }

  @Override
  public int where(TypeAxe axe) throws SerialComException
  {
    int result = 0;
    
    switch(axe)
    {
      case bras:
      {
        result = this._currentArmPosition - this._armOrigin;
        break ;
      }
      
      case carrousel:
      {
        result = this._currentCarouselPosition - this._carouselOrigin;
        break ;
      }
    }
    _LOG.trace(String.format("stubbing command 'where (%s)' for %s: %s", axe, 
        axe.name(), result));
    
    return result;
  }

  @Override
  public void out(int octet) throws SerialComException
  {
    _LOG.trace(String.format("stubbing command 'out(%s)'", octet));
  }

  @Override
  public void out(int bitPosition, boolean isOn)
      throws SerialComException
  {
    char value = (isOn) ? '1' : '0';
    _LOG.trace(String.format("stubbing command 'out(%s, %s)'", bitPosition, value, isOn));
  }

  @Override
  public String getPortPath()
  {
    return "stub_port";
  }
}
