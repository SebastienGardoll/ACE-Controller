package fr.gardoll.ace.controller.valves;

import java.util.Arrays ;

import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.com.ParaCom ;
import fr.gardoll.ace.controller.com.ParaComException ;
import fr.gardoll.ace.controller.core.Log ;

public class ParaComStub implements ParaCom
{
  private static final Logger _LOG = Log.STUB;
  
  public ParaComStub()
  {
    _LOG.debug("instanciating paracom stub");
  }
  @Override
  public String getId()
  {
    return "stub";
  }

  @Override
  public void close()
  {
    _LOG.debug("stubbing close paracom");
  }
  
  @Override
  public void send(byte[] order) throws ParaComException
  {
    String msg = String.format("send order '%s'", Arrays.toString(order));
    _LOG.trace(msg);
  }
  
  @Override
  public String getPortPath()
  {
    return "stub_port";
  }
}
