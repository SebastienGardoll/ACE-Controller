package fr.gardoll.ace.controller.column;

import java.nio.file.Path ;

import fr.gardoll.ace.controller.settings.ConfigurationException ;


public class CHybride extends Colonne
{
  private final CCone cone ;
  private final CCylindre cylindre ;

  public double volumeRetard ; // volume retard = volume cylindre - volume cône pour la même hauteur  en mL
  // en mL
  
  public CHybride(Path cheminFichierColonne) throws ConfigurationException
  {
    super(cheminFichierColonne, TypeColonne.HYBRIDE);
    this.cone = new CCone(cheminFichierColonne);
    this.cylindre = new CCylindre(cheminFichierColonne);
    this.volumeRetard = this.cone.volumeEquivalentCylindre() - this.cone.volumeReservoir() ;
    this.close();
  }
  
  @Override
  public double volumeReservoir()
  { 
    return (this.cone.volumeReservoir() + this.cylindre.volumeReservoir());
  }
  
  @Override
  public double hauteurReservoir()
  { 
    return (this.cone.hauteurReservoir() + this.cylindre.hauteurReservoir()) ;
  }
  
  @Override
  public double calculsDeplacementCarrousel(double volume)
  { 
    //pas d'implémentation pour les formes hybrides
    return 0. ;
  } 
  
  @Override
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
