package fr.gardoll.ace.controller.column;

public enum TypeColonne
{
  CYLINDRE("cylindre"),
  CONE("cone"),
  HYBRIDE("hybride");
  
  public String literal ;

  private TypeColonne(String literal)
  {
    this.literal = literal;
  }
}
