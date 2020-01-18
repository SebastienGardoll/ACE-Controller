package fr.gardoll.ace.controller.tools.tests;

import java.util.ArrayList ;
import java.util.List ;

import fr.gardoll.ace.controller.autosampler.Passeur ;
import fr.gardoll.ace.controller.pump.PousseSeringue ;
import fr.gardoll.ace.controller.valves.Valves ;

public class ValvesTest extends AbstractTest
{
  public ValvesTest(String name)
  {
    super(name) ;
  }

  public static void main(String[] args)
  {
    ValvesTest test = new ValvesTest("valves");
    boolean hasPump = false;
    boolean hasAutosampler = false;
    boolean hasValves = true;
    test.run(hasPump, hasAutosampler, hasValves) ;
  }

  @Override
  protected List<Operation> createOperations(Passeur autosampler,
      PousseSeringue pump, Valves valves)
  {
    long delay = 5000l;
    List<Operation> operations = new ArrayList<>();
    
    for(int valveId = 1; valveId <= Valves.NB_EV_MAX ; valveId++)
    {
      String operationName = String.format("- open valve %s", valveId);
      final int numValve = valveId;
      operations.add(new Operation(operationName, ()-> {
        valves.ouvrir(numValve);
        Thread.sleep(delay);
      }));
      
      operations.add(new Operation("- close all valves", ()-> {
        valves.toutFermer();
        Thread.sleep(delay);
      }));
    }
    return operations;
  }
}
