package fr.gardoll.ace.controller.column;

public class CHybride
{
  
}

/*
  private :  CCone cone ;
CCylindre cylindre ;

//  double volumeRetard ; // volume retard = volume cylindre - volume cône pour la même hauteur  en mL
// en mL

double volumeRetard ; // volume retard = volume cylindre - volume cône pour la même hauteur  en mL
// en mL

CHybride::CHybride (  const AnsiString & cheminFichierColonne )
:  Colonne ( cheminFichierColonne ),
   cone ( cheminFichierColonne ),
   cylindre ( cheminFichierColonne )

{
   volumeRetard = cone.volumeEquivalentCylindre() - cone.volumeReservoir() ;
}

//---------------------------------------------------------------------------

double CHybride::volumeReservoir () const

{ return ( cone.volumeReservoir() + cylindre.volumeReservoir() ); }

//---------------------------------------------------------------------------

double CHybride::hauteurReservoir () const

{ return ( cone.hauteurReservoir() + cylindre.hauteurReservoir()) ; }

//---------------------------------------------------------------------------

double CHybride::calculsDeplacementCarrousel ( double volume ) const

{ return 0. ; } //pas d'implémentation pour les formes hybrides

//---------------------------------------------------------------------------

double CHybride::calculsHauteur ( double volume ) const

{   if ( volume > cone.volumeReservoir() )

          return cylindre.calculsHauteur ( volume + volumeRetard );

    else  return cone.calculsHauteur ( volume );
}

//---------------------------------------------------------------------------




*/