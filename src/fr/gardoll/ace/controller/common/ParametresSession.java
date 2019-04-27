package fr.gardoll.ace.controller.common;

import java.io.File ;
import java.net.URISyntaxException ;
import java.net.URL ;
import java.nio.file.Path ;
import java.text.DecimalFormatSymbols ;
import java.util.Locale ;

import org.apache.commons.configuration2.INIConfiguration ;
import org.apache.commons.configuration2.builder.fluent.Configurations ;
import org.apache.commons.configuration2.ex.ConfigurationException ;
import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.column.Colonne ;
import fr.gardoll.ace.controller.pump.PousseSeringue ;
import fr.gardoll.ace.controller.sampler.Passeur ;

// TODO: add logging
public class ParametresSession
{
  private static final Logger _LOG = LogManager.getLogger(ParametresSession.class.getName());
  
  public final static DecimalFormatSymbols DECIMAL_SYMBOLS =
      new DecimalFormatSymbols(Locale.FRANCE);
  
  public final static int NB_POSITION = 6 ;
  
  public static final ParametresSession INSTANCE = new ParametresSession();
  
  private PousseSeringue _ps = null;
  private Passeur _sampler = null;
  
  private ParametresSession()
  {
    Path rootDir = null ;
    try
    {
      rootDir = Utils.getRootDir(this);
    }
    catch (URISyntaxException e)
    {
      String msg = String.format("unable to compute the path of the application: %s", e.getMessage());
      _LOG.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
    
    Path configurationFile = rootDir.resolve(Names.CONFIG_FILENAME);
    
    try
    {
      Configurations configs = new Configurations();
      INIConfiguration iniConf = configs.ini(configurationFile.toFile());
      
      //this. = iniConf.getSection(Names.);
      
      /*

      this->_volumeMaxSeringue = fichierConf->ReadFloat ( SEC_INFO_POUSSE_SERINGUE, SIPS_CLEF_VOL_MAX, -1.0 ) ;
      this->_volumeRincage = fichierConf->ReadFloat ( SEC_INFO_POUSSE_SERINGUE, SIPS_CLEF_VOL_RINCAGE, -1.0 ) ;
      this->_nbRincage = fichierConf->ReadInteger ( SEC_INFO_POUSSE_SERINGUE, SIPS_CLEF_NB_RINCAGE, -1 ) ;
      this->_debitMaxPousseSeringue = fichierConf->ReadFloat ( SEC_INFO_POUSSE_SERINGUE, SIPS_CLEF_DEBIT_MAX, -1 ) ;
      this->_nbSeringue = fichierConf->ReadInteger(SEC_INFO_POUSSE_SERINGUE, SIPS_CLEF_NB_SERINGUE,-1);
      this->_diametreSeringue = fichierConf->ReadFloat(SEC_INFO_POUSSE_SERINGUE, SIPS_CLEF_DIA_SERINGUE,-1.0);

      if ( FileExists ( fichierConf->ReadString (SEC_INFO_CARROUSEL, SIC_CLEF_CHEMIN_FICHIER_CARROUSEL, C_ERREUR_LECTURE ) ) )

      { TIniFile * fichierCarrousel = new TIniFile ( fichierConf->ReadString (SEC_INFO_CARROUSEL, SIC_CLEF_CHEMIN_FICHIER_CARROUSEL, C_ERREUR_LECTURE ) ) ;

        this->_nbPasCarrousel = fichierCarrousel->ReadInteger (SEC_INFO_CARROUSEL, SIC_CLEF_NB_DEMI_PAS, -1) ;
        this->_refCarrousel = fichierCarrousel->ReadInteger ( SEC_INFO_CARROUSEL, SIC_CLEF_REF_CARROUSEL, -1);
        this->_diametreCarrousel = fichierCarrousel->ReadInteger ( SEC_INFO_CARROUSEL, SIC_CLEF_DIA, -1);
        this->_epaisseur = fichierCarrousel->ReadInteger ( SEC_INFO_CARROUSEL, SIC_CLEF_EPAISSEUR, -1);
        this->_nbMaxColonne = fichierCarrousel->ReadInteger(SEC_INFO_CARROUSEL, SIC_CLEF_NB_COL , -1) ;
        delete fichierCarrousel ;
      }

      else {  throw Exception (PSS_ERREUR_FICHIER_CAR + fichierConf->ReadString (SEC_INFO_CARROUSEL, SIC_CLEF_CHEMIN_FICHIER_CARROUSEL, C_ERREUR_LECTURE ) + C_ERREUR_OUVERTURE );  }


      if ( _volumeMaxSeringue      < 0 ||
           _volumeRincage          < 0 ||
           _nbRincage              < 0 ||
           _debitMaxPousseSeringue < 0 ||
           _nbSeringue             < 0 ||
           _diametreSeringue       < 0 ||
           _nbPasCarrousel         < 0 ||
           _refCarrousel           < 0 ||
           _diametreCarrousel      < 0 ||
           _epaisseur              < 0 ||
           _nbMaxColonne           < 0    ) throw Exception(INIT_ERREUR_LECTURE);



      delete fichierConf ;

   */
    }
    catch (ConfigurationException e)
    {
      String msg = String.format("unable to read the configuration file '%s': %s",
          configurationFile.toString(), e.getMessage());
      _LOG.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public PousseSeringue getPousseSeringue()
  {
    // TODO Auto-generated method stub
    return null ;
  }

  public Passeur getPasseur()
  {
    // TODO Auto-generated method stub
    return null ;
  }

  public Colonne getColonne()
  {
    // TODO Auto-generated method stub
    return null ;
  }

  public double debitMaxPousseSeringue()
  {
    // TODO Auto-generated method stub
    return 0 ;
  }

  public int nbRincage()
  {
    // TODO Auto-generated method stub
    return 0 ;
  }

  public double volumeRincage()
  {
    // TODO Auto-generated method stub
    return 0 ;
  }

  public int epaisseur()
  {
    // TODO Auto-generated method stub
    return 0 ;
  }

  public int refCarrousel()
  {
    // TODO Auto-generated method stub
    return 0 ;
  }

}



/*

double _volumeMaxSeringue ;  // volume max du type de seringue en mL

             double _volumeRincage ;  // volume  utilisé pendant un cylce de rinçage  en mL

             int _nbPasCarrousel ;  // nombre de demi pas entre deux emplacement colonnes

             int  _refCarrousel; // distance butée haute du bras, plateau supérieur du carrousel en mm

             char _nbRincage ; //nombre de rinçage à effectuer au moment de changer d'éluant

             double _debitMaxPousseSeringue ;//débit maximum applicable au pousseSeringue en fonction du diamètre de tuyau utilisé !!!

             double _diametreSeringue ;//diametre du type de seringue utilisé

             char _nbSeringue ; //nombre de seringues utilisées

             int _diametreCarrousel ;//diamètre du carrousel en mm

             int _epaisseur ;//epaisseur du plateau sup du carrousel

             char _nbMaxColonne ; //nombre d'emplacement max de colonnes sur le carrousel choisi

*/

/*

               double  volumeMaxSeringue () const { return _volumeMaxSeringue ;};//* _nbSeringue ; } ; pour une seringue

               double  volumeRincage () const { return  _volumeRincage ; };

               int nbPasCarrousel () const { return _nbPasCarrousel ; };

               int refCarrousel () const { return _refCarrousel   ; } ;

               char nbRincage () const { return _nbRincage ; } ;

               double debitMaxPousseSeringue () const { return _debitMaxPousseSeringue ; };

               double diametreSeringue () const { return _diametreSeringue ; };

               char nbSeringue () const { return _nbSeringue ; } ;

               int diametreCarrousel () const { return _diametreCarrousel ; };

               int epaisseur () const { return _epaisseur ; } ;

               char nbMaxColonne () const { return _nbMaxColonne ; } ;


*/

/*

  : pousseSeringue (parametresSession.nbSeringue() , parametresSession.diametreSeringue(),
                    parametresSession.volumeMaxSeringue() , parametresSession.debitMaxPousseSeringue() )  ,

    passeur (parametresSession.nbPasCarrousel(), parametresSession.diametreCarrousel())


*/