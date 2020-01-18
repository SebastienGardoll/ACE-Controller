package fr.gardoll.ace.controller.tools.tests;

import java.util.ArrayList ;
import java.util.List ;

public class AutosamplerTest
{
  public static void main(String[] args)
  {
    String name = "autosampler";
    List<Operation> operations = new ArrayList<>();
    
    
    operations.add(new Operation("coucou", ()-> {
      System.out.println("coucou"); 
    }));
    
    operations.add(new Operation("hello", ()-> {
      System.out.println("hello"); 
    }));
    
    TestDriver driver = new TestDriver(name, operations);
    boolean hasPump = false;
    boolean hasAutosampler = true;
    boolean hasValves = false;
    driver.run(hasPump, hasAutosampler, hasValves) ;
  }
}
