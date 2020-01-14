package fr.gardoll.ace.controller.column;

import java.nio.file.Path ;

import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.settings.ConfigurationException ;


public class CHybride extends Colonne
{
  private final CCone cone ;
  private final CCylindre cylindre ;

  public double volumeRetard ; // volume retard = volume cylindre - volume cône pour la même hauteur  en mL
  // en mL
  
  private final double _volumeReservoir;
  private final double _hauteurReservoir;
    
  public CHybride(Path cheminFichierColonne) throws ConfigurationException
  {
    super(cheminFichierColonne, TypeColonne.HYBRIDE);
    this.cone = new CCone(cheminFichierColonne);
    this.cylindre = new CCylindre(cheminFichierColonne);
    this.volumeRetard = this.cone.volumeEquivalentCylindre() - this.cone.volumeReservoir() ;
    
    this._volumeReservoir = Utils.round((this.cone.volumeReservoir() + this.cylindre.volumeReservoir()));
    this._hauteurReservoir = Utils.round((this.cone.hauteurReservoir() + this.cylindre.hauteurReservoir()));
    
    this.close();
  }
  
  @Override
  public double volumeReservoir()
  { 
    return this._volumeReservoir;
  }
  
  @Override
  public double hauteurReservoir()
  { 
    return this._hauteurReservoir ;
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
    double result ;
    
    if (volume > this.cone.volumeReservoir())
    {
      result = this.cylindre.calculsHauteur(volume + this.volumeRetard);
    }
    else
    {
      result = this.cone.calculsHauteur(volume );
    }
    
    return Utils.round(result);
  }
}
