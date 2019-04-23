package fr.gardoll.ace.controller.column;

import java.io.File ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.common.InitializationException ;

public class CCylindre extends Colonne
{
  private double diametre ;  // diametre du cylindre en mm

  private double hauteurCylindre ; // hauteur du reservoire en mm

  private double _volumeReservoir ; //volume du réservoir en mL
    //en mL
  
  private static final Logger _LOG = LogManager.getLogger(CCylindre.class.getName());

  public CCylindre(File cheminFichierColonne ) throws InitializationException
  {
    super(cheminFichierColonne);
    
    /* XXX TODO
    this.hauteurCylindre= fichierColonne->ReadFloat ( SEC_INFO_COL, SICOL_CLEF_H_CYLINDRE, -1.)  ;

    this.diametre = fichierColonne->ReadFloat ( SEC_INFO_COL, SICOL_CLEF_DIA, -1.)   ;
    */
    
    if (this.hauteurCylindre < 0 ||
        this.diametre        < 0    )
    {
      String msg = String.format("corrupted column metadata file '%s'", cheminFichierColonne);
      _LOG.fatal(msg);
      throw new InitializationException(msg);
    }

    this._volumeReservoir =  Math.PI * Math.pow (this.diametre/2 , 2 ) * this.hauteurCylindre ;  //en µL

    this._volumeReservoir /= 1000 ; //passage au mL
  }
  
  public double calculsHauteur(double volume)
  {  
    if (volume <= 0.)
    {
      String msg = String.format("volume '%s' cannot be negative or null", volume);
      _LOG.fatal(msg);
      throw new RuntimeException(msg);
    }

    volume *=1000. ;  // v en ml est traité en micro litre car dimension en mm.

    return ((4 * volume) /  (Math.PI * Math.pow (this.diametre, 2))) ;
  }

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
