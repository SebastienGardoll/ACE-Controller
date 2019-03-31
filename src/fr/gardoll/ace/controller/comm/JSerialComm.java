package fr.gardoll.ace.controller.comm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fazecast.jSerialComm.SerialPort;

import fr.gardoll.ace.controller.comm.FluxControl;

public class JSerialComm implements SerialCom
{
  private static final Logger _LOG = LogManager.getLogger(JSerialComm.class.getName());
  
  private SerialPort _port = null;
  
  @Override
  public void open(String portPath) throws SerialComException
  {
    if (this._port != null)
    {
      String msg = "the port is already openned: don't reuse this instance.";
      _LOG.error(msg);
      throw new SerialComException(msg);
    }
    
    _LOG.debug(String.format("openning port '%s'", portPath));
    
    try
    {
      this._port = SerialPort.getCommPort(portPath);
      
      if (this._port.openPort())
      {
        _LOG.debug(String.format("port '%s' is openned", portPath));
      }
      else
      {
        String msg = String.format("fail to open port '%s'", portPath);
        _LOG.error(msg);
        throw new SerialComException(msg);
      } 
    }
    catch(Exception e)
    {
      this._port = null;
      String msg = String.format("unable to open port '%s': %s", portPath,
                                 e.getMessage());
      _LOG.error(msg);
      throw new SerialComException(e);
    }
  }
  
  @Override
  public void setVitesse(int vitesse) throws SerialComException
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void setParite(Parity choix) throws SerialComException
  {
    // TODO Auto-generated method stub

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

    SerialPort sp = SerialPort.getCommPort("/dev/ttyp6");
    sp.setComPortParameters(9600, 8, 1, 0); // default connection settings for
                                            // Arduino
    sp.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0); // block
                                                                    // until
                                                                    // bytes can
                                                                    // be
                                                                    // written
    if (sp.openPort())
      System.out.println("coucou");
    else
      System.out.println("nope");

  }
}
