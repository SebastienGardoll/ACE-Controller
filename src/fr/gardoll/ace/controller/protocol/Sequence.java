package fr.gardoll.ace.controller.protocol;

public class Sequence
{
  public final String nomAcide ;
  public final int numEv ;
  public final double volume ; //en mL
  public final long temps ;   // en seconde
  public final boolean pause ;

  public Sequence(String nomAcide, int numEv, double volume, long temps, boolean pause)
  {
    this.nomAcide = nomAcide;
    this.numEv = numEv;
    this.volume = volume;
    this.temps = temps;
    this.pause = pause;
  }
  
  @Override
  public String toString()
  {
    String result = null;
    
    if(this.pause)
    {
      result = String.format("%s mL of %s(%s) during %s %s (pause)",
          this.volume, this.nomAcide, this.numEv, this.temps);
    }
    else
    {
      result = String.format("%s mL of %s(%s) during %s %s",
          this.volume, this.nomAcide, this.numEv, this.temps);
    }
    
    return result;
  }
}
