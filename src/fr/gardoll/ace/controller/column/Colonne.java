package fr.gardoll.ace.controller.column;

import java.io.File ;

import org.apache.commons.configuration2.INIConfiguration ;
import org.apache.commons.configuration2.SubnodeConfiguration ;
import org.apache.commons.configuration2.builder.fluent.Configurations ;
import org.apache.commons.configuration2.ex.ConfigurationException ;
import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.common.InitializationException ;
import fr.gardoll.ace.controller.common.Names ;

public abstract class Colonne
{
  private static final Logger _LOG = LogManager.getLogger(Colonne.class.getName());
  
  protected final File _fichierColonne ;
  protected final INIConfiguration _iniConf;
  
  // dimensions en mm

  protected final double _hauteurMenisque ; // hauteur du menisque dans le réservoir

  protected final double _hauteurColonne ; // hauteur dépassant du plateau



  protected final double _pousseSeringueDebitMin ; // débits utilisés pour l'algorithme de distribution d'éluant
  protected final double _pousseSeringueDebitMax ; // volume éluant < volumeCritique1 => debitMin
  protected final double _pousseSeringueDebitInter ; //  volumeCritique1<= volume éluant < volumeCritique2 => debitInter
         //en mL/min               // volumeCritique2 <= volume éluant => débit max

  protected final double _volumeCritique1 ;   // en mL   volume critique servent à modifier le débit
  protected final double _volumeCritique2 ;   // du pousse seringue

  public double hauteurColonne ()  {return this._hauteurColonne ;}
  public double hauteurMenisque () {return this._hauteurMenisque ;}

  public double volumeCritique1 () {return this._volumeCritique1 ;}
  public double volumeCritique2 () {return this._volumeCritique2 ;}

  public double pousseSeringueDebitMin ()   {return this._pousseSeringueDebitMin ; }
  public double pousseSeringueDebitMax ()   {return this._pousseSeringueDebitMax ; }
  public double pousseSeringueDebitInter () {return this._pousseSeringueDebitInter ; }
  
  
  //calculs la hauteur du liquide contenu dans le réservoir en mm
  public abstract double calculsHauteur(double Volume) ;
  // en mm                        en mL
  
  //calculs le déplacement supplémentaire du carrousel
  public abstract double calculsDeplacementCarrousel(double volume) ;
  // en mm                                    en mL
  
  //renvoie le volume du reservoire
  public abstract double volumeReservoir() ;
  //en mL
  
  public abstract double hauteurReservoir() ;
  
  public Colonne(File cheminFichierColonne) throws InitializationException
  {         
    // attention dans le fichier init la virgule des réels est : .
    
    this._fichierColonne = cheminFichierColonne ;
    
    if (! this._fichierColonne.isFile())
    {
      String msg = String.format("cannot stat column metadata file '%s'", cheminFichierColonne);
      _LOG.fatal(msg);
      throw new InitializationException(msg);
    }
    
    Configurations configs = new Configurations();
    
    try
    {
      this._iniConf = configs.ini(cheminFichierColonne);
      SubnodeConfiguration section = this._iniConf.getSection(Names.SEC_INFO_COL);
      this._hauteurColonne = section.getDouble(Names.SICOL_CLEF_H_COLONNE, -1.);
    }
    catch (ConfigurationException e)
    {
      String msg = String.format("unable to read the column specifications in the file '%s': %s",
          this._fichierColonne.toString(), e.getMessage());
      _LOG.fatal(msg, e);
      throw new RuntimeException(msg,e);
    }

    /* XXX TODO
    this._hauteurColonne = fichierColonne.ReadFloat (SEC_INFO_COL, SICOL_CLEF_H_COLONNE, -1. );

    this._hauteurMenisque = fichierColonne.ReadFloat (SEC_INFO_COL, SICOL_CLEF_H_MENISQUE, -1.);

    this._pousseSeringueDebitMin = fichierColonne.ReadFloat(SEC_INFO_COL, SICOL_CLEF_COl_DEBIT_MIN, -1. ) ;

    this._pousseSeringueDebitInter = fichierColonne.ReadFloat(SEC_INFO_COL, SICOL_CLEF_COL_DEBIT_INTER, -1. ) ;

    this._pousseSeringueDebitMax = fichierColonne.ReadFloat(SEC_INFO_COL, SICOL_CLEF_COL_DEBIT_MAX, -1. ) ;

    this._volumeCritique1 =  fichierColonne.ReadFloat(SEC_INFO_COL, SICOL_CLEF_VOL_CRITIQUE_1, -1. ) ;

    this._volumeCritique2 =  fichierColonne.ReadFloat(SEC_INFO_COL, SICOL_CLEF_VOL_CRITIQUE_2, -1. ) ;
    */
    
    if ( _hauteurColonne           < 0 ||
         _hauteurMenisque          < 0 ||
         _pousseSeringueDebitMin   < 0 ||
         _pousseSeringueDebitInter < 0 ||
         _pousseSeringueDebitMax   < 0 ||
         _volumeCritique1          < 0 ||
         _volumeCritique2          < 0    )
    {
      String msg = String.format("corrupted column metadata file '%s'", cheminFichierColonne);
      _LOG.fatal(msg);
      throw new InitializationException(msg);
    }
  }
}
