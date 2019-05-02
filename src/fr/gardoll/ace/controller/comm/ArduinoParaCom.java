package fr.gardoll.ace.controller.comm;

import java.nio.charset.Charset ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.common.InitializationException ;
import fr.gardoll.ace.controller.common.ParaComException ;
import fr.gardoll.ace.controller.common.SerialComException ;

public class ArduinoParaCom implements ParaCom
{
  private static final int ATTENTE_EV = 200 ; // en milisecondes
  
  private static final Logger _LOG = LogManager.getLogger(ArduinoParaCom.class.getName());
  
  private final JSerialComm _port;

  public ArduinoParaCom(String portPath) throws InitializationException, InterruptedException
  {
    this._port = new JSerialComm(SerialMode.FULL_BLOCKING,
        SerialMode.FULL_BLOCKING, Charset.forName("ASCII"), 1);
    
    try
    {
      this._port.open(portPath);
      
      this._port.setVitesse(9600);
      this._port.setTimeOut(100);
      this._port.setByteSize(8);
      this._port.setParite(Parity.NOPARITY);
      this._port.setStopBit(StopBit.ONESTOPBIT);
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while initializing the port '%s': %s",
          portPath, e.getMessage());
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
  public void ouvrir(int numEv) throws ParaComException, InterruptedException
  {
    if (numEv < 0)
    {
      String msg = String.format("isolation valve number '%s' cannot be negative", numEv);
      _LOG.fatal(msg);
      throw new ParaComException(msg);
    }

    byte ordre ;

    switch (numEv)
    {
      case 0 : { ordre = 0 ; break ; }   //ttes ev fermées

      case 1 : { ordre = 1 ; break ; }  // uniquement l'ev 1 ouverte.

      case 2 : { ordre = 2 ; break ; }  // ...

      case 3 : { ordre = 4 ; break ; }

      case 4 : { ordre = 8 ; break ; }

      case 5 : { ordre = 16 ; break ; }

      case 6 : { ordre = 32 ; break ; }

      case 7 : { ordre = 64 ; break ; }

      default :
      {
        String msg = String.format("unsupported isolation valve number '%s'", numEv);
        _LOG.fatal(msg);
        throw new ParaComException(msg);
      }
    }
    
    try
    {
      this._port.write(Integer.valueOf(ordre).byteValue());
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while writing on port '%s': %s",
          this._port.getId(), e.getMessage());
      _LOG.fatal(msg, e);
      throw new ParaComException(msg, e);
    }
    
    Thread.sleep(ATTENTE_EV) ; //temps d'attente de l'exécution mécanique de l'ordre
  }

  @Override
  public void ouvrirH2O() throws ParaComException, InterruptedException
  {
    this.ouvrir(NUM_EV_H2O); 
  }

  @Override
  public void toutFermer() throws ParaComException, InterruptedException
  {
    this.ouvrir(0) ; 
  }

  @Override
  public void close()
  {
    this._port.close();
  }
}
