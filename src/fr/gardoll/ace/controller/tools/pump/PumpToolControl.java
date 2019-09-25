package fr.gardoll.ace.controller.tools.pump;

import fr.gardoll.ace.controller.core.AbstractStateFullToolControl ;
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.ParametresSession ;

public class PumpToolControl extends AbstractStateFullToolControl
{

  public PumpToolControl(ParametresSession parametresSession)
      throws InitializationException, InterruptedException
  {
    super(parametresSession, true, true, true) ;
  }

  @Override
  protected void closeOperations() throws InterruptedException
  {
    // TODO Auto-generated method stub
  }
}
