package fr.gardoll.ace.controller.comm;

import java.nio.charset.Charset;
import java.util.AbstractMap.SimpleEntry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortTimeoutException;

import fr.gardoll.ace.controller.common.SerialComException ;

public class JSerialComm implements SerialCom
{
  private static final Logger _LOG = LogManager.getLogger(JSerialComm.class.getName());
  
  private SerialPort _port    = null ;
  private Charset _charset    = null ;
  private int _sizeReadBuffer = -1 ;
  private String _name        = "not openned" ;
  private int _readMode       = SerialPort.TIMEOUT_NONBLOCKING ;
  private int _writeMode      = SerialPort.TIMEOUT_NONBLOCKING ;
  private int _mode           = SerialPort.TIMEOUT_NONBLOCKING ;
  
  public JSerialComm(SerialMode readMode, SerialMode writeMode,
                     Charset charset, int sizeReadBuffer)
  {
    _LOG.info("initializing the JSerialComm");
    
    this._charset = charset ;
    this._sizeReadBuffer = sizeReadBuffer ;
    
    switch(readMode)
    {
      case FULL_BLOCKING:
      {
        this._readMode = SerialPort.TIMEOUT_READ_BLOCKING ;
        this._mode     = this._readMode ;
        break;
      }
    
      case NON_BLOCKING:
      {
        this._readMode = SerialPort.TIMEOUT_NONBLOCKING ;
        break ;
      }
    }
    
    switch(writeMode)
    {
      case FULL_BLOCKING:
      {
        this._writeMode =  SerialPort.TIMEOUT_WRITE_BLOCKING ;
        
        if (this._mode == SerialPort.TIMEOUT_NONBLOCKING)
        {
          this._mode = this._writeMode ;
        }
        else
        {
          this._mode = this._mode | this._writeMode ;
        }
        
        break;
      }
    
      case NON_BLOCKING:
      {
        this._writeMode = SerialPort.TIMEOUT_NONBLOCKING ;
        break;
      }
    }
  }
  
  @Override
  public void open(String portPath) throws SerialComException
  {
    if (this._port != null)
    {
      String msg = String.format("the port '%s' is already openned.", portPath);
      _LOG.error(msg) ;
      return;
    }
    
    _LOG.debug(String.format("openning port '%s'.", portPath));
     
    this._name = portPath ;
    
    try
    {
      this._port = SerialPort.getCommPort(portPath) ;
      
      if (this._port.openPort())
      {
        _LOG.debug(String.format("port '%s' is openned.", portPath)) ;
      }
      else
      {
        String msg = String.format("fail to open port '%s'.", portPath);
        _LOG.error(msg) ;
        throw new SerialComException(msg) ;
      } 
    }
    catch(Exception e)
    {
      this._port = null ;
      String msg = String.format("unable to open port '%s': %s.", portPath,
                                 e.getMessage()) ;
      _LOG.error(msg);
      throw new SerialComException(msg, e) ;
    }
  }
  
  @Override
  public void setVitesse(int vitesse) throws SerialComException
  {
    _LOG.debug(String.format("setting the baud rate of port '%s' to '%s'",
        this._name, vitesse)) ;
    this._port.setBaudRate(vitesse) ;
  }

  @Override
  public void setParite(Parity choix) throws SerialComException
  {
    _LOG.debug(String.format("setting the parity of port '%s' to '%s'",
        this._name, choix)) ;
    
    int _parity = -1 ;
     
    switch(choix)
    {
      case NOPARITY:
      {
        _parity = SerialPort.NO_PARITY ;
        break ;
      }
       
      case EVENPARITY:
      {
        _parity = SerialPort.EVEN_PARITY ;
        break ;
      }
       
      case ODDPARITY:
      {
        _parity = SerialPort.ODD_PARITY ;
        break ;
      }
    }
     
    this._port.setParity(_parity) ;
  }

  @Override
  public void setStopBit(StopBit choix) throws SerialComException
  {
    _LOG.debug(String.format("setting the number of stop bit of port '%s' to '%s'",
        this._name, choix)) ;
    int numStopBit = -1 ; 
    switch(choix)
    {
      case ONESTOPBIT:
      {
        numStopBit = 1 ;
        break;
      }
      
      case TWOSTOPBITS:
      {
        numStopBit = 2 ;
        break ;
      }      
    }
    
    this._port.setNumStopBits(numStopBit) ;
  }

