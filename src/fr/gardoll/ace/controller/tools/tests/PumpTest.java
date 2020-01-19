package fr.gardoll.ace.controller.tools.tests;

import java.util.ArrayList;
import java.util.List;

import fr.gardoll.ace.controller.autosampler.Passeur;
import fr.gardoll.ace.controller.pump.PousseSeringue;
import fr.gardoll.ace.controller.valves.Valves;

public class PumpTest extends AbstractTest
{

  public PumpTest(String name)
  {
    super(name);
  }

  public static void main(String[] args)
  {
    PumpTest test = new PumpTest("pump");
    boolean hasPump = true;
    boolean hasAutosampler = false;
    boolean hasValves = true;
    test.run(hasPump, hasAutosampler, hasValves);
  }

  @Override
  protected List<Operation> createOperations(Passeur autosampler,
      PousseSeringue pump, Valves valves)
  {
    long delay = 5000l;
    List<Operation> operations = new ArrayList<>();

    operations.add(new Operation("- asking if the pump is pumping (nothing should happen) ", ()-> {
      pump.finPompage();
      Thread.sleep(delay);
    }));
    
    operations.add(new Operation("- setting the withdrawing rate (nothing should happen)", ()-> {
      pump.setDebitRefoulement(10.);
      Thread.sleep(delay);
    }));
    
    operations.add(new Operation("- setting the infusing rate (nothing should happen)", ()-> {
      pump.setDebitAspiration(10.);
      Thread.sleep(delay);
    }));
    
    operations.add(new Operation("- withdrawing 1 mL", ()-> {
      pump.aspiration(1., Valves.NUM_EV_H2O);
      pump.finPompage();
      Thread.sleep(delay);
    }));
    
    operations.add(new Operation("- infusing 0.5 mL", ()-> {
      pump.refoulement(0.5, Valves.NUM_EV_REFOULEMENT);
      pump.finPompage();
      Thread.sleep(delay);
    }));
    
    operations.add(new Operation("- asking the delivered volume (nothing should happen)", ()-> {
      pump.volumeDelivre();
      Thread.sleep(delay);
    }));
    
    operations.add(new Operation("- reinit: drain the pump (infusing 0.5 mL)", ()-> {
      pump.reinit();
      Thread.sleep(delay);
    }));
    
    return operations;
  }

}
