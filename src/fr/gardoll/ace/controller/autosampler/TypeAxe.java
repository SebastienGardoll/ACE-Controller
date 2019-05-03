package fr.gardoll.ace.controller.autosampler;

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
