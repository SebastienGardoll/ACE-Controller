package fr.gardoll.ace.controller.com;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.ParaComException ;

public class ParaComStub implements ParaCom
{
  private static final Logger _LOG = LogManager.getLogger(ParaComStub.class.getName());
  
  public ParaComStub()
  {
    _LOG.debug("instanciating paracom stub");
  }
  @Override
  public String getId()
  {
    _LOG.debug("stubbing method getId");
    return "stub";
  }

  @Override
  public void ouvrir(int numEv) throws ParaComException, InterruptedException
  {
    _LOG.debug(String.format("stubbing openning valve %s", numEv));
  }

  @Override
  public void ouvrirH2O() throws ParaComException, InterruptedException
  {
    _LOG.debug("stubbing openning H20 valve");
  }

  @Override
  public void toutFermer() throws ParaComException, InterruptedException
  {
    _LOG.debug("stubbing closing all valves");
  }

  @Override
  public void close()
  {
    _LOG.debug("stubbing closing paracom");
  }
}
