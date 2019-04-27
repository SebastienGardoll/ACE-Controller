package fr.gardoll.ace.controller.column;

import java.nio.file.Path ;

import fr.gardoll.ace.controller.common.InitializationException ;

public class CHybride extends Colonne
{
  private final CCone cone ;
  private final CCylindre cylindre ;

  public double volumeRetard ; // volume retard = volume cylindre - volume cône pour la même hauteur  en mL
  // en mL
  
  public CHybride(Path cheminFichierColonne) throws InitializationException
  {
    super(cheminFichierColonne);
    this.cone = new CCone(cheminFichierColonne);
    this.cylindre = new CCylindre(cheminFichierColonne);
    this.volumeRetard = this.cone.volumeEquivalentCylindre() - this.cone.volumeReservoir() ;
  }
  
  public double volumeReservoir()
  { 
    return (this.cone.volumeReservoir() + this.cylindre.volumeReservoir());
  }
  
  public double hauteurReservoir()
  { 
    return (this.cone.hauteurReservoir() + this.cylindre.hauteurReservoir()) ;
  }
  
  public double calculsDeplacementCarrousel(double volume)
  { 
    //pas d'implémentation pour les formes hybrides
    return 0. ;
  } 
  
  public double calculsHauteur(double volume)
  { 
    if (volume > this.cone.volumeReservoir())
    {
      return this.cylindre.calculsHauteur(volume + this.volumeRetard);
    }
    else
    {
      return this.cone.calculsHauteur(volume );
    }
  }
}
