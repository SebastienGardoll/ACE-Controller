package fr.gardoll.ace.controller.valves;

import java.io.Closeable ;
import java.io.IOException ;

import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.com.ArduinoParaCom ;
import fr.gardoll.ace.controller.com.JSerialComm ;
import fr.gardoll.ace.controller.com.ParaCom ;
import fr.gardoll.ace.controller.com.ParaComException ;
import fr.gardoll.ace.controller.core.Log ;

public class Valves implements Closeable
{
  public static final int NUM_SHUT_IV = 0;
  
  // numéro de l'électrovanne de l'eau
  public static final int NUM_EV_H2O = 1 ;

  // refoulement vers le carrousel
  public static final int NUM_EV_REFOULEMENT = 7 ;

  // nombre d'électrovannes possible
  public static final int NB_EV_MAX = 7 ;

  private final ParaCom _paracom ;
  
  public Valves(ParaCom paraCom)
  {
    _LOG.debug("initializing the valves");
    this._paracom = paraCom;
  }
  
  private static final long ATTENTE_EV = 200l ; // en milisecondes
  
  private static final Logger _LOG = Log.HIGH_LEVEL;
  
  public void ouvrir(int numEv) throws ParaComException
  {
    String debugMsg = null;
    
    switch(numEv)
    {
      case NUM_SHUT_IV:
      {
        debugMsg = "shutting all the valves";
        break;
      }
      
      case NUM_EV_H2O:
      {
        debugMsg = "openning the H2O valve";
        break;
      }
      
      case NUM_EV_REFOULEMENT:
      {
        debugMsg = "openning the withdrawing valve";
        break;
      }
      
      default:
      {
        debugMsg = String.format("openning the valve '%s'", numEv);
        break;
      }
    }
    
    _LOG.debug(debugMsg);
    
    if (numEv < 0)
    {
      String msg = String.format("isolation valve number '%s' cannot be negative", numEv);
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
      
      case 8 : { ordre = -128 ; break;}

      default :
      {
        String msg = String.format("unsupported isolation valve number '%s'", numEv);
        throw new ParaComException(msg);
      }
    }
    
    try
    {
      byte b = Integer.valueOf(ordre).byteValue();
      _LOG.trace(String.format("sending order '%s'", ordre));
      this._paracom.send(new byte[] {b});
      try
      {
        // temps d'attente de l'exécution mécanique de l'ordre
        Thread.sleep(ATTENTE_EV) ;
      }
      catch (InterruptedException e)
      {
        throw new RuntimeException(e);
      } 
    }
    catch(ParaComException e)
    {
      String msg = String.format("error while writing on port '%s'",
          this._paracom.getId());
      throw new ParaComException(msg, e);
    }
  }
  
  public void ouvrirH2O() throws ParaComException
  {
    this.ouvrir(NUM_EV_H2O); 
  }

  public void toutFermer() throws ParaComException
  {
    this.ouvrir(NUM_SHUT_IV) ; 
  }

  @Override
  public void close() throws IOException
  {
    try
    {
      _LOG.debug("closing the valves");
      this.toutFermer();
    }
    catch(Exception e)
    {
      throw new IOException("error while closing the valves", e);
    }
    
    _LOG.debug(String.format("closing valves controller with paracom id '%s'", this._paracom.getId()));
    this._paracom.close();
  }
  
  public static void main(String[] args)
  {
    // Windows 10: "COM6"
    // CentOS   7: "/dev/ttyUSB0"
    String portPath = "/dev/ttyUSB0"; // To be modified.
    JSerialComm port = new JSerialComm(portPath);
    
    try(ArduinoParaCom paraCom = new ArduinoParaCom(port);
        Valves valves = new Valves(paraCom))
    {
      Thread.sleep(2000);
      _LOG.info("begin");
      
      for(int valveId = 1 ; valveId < 8 ; valveId++)
      {
        valves.ouvrir(valveId);
        Thread.sleep(3000);
        valves.toutFermer();
        Thread.sleep(2000);
      }
      
      _LOG.info("end");
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
}
