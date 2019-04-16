package fr.gardoll.ace.controller.column;

public class CCylindre
{
  
}

/*
  private :

  double diametre ;  // diametre du cylindre en mm

  double hauteurCylindre ; // hauteur du reservoire en mm

  double _volumeReservoir ; //volume du réservoir en mL
    //en mL

  CCylindre::CCylindre ( const AnsiString & cheminFichierColonne )
  : Colonne ( cheminFichierColonne )

  {
     hauteurCylindre= fichierColonne->ReadFloat ( SEC_INFO_COL, SICOL_CLEF_H_CYLINDRE, -1.)  ;

     diametre = fichierColonne->ReadFloat ( SEC_INFO_COL, SICOL_CLEF_DIA, -1.)   ;

     if ( hauteurCylindre < 0 ||
          diametre        < 0    ) throw EColonne (CL_ERREUR_FICHIER+cheminFichierColonne);




     _volumeReservoir =  M_PI * pow ( diametre/2 , 2 ) * hauteurCylindre ;  //en µL

     _volumeReservoir /= 1000 ; //passage au mL
  }

  //---------------------------------------------------------------------------

  double CCylindre::calculsHauteur ( double volume ) const

  {  if ( volume <= 0. ) throw EColonne ( CL_ERREUR_VOLUME );

     volume *=1000. ;  // v en ml est traité en micro litre car dimension en mm.

     return (  ( 4 * volume ) /  ( M_PI * pow ( diametre, 2 ) ) ) ;
  }

  //---------------------------------------------------------------------------

  double CCylindre::calculsDeplacementCarrousel ( double volume ) const

  {  return 0. ; } //pas d'implémentation pour les formes cylindrique




*/
