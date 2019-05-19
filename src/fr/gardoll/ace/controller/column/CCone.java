package fr.gardoll.ace.controller.column;

import java.nio.file.Path ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.common.InitializationException ;
import fr.gardoll.ace.controller.common.Names ;

public class CCone extends Colonne
{
  private final double hauteurCone ;  // hauteur du réservoir

  private final double diametreSup ; // diametre supérieure du réservoir conique ( tronc de cône )

  private final double diametreInf ; // diametre inférieur

  private final double a, b, c ;    // intermédiaire de résolution eq 3°

  private final double _volumeReservoir ; //volume du réservoir en mL
    //en mL
  
  private static final Logger _LOG = LogManager.getLogger(CCone.class.getName());
  
  public CCone(Path cheminFichierColonne) throws InitializationException
  { 
    super(cheminFichierColonne, TypeColonne.CONE);
    
    this.hauteurCone = this._colSection.getDouble(Names.SICOL_CLEF_H_CONE, -1.)  ;
    this.diametreSup = this._colSection.getDouble(Names.SICOL_CLEF_DIA_SUP, -1.) ;
    this.diametreInf = this._colSection.getDouble(Names.SICOL_CLEF_DIA_INF, -1.) ;
    
    if (this.diametreSup == this.diametreInf )
    {
      String msg = String.format("superior diameter '%s' must be greater than the inferior diameter '%s'",
          this.diametreSup, this.diametreInf);
      _LOG.fatal(msg);
      throw new InitializationException(msg);
    }

    if ( this.hauteurCone < 0 ||
         this.diametreSup < 0 ||
         this.diametreInf < 0    )
    {
      String msg = String.format("corrupted column metadata file '%s'", cheminFichierColonne);
      _LOG.fatal(msg);
      throw new InitializationException(msg);
    }

    a = Math.pow(this.diametreSup - this.diametreInf, 2) / Math.pow (this.hauteurCone, 2) ;
    b = 3 * this.diametreInf * (this.diametreSup - this.diametreInf ) / this.hauteurCone ;
    c = 3 * Math.pow (this.diametreInf, 2);

    {
      double tmp = Math.PI * this.hauteurCone * (Math.pow (this.diametreSup/2, 2) + (this.diametreSup * this.diametreInf)/4 + Math.pow (this.diametreInf/2, 2)) / 3; //en µL
      this._volumeReservoir = tmp / 1000 ; //passage au mL
    }
  }
  
  @Override
  public double calculsHauteur(double volume)
  {  
    if ( volume <= 0. )
    {
      String msg = String.format("volume '%s' cannot be negative or null", volume);
      _LOG.fatal(msg);
      throw new RuntimeException(msg);
    }

    volume *= 1000. ; // volume en ml est traité en micro litre car dimensions en mm.

    double d = 12 * volume  / Math.PI ;

    d  = -d ; // valeur aberrante en fesant -12*Volume  / 3.14

    double q = (2 * Math.pow (this.b , 3) / (27 * Math.pow (this.a,  3))) + d/this.a - (this.b * this.c/ (3 * Math.pow (this.a,2))) ;

    double X = Math.pow (-q , 0.33333333333333333333); // 1/3 marche mais résultats aberrant ( -2.714 ... )

    return  X - (this.b / (3 * this.a)) ;
  }
  
  @Override
  public double calculsDeplacementCarrousel(double volume)
  {  
    if (volume <= 0.)
    {
      String msg = String.format("volume '%s' cannot be negative or null", volume);
      _LOG.fatal(msg);
      throw new RuntimeException(msg);
    }

    double h = this.calculsHauteur(volume) ; // le réservoir est cônique

    volume *= 1000. ; // v en ml est traité en micro litre car dimension en mm.

    return (0.25 * ( - this.diametreInf + Math.pow (-3 * this.diametreInf * this.diametreInf + (48 * volume )/( Math.PI * h), 0.5))) ;
  }
  
  public double volumeEquivalentCylindre()
  {  
    return ( Math.PI * Math.pow(this.diametreSup/2, 2) * this.hauteurCone ) /1000.  ;//en mL
  }

  @Override
  public double volumeReservoir()
  {
    return this._volumeReservoir;
  }

  @Override
  public double hauteurReservoir()
  {
    return this.hauteurCone;
  }
}