  @Override
  public void setByteSize(int nbBit) throws SerialComException
  {
    _LOG.debug(String.format("setting the number of data bit of port '%s' to '%s'",
        this._name, nbBit)) ;
    this._port.setNumDataBits(nbBit) ;
  }

  @Override
  public void setControlFlux(FlowControl choix) throws SerialComException
  {
    _LOG.debug(String.format("setting the flow control of port '%s' to '%s'",
        this._name, choix)) ;
    int _flowCtrl = -1 ;
    switch(choix)
    {
      case DISABLE:
      {
        _flowCtrl = SerialPort.FLOW_CONTROL_DISABLED ;
        break ;
      }
    
      case XON_XOFF:
      {
        _flowCtrl = SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED |
                    SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED ;
        break;
      }
    
      case RTS_CTS:
      {
        _flowCtrl =  SerialPort.FLOW_CONTROL_RTS_ENABLED |
                     SerialPort.FLOW_CONTROL_CTS_ENABLED ;
        break ;
      }
    }
      
    this._port.setFlowControl(_flowCtrl) ;
  }

  @Override
  public void setTimeOut(int delais) throws SerialComException
  {
    _LOG.debug(String.format("setting the timeout of port '%s' to '%s'", this._name,
        delais)) ;
    this._port.setComPortTimeouts(this._mode, delais, delais) ;
  }

  @Override
  public void ecrire(String ordre) throws SerialComException
  {
    _LOG.debug(String.format("writing '%s' on port '%s'", ordre, this._name)) ;
    
    try
    {
      byte[] buffer = ordre.getBytes(this._charset) ;
      int nb_byte_sent = this._port.writeBytes(buffer, buffer.length) ;
      
      if (nb_byte_sent < 0)
      {
        String msg = String.format("transmission error while sending order '%s' on port '%s'",
                                   ordre, this._name) ;

        throw new SerialComException(msg) ;
      }
      
      if (nb_byte_sent != buffer.length)
      {
        String msg = String.format("transmission error while sending order '%s' on port '%s': only %s bytes sent out of %s",
            ordre, this._name, nb_byte_sent, buffer.length) ;
        
        throw new SerialComException(msg) ;
      }
    }
    catch (Exception e)
    {
      String msg = String.format("transmission error while sending order '%s' one port '%s': %s",
                                 ordre, this._name, e.getMessage()) ; 
      throw new SerialComException(msg, e) ;
    }
  }

  @Override
  public String lire() throws SerialComException
  {
    _LOG.debug(String.format("reading on port '%s'", this._name)) ;
    StringBuilder sb = new StringBuilder() ;
    
    boolean continue_to_read = true ;
    
    while(continue_to_read)
    {
      SimpleEntry<Integer, byte[]> buffer = internal_read() ;
      
      String rawResult = new String(buffer.getValue(), this._charset) ;
      
      // Remove any space characters.
      String intermediateResult = rawResult.replaceAll("\\s", "") ;
      
      // Add the intermediate result to the result.
      sb.append(intermediateResult) ;
      
      // Was the buffer full ?
      continue_to_read = buffer.getKey() == buffer.getValue().length ;
    }
    
    String result = sb.toString() ; 
    _LOG.debug(String.format("read '%s'", result));

    return result ;
  }
  
  private SimpleEntry<Integer, byte[]> internal_read() throws SerialComException
  {
    byte[] buffer = new byte[this._sizeReadBuffer] ;
    int nb_byte_read = 0 ;
    
    try
    {
      // Blocking until timeout elapsed or read the size of the buffer. 
      nb_byte_read = this._port.readBytes(buffer, buffer.length) ;
    }
    catch(Exception e)
    {
      if (e instanceof SerialPortTimeoutException)
      {
        // Nothing to do, keep running.
      }
      else
      {
        String msg = String.format("error while reading on port '%s': %s",
                                   this._name, e.getMessage()) ;
        throw new SerialComException(msg, e) ; 
      }
    }
    
    SimpleEntry<Integer, byte[]> result = new SimpleEntry<Integer, byte[]>
                                                        (nb_byte_read, buffer) ;
    return result ;
  }

  @Override
  public void close()
  {
    _LOG.debug(String.format("closing port '%s'", this._name));
    this._port.closePort() ;
  }
  
  public static void main(String[] args)
  {
    
  }
}
