package fr.gardoll.ace.controller.tools.tests;

import fr.gardoll.ace.controller.core.AbstractCancelableToolControl ;
import fr.gardoll.ace.controller.core.AbstractThreadControl ;
import fr.gardoll.ace.controller.core.AbstractToolControl ;
import fr.gardoll.ace.controller.core.CancellationException ;
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.settings.ConfigurationException ;
import fr.gardoll.ace.controller.settings.ParametresSession ;

public class AutosamplerTest
{

  class AutosamplerControl extends AbstractCancelableToolControl
  {
    public AutosamplerControl(ParametresSession parametresSession)
        throws InitializationException, ConfigurationException
    {
      super(parametresSession, false, true, false) ;
    }

    @Override
    protected void closeOperations()
    {
      // TODO
      
    }

    @Override
    protected String getToolName()
    {
      // TODO 
      return null ;
    }
  }
  
  class AutosamplerTestThread extends AbstractThreadControl
  {
    public AutosamplerTestThread(AbstractToolControl toolCtrl)
    {
      super(toolCtrl, false) ;
    }

    @Override
    protected void threadLogic() throws CancellationException,
        InitializationException, ConfigurationException, Exception
    {
      // TODO
      // how to update list of task in AbstractPanel ?
    }
  }
  
  public static void main(String[] args)
  {
    // Should do as ToolFrame class.
  }

}
