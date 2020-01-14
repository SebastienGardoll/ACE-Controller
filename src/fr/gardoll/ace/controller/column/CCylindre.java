package fr.gardoll.ace.controller.column;

import java.nio.file.Path ;

import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.settings.ConfigurationException ;
import fr.gardoll.ace.controller.settings.Names ;

public class CCylindre extends Colonne
{
  private final double diametre ;  // diametre du cylindre en mm

  private final double hauteurCylindre ; // hauteur du reservoire en mm

  private final double _volumeReservoir ; //volume du réservoir en mL
    //en mL
  
  public CCylindre(Path cheminFichierColonne ) throws ConfigurationException
  {
    super(cheminFichierColonne, TypeColonne.CYLINDRE);
    
    this.hauteurCylindre = this._colSection.getDouble(Names.SICOL_CLEF_H_CYLINDRE, -1.)  ;
    this.diametre        = this._colSection.getDouble(Names.SICOL_CLEF_DIA, -1.)   ;
    
    if (this.hauteurCylindre < 0. ||
        this.diametre        < 0.    )
    {
      String msg = String.format("corrupted column metadata file '%s'", cheminFichierColonne);
      throw new ConfigurationException(msg);
    }

    {
      double tmp =  Math.PI * Math.pow (this.diametre/2. , 2. ) * this.hauteurCylindre ;  //en µL
      this._volumeReservoir = Utils.round(tmp / 1000.) ; //passage au mL
    }
    
    this.close();
  }
  
  @Override
  public double calculsHauteur(double volume)
  {  
    if (volume <= 0.)
    {
      String msg = String.format("volume '%s' cannot be negative or null", volume);
      throw new RuntimeException(msg);
    }

    volume *=1000. ;  // v en ml est traité en micro litre car dimension en mm.

    return Utils.round(((4. * volume) /  (Math.PI * Math.pow (this.diametre, 2.))));
  }

  @Override
  public double calculsDeplacementCarrousel(double volume)
  {  
    //pas d'implémentation pour les formes cylindrique
    return 0. ;
  }

  @Override
  public double volumeReservoir()
  {
    return this._volumeReservoir ;
  }

  @Override
  public double hauteurReservoir()
  {
    return this.hauteurCylindre;
  } 
}
