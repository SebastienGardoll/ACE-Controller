package fr.gardoll.ace.controller.com;

import java.io.File ;
import java.nio.charset.Charset;
import java.util.AbstractMap.SimpleEntry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortTimeoutException;

import fr.gardoll.ace.controller.core.Utils ;

public class JSerialComm implements SerialCom
{
  
  private static final Logger _LOG = LogManager.getLogger(JSerialComm.class.getName());
  
  private SerialPort _port    = null ;
  private Charset _charset = Charset.forName("ASCII"); // DEFAULT
  private int _sizeReadBuffer = 256; // DEFAULT
  private boolean _isOpened   = false;
  private String _id          = "unknown" ;
  private int _readMode       = SerialPort.TIMEOUT_READ_BLOCKING ;
  private int _writeMode      = SerialPort.TIMEOUT_WRITE_BLOCKING ;
  private int _mode           = this._readMode | this._writeMode ;
  private final String _portPath ;
  
  public JSerialComm(String portPath)
  {
    _LOG.debug("initializing the JSerialComm");
    
    this._portPath = portPath;
    
    _LOG.debug(String.format("port is '%s'.", portPath));
    
    File file = new File(portPath);
    this._id = file.getName() ;
    
    _LOG.debug(String.format("computed id of the port is '%s'.", this._id));
    
    _LOG.debug("get comm port");
    this._port = SerialPort.getCommPort(portPath) ;
  }
  
  // Time (milliseconds) to wait after opening the port.
  @Override
  public void open(int openingDelay) throws SerialComException, InterruptedException
  {
    if (this._isOpened)
    {
      String msg = String.format("the port '%s' is already openned.", this.getId());
      _LOG.error(msg) ;
      return;
    }
    
    try
    {
      if (this._port.openPort())
      {
        _LOG.debug(String.format("port '%s' is openned.", this.getId())) ;
      }
      else
      {
        String msg = String.format("fail to open port '%s'.", this.getId());
        throw new SerialComException(msg) ;
      } 
      
      Thread.sleep(openingDelay);
      
      this._isOpened = true;
    }
    catch(InterruptedException e)
    {
      throw e;
    }
    catch(Exception e)
    {
      this._port = null ;
      String msg = String.format("unable to open port '%s'", this.getId()) ;
      throw new SerialComException(msg, e) ;
    }
  }
  
  @Override
  public void setVitesse(int vitesse) throws SerialComException
  {
    _LOG.debug(String.format("setting the baud rate of port '%s' to '%s'",
        this._id, vitesse)) ;
    this._port.setBaudRate(vitesse) ;
  }

