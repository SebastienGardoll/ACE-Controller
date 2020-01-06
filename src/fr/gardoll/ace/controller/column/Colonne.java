package fr.gardoll.ace.controller.column;

import java.nio.file.Files ;
import java.nio.file.Path ;
import java.nio.file.Paths ;

import org.apache.commons.configuration2.INIConfiguration ;
import org.apache.commons.configuration2.SubnodeConfiguration ;
import org.apache.commons.configuration2.builder.fluent.Configurations ;
import org.apache.commons.configuration2.ex.ConfigurationException ;

import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.Names ;

public abstract class Colonne
{
  public static final String COLUMN_FILE_EXTENTION = "cln";
  
  protected final Path _fichierColonne ;
  protected final SubnodeConfiguration _colSection;
  
  private final TypeColonne _type;
  
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
  
  public Colonne(Path cheminFichierColonne, TypeColonne type) throws InitializationException
  {         
    // attention dans le fichier init la virgule des réels est : .
    
    this._fichierColonne = cheminFichierColonne ;
    this._type = type;
    
    if (! (Files.isReadable(this._fichierColonne) && Files.isRegularFile(this._fichierColonne)))
    {
      String msg = String.format("cannot read column configuration file '%s'", cheminFichierColonne);
      throw new InitializationException(msg);
    }
    
    Configurations configs = new Configurations();
    try
    {
      INIConfiguration iniConf = configs.ini(this._fichierColonne.toFile());
      this._colSection = iniConf.getSection(Names.SEC_INFO_COL);
      this._hauteurColonne           = this._colSection.getDouble(Names.SICOL_CLEF_H_COLONNE, -1.);
      this._hauteurMenisque          = this._colSection.getDouble(Names.SICOL_CLEF_H_MENISQUE, -1.);
      this._pousseSeringueDebitMin   = this._colSection.getDouble(Names.SICOL_CLEF_COl_DEBIT_MIN, -1.) ;
      this._pousseSeringueDebitInter = this._colSection.getDouble(Names.SICOL_CLEF_COL_DEBIT_INTER, -1.) ;
      this._pousseSeringueDebitMax   = this._colSection.getDouble(Names.SICOL_CLEF_COL_DEBIT_MAX, -1.) ;
      this._volumeCritique1          = this._colSection.getDouble(Names.SICOL_CLEF_VOL_CRITIQUE_1, -1.) ;
      this._volumeCritique2          = this._colSection.getDouble(Names.SICOL_CLEF_VOL_CRITIQUE_2, -1.) ;
    }
    catch (ConfigurationException e)
    {
      String msg = String.format("unable to read the column specifications in the file '%s'",
          this._fichierColonne.toString());
      throw new RuntimeException(msg,e);
    }
    
    if (this._hauteurColonne           < 0. ||
        this._hauteurMenisque          < 0. ||
        this._pousseSeringueDebitMin   < 0. ||
        this._pousseSeringueDebitInter < 0. ||
        this._pousseSeringueDebitMax   < 0. ||
        this._volumeCritique1          < 0. ||
        this._volumeCritique2          < 0.    )
    {
      String msg = String.format("corrupted column metadata file '%s'", cheminFichierColonne);
      throw new InitializationException(msg);
    }
  }
  
  public TypeColonne getType()
  {
    return this._type;
  }
  
  void close()
  {
    this._colSection.close();
  }
  
  public Path getColumnFilePath()
  {
    return this._fichierColonne;
  }
  
  public static Colonne getInstance(String filePath) throws InitializationException
  {
    Path columnPath = Paths.get(filePath);
    return getInstance(columnPath);
  }
  
  public static Colonne getInstance(Path columnPath) throws InitializationException
  {
    if (! (Files.isReadable(columnPath) &&
        Files.isRegularFile(columnPath)))
    {
      String msg = String.format("cannot read column configuration file '%s'", columnPath);
      throw new InitializationException(msg);
    }
    
    Configurations configs = new Configurations();
    TypeColonne type = null;
    SubnodeConfiguration section = null;
    try
    {
      INIConfiguration iniConf = configs.ini(columnPath.toFile());
      section = iniConf.getSection(Names.SEC_INFO_COL);
      int typeValue = section.getInteger(Names.SICOL_CLEF_TYPE, 0) ;
      type = TypeColonne.values()[typeValue];
    }
    catch (ConfigurationException e)
    {
      String msg = String.format("unable to read the column specifications in the file '%s'",
          columnPath);
      throw new InitializationException(msg,e);
    }
    finally
    {
      if(section != null)
      {
        section.close();
      }
    }
    
    Colonne colonne = null;
    switch (type)
    { 
      case CYLINDRE : { colonne = new CCylindre (columnPath) ; break ; }
      case CONE :     { colonne = new CCone (columnPath) ; break ; }
      case HYBRIDE :  { colonne = new CHybride (columnPath) ; break ; }
    }
    
    return colonne;
  }
}
