package fr.gardoll.ace.controller.tools.tests;

import java.util.ArrayList ;
import java.util.List ;

import fr.gardoll.ace.controller.autosampler.Passeur ;
import fr.gardoll.ace.controller.pump.PousseSeringue ;
import fr.gardoll.ace.controller.valves.Valves ;

public class AutosamplerTest extends AsbtractTest
{
  public AutosamplerTest(String name)
  {
    super(name) ;
  }

  public static void main(String[] args)
  {
    AutosamplerTest test = new AutosamplerTest("autosampler");
    boolean hasPump = false;
    boolean hasAutosampler = true;
    boolean hasValves = false;
    test.run(hasPump, hasAutosampler, hasValves) ;
  }

  @Override
  protected List<Operation> createOperations(Passeur autosampler,
      PousseSeringue pump, Valves valves)
  {
    
    List<Operation> operations = new ArrayList<>();
    
    operations.add(new Operation("- to trash", ()-> {
      autosampler.moveArmToTrash();
      autosampler.finMoveBras() ;
    }));
    
    operations.add(new Operation("- reinit", ()-> {
      autosampler.reinit();
    }));
    
    return operations;
  }
}
