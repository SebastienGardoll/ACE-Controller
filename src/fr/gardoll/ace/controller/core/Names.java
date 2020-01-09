package fr.gardoll.ace.controller.core;

import java.nio.file.Path ;

public class Names
{
  private Names() {}
  
  public static Path computeProtocolFilePath(String protocolFileName)
  {
    Path rootDir = Utils.getInstance().getRootDir();
    Path protocolFilePath = rootDir.resolve(Names.CONFIG_DIRNAME)
                                   .resolve(Names.PROTOCOL_DIRNAME)
                                   .resolve(protocolFileName);
    return protocolFilePath;
  }
  
  public static Path computeColumnFilePath(String columnFileName)
  {
    Path rootDir = Utils.getInstance().getRootDir();
    Path filePath = rootDir.resolve(Names.CONFIG_DIRNAME)
                           .resolve(Names.COLUMN_DIRNAME)
                           .resolve(columnFileName);
    return filePath;
  }
  
  public static Path computeGeneralSettingsFilePath()
  {
    Path rootDir = Utils.getInstance().getRootDir();
    Path filePath = rootDir.resolve(Names.CONFIG_DIRNAME)
                           .resolve(Names.CONFIG_FILENAME);
    
    return filePath;
  }
  
  public final static Path COLUMN_DIR_PATH = 
      Utils.getInstance().getRootDir().toAbsolutePath().resolve(Names.CONFIG_DIRNAME).resolve(Names.COLUMN_DIRNAME);
  
  public final static Path PROTOCOL_DIR_PATH = 
      Utils.getInstance().getRootDir().toAbsolutePath().resolve(Names.CONFIG_DIRNAME).resolve(Names.PROTOCOL_DIRNAME);
  
  public static final String CONFIG_DIRNAME    = "conf";
  public static final String CONFIG_FILENAME   = "configuration.ini";
  
  public static final String LOG_DIRNAME       = "log"; 
  public static final String PROTOCOL_DIRNAME  = "protocoles";
  public static final String COLUMN_DIRNAME    = "columns";
  public static final String CAROUSEL_DIRNAME  = "carousel";
  
  public static final String TRUE  = "true";
  public static final String FALSE = "false";
  
  //*********************** Fichier configuration  ************************** //

  // COMMUN

  public static final String CLEF_DATE                         = "date";

  //----------------------------
  
  public static final String SEC_ACE_CONTROLLER                = "ace_controller_settings";
  public static final String SAC_IS_DEBUG                      = "is_debug";
  public static final String SAC_IS_FULL_SCREEN                = "is_fullScreen";
  
  //----------------------------

  public static final String SEC_INFO_POUSSE_SERINGUE          = "informationsPousseSeringue";

  public static final String SIPS_CLEF_SERIAL_COM_CLASS_PATH   = "serialComClassPath";
  
  public static final String SIPS_CLEF_PORT_PATH               = "portPath";
  
  public static final String SIPS_CLEF_VOL_MAX                 = "volumeMax";

  public static final String SIPS_CLEF_VOL_RINCAGE             = "volumeRincage";

  public static final String SIPS_CLEF_NB_RINCAGE              = "nombreRincage";

  public static final String SIPS_CLEF_DEBIT_MAX               = "debitMax";

  public static final String SIPS_CLEF_NB_SERINGUE             = "nombreSeringue";

  public static final String SIPS_CLEF_DIA_SERINGUE            = "diametreSeringue";

  //----------------------------

  public static final String SEC_INFO_CARROUSEL                = "informationsCarrousel";

  public static final String SIC_CLEF_SERIAL_COM_CLASS_PATH    = "serialComClassPath";
  
  public static final String SIC_CLEF_PORT_PATH                = "portPath";
  
  public static final String SIC_CLEF_CHEMIN_FICHIER_CARROUSEL = "cheminFichierCarrousel";

  public static final String SIC_CLEF_NOM_CARROUSEL            = "nomCarrousel";

  public static final String SIC_CLEF_DIA                      = "diametre";

  public static final String SIC_CLEF_NB_COL                   = "nombreColonne";

  public static final String SIC_CLEF_REF_CARROUSEL            = "referenceCarrousel";

  public static final String SIC_CLEF_EPAISSEUR                = "epaisseur";

  public static final String SIC_CLEF_NB_DEMI_PAS              = "nombreDemiPas";

  public static final String SIC_CLEF_AJUSTEMENT               = "ajuste?";

  //----------------------------

  public static final String SEC_INFO_COL                      = "informationsColonne";

  public static final String SICOL_NOM_COL                     = "nomColonne";

  public static final String SICOL_CLEF_H_COLONNE              = "hauteurColonne";

  public static final String SICOL_CLEF_H_MENISQUE             = "hauteurMenisque";

  public static final String SICOL_CLEF_H_CYLINDRE             = "hauteurCylindre";

  public static final String SICOL_CLEF_DIA                    = "diametre";

  public static final String SICOL_CLEF_COL_DEBIT_MAX          = "pousseSeringueDebitMax";

  public static final String SICOL_CLEF_COL_DEBIT_INTER        = "pousseSeringueDebitInter";

  public static final String SICOL_CLEF_COl_DEBIT_MIN          = "pousseSeringueDebitMin";

  public static final String SICOL_CLEF_VOL_CRITIQUE_1         = "volumeClef1";

  public static final String SICOL_CLEF_VOL_CRITIQUE_2         = "volumeClef2";

  public static final String SICOL_CLEF_DIA_SUP                = "diametreSup";

  public static final String SICOL_CLEF_DIA_INF                = "diametreInf";

  public static final String SICOL_CLEF_TYPE                   = "type";

  public static final String SICOL_CLEF_H_CONE                 = "hauteurCone";

  //----------------------------

  public static final String SEC_INFO_PROTOCOLE                = "informationsProtocole";

  public static final String SIP_CLEF_NOM_PROTO                = "nomProtocole";

  public static final String SIP_CLEF_AUTEUR                   = "auteur";

  public static final String SIP_CLEF_CHEMIN_FICHIER_COL       = "cheminFichierColonne";

  public static final String SIP_CLEF_COMMENTAIRES             = "commentaires";

  public static final String SIP_CLEF_ACIDE                    = "acide";

  public static final String SIP_CLEF_NUM_EV                   = "numeroEv";

  public static final String SIP_CLEF_VOL                      = "volume";

  public static final String SIP_CLEF_TEMPS                    = "temps";

  public static final String SIP_CLEF_PAUSE                    = "pause?";

  //----------------------------

  public static final String SEC_INFO_PARA_COM                 = "informationsParacom";

  public static final String SIPC_CLEF_PARA_COM_CLASS_PATH     = "paraComClassPath";
  
  public static final String SIPC_CLEF_SERIAL_COM_CLASS_PATH   = "serialComClassPath";
  
  public static final String SIPC_CLEF_PORT_PATH               = "portPath";
}