  @Override
  public void setParite(Parity choix) throws SerialComException
  {
    _LOG.debug(String.format("setting the parity of port '%s' to '%s'",
        this._id, choix)) ;
    
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
        this._id, choix)) ;
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
        this._id, nbBit)) ;
    this._port.setNumDataBits(nbBit) ;
  }

  @Override
  public void setControlFlux(FlowControl choix) throws SerialComException
  {
    _LOG.debug(String.format("setting the flow control of port '%s' to '%s'",
        this._id, choix)) ;
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
    _LOG.debug(String.format("setting the timeout of port '%s' to '%s'", this._id,
        delais)) ;
    this._port.setComPortTimeouts(this._mode, delais, delais) ;
  }

  @Override
  public void ecrire(String ordre) throws SerialComException
  {
    //_LOG.trace(String.format("writing '%s' on port '%s'", ordre, this._id)) ;
    
    try
    {
      byte[] buffer = ordre.getBytes(this._charset) ;
      int nb_byte_sent = this._port.writeBytes(buffer, buffer.length) ;
      
      if (nb_byte_sent < 0)
      {
        String msg = String.format("transmission error while sending order '%s' on port '%s'",
                                   ordre, this._id) ;

        throw new SerialComException(msg) ;
      }
      
      if (nb_byte_sent != buffer.length)
      {
        String msg = String.format("transmission error while sending order '%s' on port '%s': only %s bytes sent out of %s",
            ordre, this._id, nb_byte_sent, buffer.length) ;
        
        throw new SerialComException(msg) ;
      }
    }
    catch (Exception e)
    {
      String msg = String.format("transmission error while sending order '%s' one port '%s': %s",
                                 ordre, this._id, e.getMessage()) ; 
      throw new SerialComException(msg, e) ;
    }
  }

  @Override
  public String lire() throws SerialComException
  {
    //_LOG.trace(String.format("reading on port '%s'", this._id)) ;
    StringBuilder sb = new StringBuilder("") ;
    
    boolean continue_to_read = true ;
    
    while(continue_to_read)
    {
      SimpleEntry<Integer, byte[]> buffer = internal_read() ;
      
      // Timeout occurs.
      if(buffer.getKey() <= 0)
      {
        break;
      }
      
      // Truncate the buffer after the meeting the first termination byte (code zero).
      int length = 0;
      for(int index = 0; index < buffer.getValue().length ; index++)
      {
        if(buffer.getValue()[index] != 0)
        {
          length++;
        }
        else
        {
          break;
        }
      }
      String rawResult = new String(buffer.getValue(), 0, length, this._charset) ;
      
      // Add the intermediate result to the result.
      sb.append(rawResult) ;
      
      // Was the buffer full ?
      continue_to_read = buffer.getKey() == buffer.getValue().length ;
    }
    
    String result = sb.toString() ; 
    //_LOG.trace(String.format("read '%s'", result));

    return result ;
  }
  
  private SimpleEntry<Integer, byte[]> internal_read() throws SerialComException
  {
    byte[] buffer = new byte[this._sizeReadBuffer] ;
    int nb_byte_read = 0 ;
    
    try
    {
      // Blocking until timeout elapsed or read the size of the buffer.
      // SerialPortTimeoutException is not raised normally.
      // Return -1 on error.
      nb_byte_read = this._port.readBytes(buffer, buffer.length) ;
      
      if(nb_byte_read < 0)
      {
        String msg = String.format("error while reading on port '%s'",
            this._id) ;
        throw new SerialComException(msg) ;
      }
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
                                   this._id, e.getMessage()) ;
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
    if(this._isOpened)
    {
      _LOG.debug(String.format("closing port '%s'", this._id));
      this._port.closePort() ;
      this._isOpened = false;
    }
    else
    {
      _LOG.debug(String.format("port '%s' is already closed", this._id));
    }
  }
  
  @Override
  public void write(byte[] ordre) throws SerialComException
  {
    String orderString = Utils.toString(ordre, ", ");
    //_LOG.trace(String.format("writing '%s' on port '%s'", orderString, this._id)) ;
    try
    {
      int nb_byte_sent = this._port.writeBytes(ordre, ordre.length) ;
      
      if (nb_byte_sent < 0)
      {
        String msg = String.format("transmission error while sending order '%s' on port '%s'",
            orderString, this._id) ;

        throw new SerialComException(msg) ;
      }
      
      if (nb_byte_sent != ordre.length)
      {
        String msg = String.format("transmission error while sending order '%s' on port '%s': only %s bytes sent out of %s",
            orderString, this._id, nb_byte_sent, ordre.length) ;
        
        throw new SerialComException(msg) ;
      }
    }
    catch (Exception e)
    {
      String msg = String.format("transmission error while sending order '%s' one port '%s': %s",
          orderString, this._id, e.getMessage()) ; 
      throw new SerialComException(msg, e) ;
    }
  }
  
  @Override
  public String getId()
  {
    return this._id;
  }

  @Override
  public void setMode(SerialMode readMode, SerialMode writeMode)
  {
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
  public void setCharset(Charset charset)
  {
    this._charset = charset;
  }

  @Override
  public void setReadBufferSize(int nbOfBytes)
  {
    this._sizeReadBuffer = nbOfBytes;
  }

  @Override
  public String getPath()
  {
    return this._portPath;
  }
  
  public static void main(String[] args)
  {
    // To be modified.
    String portPath = "/dev/cu.usbserial-A602K71L";
    
    JSerialComm port = new JSerialComm(portPath);
    
    try
    {
      System.out.println("begin") ;
      
      port.setReadBufferSize(10);
      port.setMode(SerialMode.FULL_BLOCKING, SerialMode.FULL_BLOCKING);
      port.setCharset(Charset.forName("ASCII"));
      port.setVitesse(9600);
      port.setTimeOut(10);
      port.setByteSize(8);
      port.setParite(Parity.NOPARITY);
      port.setStopBit(StopBit.ONESTOPBIT);
      
      port.open(1000);
      
      String msg = "cou cou\n";
      System.out.println(String.format("sending: '%s'", msg)) ;
      port.ecrire(msg);
      
      String received = port.lire();
      System.out.println(String.format("received: '%s'", received)) ;
        
      port.close();
      System.out.println("end") ;
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
}
