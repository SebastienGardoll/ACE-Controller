package fr.gardoll.ace.controller.column;

public class CCone
{
  
}

/*
  private :

  double hauteurCone ;  // hauteur du réservoir

  double diametreSup ; // diametre supérieure du réservoir conique ( tronc de cône )

  double diametreInf ; // diametre inférieur

  double a, b, c ;    // intermédiaire de résolution eq 3°

  double _volumeReservoir ; //volume du réservoir en mL
    //en mL
  
//---------------------------------------------------------------------------


#pragma hdrstop

#include "CCone.h"

//---------------------------------------------------------------------------

#pragma package(smart_init)

CCone::CCone ( const AnsiString & cheminFichierColonne )
: Colonne (  cheminFichierColonne )

{   hauteurCone = fichierColonne->ReadFloat ( SEC_INFO_COL, SICOL_CLEF_H_CONE, -1.)  ;

    diametreSup = fichierColonne->ReadFloat ( SEC_INFO_COL, SICOL_CLEF_DIA_SUP, -1.) ;

    diametreInf = fichierColonne->ReadFloat ( SEC_INFO_COL, SICOL_CLEF_DIA_INF, -1.) ;

    if ( diametreSup == diametreInf ) throw EColonne ( CL_ERREUR_DIM );

    if ( hauteurCone < 0 ||
         diametreSup < 0 ||
         diametreInf < 0    ) throw EColonne (CL_ERREUR_FICHIER+cheminFichierColonne);




    a = pow ( diametreSup-diametreInf, 2) / pow ( hauteurCone, 2) ;
    b = 3 * diametreInf * ( diametreSup - diametreInf ) / hauteurCone ;
    c = 3 * pow (diametreInf, 2);

    _volumeReservoir = M_PI * hauteurCone * ( pow ( diametreSup/2, 2 ) + ( diametreSup * diametreInf )/4 + pow ( diametreInf/2, 2 ) ) / 3; //en µL

    _volumeReservoir /= 1000 ; //passage au mL

}

//---------------------------------------------------------------------------

double CCone::calculsHauteur ( double volume ) const

{  if ( volume <= 0. ) throw EColonne ( CL_ERREUR_VOLUME );

   volume *= 1000. ; // volume en ml est traité en micro litre car dimensions en mm.

   double d = 12 * volume  / M_PI ;

   d  = -d ; // valeur aberrante en fesant -12*Volume  / 3.14

   double q = ( 2 * pow ( b , 3 ) / ( 27 * pow ( a ,  3 ) ) ) + d/a - ( b*c/ ( 3 * pow (a,2))) ;

   double X = pow (  -q , 0.33333333333333333333  ); // 1/3 marche mais résultats aberrant ( -2.714 ... )

   return    X - ( b / ( 3 * a ) )    ;
}

//---------------------------------------------------------------------------

double CCone::calculsDeplacementCarrousel ( double volume ) const

{  if ( volume <= 0. ) throw Exception ( CL_ERREUR_VOLUME );

   double h = calculsHauteur ( volume ) ;   // le réservoir est cônique

   volume *= 1000. ; // v en ml est traité en micro litre car dimension en mm.

   return ( 0.25 * ( -diametreInf + pow ( -3*diametreInf*diametreInf + ( 48*volume )/( M_PI*h ) , 0.5 ) ) ) ;
}

//---------------------------------------------------------------------------

double CCone::volumeEquivalentCylindre () const

{  return ( M_PI * pow ( diametreSup/2, 2 ) * hauteurCone ) /1000.  ; } //en mL

}
*/