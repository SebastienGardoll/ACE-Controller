package fr.gardoll.ace.controller.tools.tests;

import java.util.ArrayList ;
import java.util.List ;

import fr.gardoll.ace.controller.autosampler.Passeur ;
import fr.gardoll.ace.controller.pump.PousseSeringue ;
import fr.gardoll.ace.controller.valves.Valves ;

public class AutosamplerTest extends AbstractTest
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
    long delay = 5000l;
    List<Operation> operations = new ArrayList<>();
    
    operations.add(new Operation("- referencing (nothing should happen)", ()-> {
      autosampler.setOrigineBras();
      Thread.sleep(delay);
    }));
    
    operations.add(new Operation("- asking if the arm is moving (nothing should happen)", ()-> {
      autosampler.finMoveBras();
      Thread.sleep(delay);
    }));
    
    operations.add(new Operation("- asking if the carousel is moving (nothing should happen)", ()-> {
      autosampler.finMoveCarrousel();
      Thread.sleep(delay);
    }));
    
    operations.add(new Operation("- arm goes down of 50 mm", ()-> {
      int nbStep = Passeur.convertBras(-50.);
      autosampler.moveBras(nbStep);
      autosampler.finMoveBras() ;
      autosampler.setOrigineBras();
      Thread.sleep(delay);
    }));
    
    operations.add(new Operation("- arm goes up of 25 mm", () -> {
      int nbStep = Passeur.convertBras(25.);
      autosampler.moveBras(nbStep);
      autosampler.finMoveBras() ;
      Thread.sleep(delay);
    })) ;

    operations.add(new Operation("- arm returns to the last position", () -> {
      autosampler.moveOrigineBras();
      autosampler.finMoveBras() ;
      Thread.sleep(delay);
    })) ;
    
    operations.add(new Operation("- arm goes to the top", ()-> {
      autosampler.moveButeBras();
      autosampler.finMoveBras() ;
      autosampler.setOrigineBras();
      Thread.sleep(delay);
    }));
    
    operations.add(new Operation("- arm goes to trash", ()-> {
      autosampler.moveArmToTrash();
      autosampler.finMoveBras() ;
      Thread.sleep(delay);
    }));
    
    operations.add(new Operation("- arm goes to the top", ()-> {
      autosampler.moveButeBras();
      autosampler.finMoveBras() ;
      autosampler.setOrigineBras();
      Thread.sleep(delay);
    }));
    
    operations.add(new Operation("- referencing carousel (nothing should happen)", () -> {
      autosampler.setOrigineCarrousel();
      Thread.sleep(delay);
    })) ;
    
    operations.add(new Operation("- carousel turns 6 positions to the LEFT", ()-> {
      autosampler.moveCarrousel(-6);
      autosampler.finMoveCarrousel();
      Thread.sleep(delay);
    }));
    
    operations.add(new Operation("- carousel turns 3 positions to the RIGHT", ()-> {
      autosampler.moveCarrousel(3);
      autosampler.finMoveCarrousel();
      Thread.sleep(delay);
    }));
    
    operations.add(new Operation("- carousel returns to the trash (3 positions to the RIGHT)", () -> {
      autosampler.moveOrigineCarrousel();
      autosampler.finMoveCarrousel();
      Thread.sleep(delay);
    })) ;

    operations.add(new Operation("- reinit (nothing should happen)", ()-> {
      autosampler.reinit();
      Thread.sleep(delay);
    }));
    
    return operations;
  }
}
