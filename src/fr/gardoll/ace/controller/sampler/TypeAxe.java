package fr.gardoll.ace.controller.sampler;

public enum TypeAxe
{
  carrousel,
  bras;
  
  @Override
  public String toString()
  {
    return String.valueOf(this.ordinal());
  }
}
