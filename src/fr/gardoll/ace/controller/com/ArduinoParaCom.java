package fr.gardoll.ace.controller.com;

import java.nio.charset.Charset ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.ParaComException ;
import fr.gardoll.ace.controller.core.SerialComException ;

public class ArduinoParaCom implements ParaCom
{
  private static final int OPENING_DELAY = 2000;
  
  private static final Logger _LOG = LogManager.getLogger(ArduinoParaCom.class.getName());
  
  private final SerialCom _port;

  public ArduinoParaCom(SerialCom port) throws InitializationException, InterruptedException
  {
    _LOG.debug(String.format("initialiazing arduino with the port '%s'", port.getId()));
    this._port = port;
    
    try
    {
      this._port.setReadBufferSize(10);
      this._port.setMode(SerialMode.FULL_BLOCKING, SerialMode.FULL_BLOCKING);
      this._port.setCharset(Charset.forName("ASCII"));
      this._port.setVitesse(9600);
      this._port.setTimeOut(100);
      this._port.setByteSize(8);
      this._port.setParite(Parity.NOPARITY);
      this._port.setStopBit(StopBit.ONESTOPBIT);
      this._port.open(ArduinoParaCom.OPENING_DELAY);
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while initializing the port '%s'",
          this._port.getPath());
      _LOG.fatal(msg, e);
      throw new InitializationException(msg, e);
    }
  }
  
  @Override
  public String getId()
  {
    return this._port.getId();
  }

  @Override
  public void send(byte[] order) throws ParaComException, InterruptedException
  {
    _LOG.debug(String.format("sending order '%s'", order));
    
    try
    {
      this._port.write(order);
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while writing on port '%s'",
          this._port.getId());
      _LOG.fatal(msg, e);
      throw new ParaComException(msg, e);
    }
    
    this.check();    
  }
  
  private void check() throws ParaComException
  {
    try
    {
      String ack = this._port.lire().strip() ;
      _LOG.debug(String.format("checking ack '%s'", ack));
      
      switch(ack)
      {
        case "E":
        {
          String msg = "error while sending order to usb2valves";
          _LOG.fatal(msg);
          throw new ParaComException(msg);
        }
        
        case "0":
        {
          // Everything is OK.
          break;
        }
        
        case "":
        {
          String msg = String.format("arduino is disconnected (port is %s)",
                                     this._port.getId());
          _LOG.fatal(msg);
          throw new ParaComException(msg);
        }
        
        default:
        {
          String msg = String.format("unsupported ack '%s'", ack);
          _LOG.fatal(msg);
          throw new ParaComException(msg);
        }
      }
    }
    catch (SerialComException e)
    {
      String msg = "error while waiting the usb2valves acknowledge";
      _LOG.fatal(msg, e);
      throw new ParaComException(msg, e);
    }
  }

  @Override
  public void close()
  {
    _LOG.debug(String.format("closing the port '%s'", this._port.getId()));
    this._port.close();
  }
}
