package fr.gardoll.ace.controller.core;

public class Names
{
  private Names() {}
  
  public static final String CONFIG_FILENAME   = "configuration.ini";
  
  public static final String LOG_DIRNAME       = "journaux"; 
  
  public static final String PROTOCOLE_DIRNAME = "protocoles";
  
  public static final String COLUMN_DIRNAME    = "colonnes";
  
  public static final String PLATE_DIRNAME     = "carrousels";
  
  //*********************** Fichier configuration  ************************** //

  // COMMUN

  public static final String CLEF_DATE                         = "date";

  //----------------------------

  public static final String SEC_INFO_POUSSE_SERINGUE          = "informationsPousseSeringue";

  public static final String SIPS_CLEF_VOL_MAX                 = "volumeMax";

  public static final String SIPS_CLEF_VOL_RINCAGE             = "volumeRincage";

  public static final String SIPS_CLEF_NB_RINCAGE              = "nombreRincage";

  public static final String SIPS_CLEF_DEBIT_MAX               = "debitMax";

  public static final String SIPS_CLEF_NB_SERINGUE             = "nombreSeringue";

  public static final String SIPS_CLEF_DIA_SERINGUE            = "diametreSeringue";

  //----------------------------

  public static final String SEC_INFO_CARROUSEL                = "informationsCarrousel";

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
}
