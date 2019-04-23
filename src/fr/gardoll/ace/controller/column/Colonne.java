package fr.gardoll.ace.controller.column;

public class Colonne
{

  public int hauteurReservoir()
  {
    // TODO Auto-generated method stub
    return 0 ;
  }

  public int hauteurColonne()
  {
    // TODO Auto-generated method stub
    return 0 ;
  }

  public double volumeCritique1()
  {
    // TODO Auto-generated method stub
    return 0 ;
  }

  public double pousseSeringueDebitMin()
  {
    // TODO Auto-generated method stub
    return 0 ;
  }

  public double volumeCritique2()
  {
    // TODO Auto-generated method stub
    return 0 ;
  }

  public double pousseSeringueDebitInter()
  {
    // TODO Auto-generated method stub
    return 0 ;
  }

  public double pousseSeringueDebitMax()
  {
    // TODO Auto-generated method stub
    return 0 ;
  }

  public int hauteurMenisque()
  {
    // TODO Auto-generated method stub
    return 0 ;
  }

  public double calculsHauteur(double volume)
  {
    // TODO Auto-generated method stub
    return 0 ;
  }

  public double calculsDeplacementCarrousel(double volume)
  {
    // TODO Auto-generated method stub
    return 0 ;
  }

}

/*
  TIniFile * fichierColonne ;
  
  // dimensions en mm

  double _hauteurMenisque ; // hauteur du menisque dans le réservoir

  double _hauteurColonne ; // hauteur dépassant du plateau



  double _pousseSeringueDebitMin ; // débits utilisés pour l'algorithme de distribution d'éluant
  double _pousseSeringueDebitMax ; // volume éluant < volumeCritique1 => debitMin
  double _pousseSeringueDebitInter ; //  volumeCritique1<= volume éluant < volumeCritique2 => debitInter
         //en mL/min               // volumeCritique2 <= volume éluant => débit max




  double _volumeCritique1 ;   // en mL   volume critique servent à modifier le débit
  double _volumeCritique2 ;   // du pousse seringue

  double  virtual hauteurColonne ()   const { return _hauteurColonne ; };
  double  virtual hauteurMenisque ()  const { return _hauteurMenisque ; };

  double  virtual volumeCritique1 () const  { return _volumeCritique1 ; };
  double  virtual volumeCritique2 () const  { return _volumeCritique2 ; };

  double  virtual pousseSeringueDebitMin ()   const { return _pousseSeringueDebitMin ; };
  double  virtual pousseSeringueDebitMax ()   const { return _pousseSeringueDebitMax ; };
  double  virtual pousseSeringueDebitInter () const { return _pousseSeringueDebitInter ; };
  
  double virtual calculsHauteur ( double Volume ) const = 0 ; //calculs la hauteur du liquide contenu dans le réservoir en mm
  // en mm                        en mL
                     

  double virtual calculsDeplacementCarrousel ( double volume ) const = 0 ; //calculs le déplacement supplémentaire du carrousel
  // en mm                                    en mL
                     

  double virtual volumeReservoir () const = 0 ; //renvoie le volume du reservoire
  //en mL

  
  //assesseurs


  double  virtual hauteurReservoir () const = 0 ;

  
  Colonne::Colonne ( const AnsiString & cheminFichierColonne )

  {         // attention dans le fichier init la virgule des réels est : .
    
    DecimalSeparator = '.';

    if ( ! FileExists ( cheminFichierColonne ) ) throw EColonne ( CL_ERREUR_OUVERTURE + cheminFichierColonne + C_ERREUR_OUVERTURE ) ;

    fichierColonne = new TIniFile ( cheminFichierColonne ) ;

    this->_hauteurColonne = fichierColonne->ReadFloat (SEC_INFO_COL, SICOL_CLEF_H_COLONNE, -1. );

    this->_hauteurMenisque = fichierColonne->ReadFloat (SEC_INFO_COL, SICOL_CLEF_H_MENISQUE, -1.);

    this->_pousseSeringueDebitMin = fichierColonne->ReadFloat(SEC_INFO_COL, SICOL_CLEF_COl_DEBIT_MIN, -1. ) ;

    this->_pousseSeringueDebitInter = fichierColonne->ReadFloat(SEC_INFO_COL, SICOL_CLEF_COL_DEBIT_INTER, -1. ) ;

    this->_pousseSeringueDebitMax = fichierColonne->ReadFloat(SEC_INFO_COL, SICOL_CLEF_COL_DEBIT_MAX, -1. ) ;

    this->_volumeCritique1 =  fichierColonne->ReadFloat(SEC_INFO_COL, SICOL_CLEF_VOL_CRITIQUE_1, -1. ) ;

    this->_volumeCritique2 =  fichierColonne->ReadFloat(SEC_INFO_COL, SICOL_CLEF_VOL_CRITIQUE_2, -1. ) ;

    if ( _hauteurColonne           < 0 ||
         _hauteurMenisque          < 0 ||
         _pousseSeringueDebitMin   < 0 ||
         _pousseSeringueDebitInter < 0 ||
         _pousseSeringueDebitMax   < 0 ||
         _volumeCritique1          < 0 ||
         _volumeCritique2          < 0    ) throw EColonne (CL_ERREUR_FICHIER+cheminFichierColonne);
  }

*/