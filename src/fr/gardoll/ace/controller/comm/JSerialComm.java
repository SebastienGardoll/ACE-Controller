package fr.gardoll.ace.controller.comm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fazecast.jSerialComm.SerialPort;

public class JSerialComm implements SerialCom
{
  private static final Logger _LOG = LogManager.getLogger(JSerialComm.class.getName());
  
  private SerialPort _port = null;
  
  @Override
  public void open(String portPath) throws SerialComException
  {
    if (this._port != null)
    {
      String msg = "the port is already openned.";
      _LOG.error(msg);
      return;
    }
    
    _LOG.debug(String.format("openning port '%s'.", portPath));
    
    try
    {
      this._port = SerialPort.getCommPort(portPath);
      
      if (this._port.openPort())
      {
        _LOG.debug(String.format("port '%s' is openned.", portPath));
      }
      else
      {
        String msg = String.format("fail to open port '%s'.", portPath);
        _LOG.error(msg);
        throw new SerialComException(msg);
      } 
    }
    catch(Exception e)
    {
      this._port = null;
      String msg = String.format("unable to open port '%s': %s.", portPath,
                                 e.getMessage());
      _LOG.error(msg);
      throw new SerialComException(e);
    }
  }
  
  @Override
  public void setVitesse(int vitesse) throws SerialComException
  {
    _port.setBaudRate(vitesse) ;
  }

  @Override
  public void setParite(Parity choix) throws SerialComException
  {
     int _parity = -1 ;
     
     switch(choix)
     {
       case NOPARITY:
       {
         _parity = SerialPort.NO_PARITY ;
         break;
       }
       
       case EVENPARITY:
       {
         _parity = SerialPort.EVEN_PARITY ;
         break;
       }
       
       case ODDPARITY:
       {
         _parity = SerialPort.ODD_PARITY ;
         break;
       }
     }
     
     _port.setParity(_parity) ;
  }

  @Override
  public void setStopBit(StopBit choix) throws SerialComException
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void setByteSize(short nbBit) throws SerialComException
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void setControlFlux(FluxControl choix) throws SerialComException
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void setTimeOut(int delais) throws SerialComException
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void ecrire(String ordre) throws SerialComException
  {
    // TODO Auto-generated method stub

  }

  @Override
  public String lire() throws SerialComException
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void close()
  {
    this._port = null;
  }
  
  public static void main(String[] args)
  {
    
  }
}
