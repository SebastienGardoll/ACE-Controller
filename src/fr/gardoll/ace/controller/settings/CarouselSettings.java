package fr.gardoll.ace.controller.settings;

import java.nio.file.Path ;

// This class is not meant to be used outside of GeneralSettings.
// This class is meant to be replaced by a future Carousel class.
class CarouselSettings extends Settings
{
  private static final String SEC_INFO_CARROUSEL                = "informationsCarrousel";
  private static final String SIC_CLEF_NOM_CARROUSEL            = "nomCarrousel";
  private static final String SIC_CLEF_DIA                      = "diametre";
  private static final String SIC_CLEF_NB_COL                   = "nombreColonne";
  private static final String SIC_CLEF_REF_CARROUSEL            = "referenceCarrousel";
  private static final String SIC_CLEF_EPAISSEUR                = "epaisseur";
  private static final String SIC_CLEF_NB_DEMI_PAS              = "nombreDemiPas";
  private static final String SIC_CLEF_AJUSTEMENT               = "ajuste?";
  
  public static final int DEFAULT_MIN_NB_PAS_CARROUSEL = 1;
  public static final int DEFAULT_MIN_NB_MAX_COLUMN = 2 ;
  public static final int DEFAULT_MIN_THICKNESS = 1 ;
  public static final int DEFAULT_MIN_PLATE_DIAMETER = 1 ;
  public static final int DEFAULT_MIN_REF_CAROUSEL = 1 ;
  
  public CarouselSettings(Path configurationFilePath) throws ConfigurationException
  {
    super(configurationFilePath);
  }
  
  public void checkCarouselTweaked(boolean isTweaked) throws ConfigurationException
  {
    // Nothing to do.
  }
  
  public boolean isCarouselTweaked() throws ConfigurationException
  {
    int rawValue = (int) this.fetchValue(SEC_INFO_CARROUSEL,
                                         SIC_CLEF_AJUSTEMENT,
                                         Integer.class);
    return rawValue == 1;
  }
  
  public void setCarouselTweaked(boolean isTweaked) throws ConfigurationException
  {
    this.checkCarouselTweaked(isTweaked);
    
    int rawValue = (isTweaked)?1:0;
    
    this.setValue(SEC_INFO_CARROUSEL,
                  SIC_CLEF_AJUSTEMENT,
                  rawValue);
  }
  
  public void checkCarouselName(String name) throws ConfigurationException
  {
    if(name.isBlank())
    {
      String msg = "the carousel name is missing";
      throw new ConfigurationException(msg);
    }
  }
  
  public String getCarouselName() throws ConfigurationException
  {
    return (String) this.fetchValue(SEC_INFO_CARROUSEL,
                              SIC_CLEF_NOM_CARROUSEL,
                              String.class);
  }
  
  public void setCarouselName(String name) throws ConfigurationException
  {
    this.checkCarouselName(name);
    this.setValue(SEC_INFO_CARROUSEL,
                  SIC_CLEF_NOM_CARROUSEL,
                  name);
  }
  
  public void checkNbMaxColonne(int nbMaxColonne) throws ConfigurationException
  {
    if(nbMaxColonne < DEFAULT_MIN_NB_MAX_COLUMN)
    {
      String msg = String.format("the maximum number of column cannot be less than %s", DEFAULT_MIN_NB_MAX_COLUMN);
      throw new ConfigurationException(msg);
    }
  }
  
  public int getNbMaxColonne() throws ConfigurationException
  {
    int totalNbPosition = (int) this.fetchValue(SEC_INFO_CARROUSEL,
                                                SIC_CLEF_NB_COL,
                                                Integer.class);
    // Minus the trash that takes a column position.
    return (totalNbPosition-1);
  }
  
  public void setNbMaxColonne(int nbMaxColonne) throws ConfigurationException
  {
    this.checkNbMaxColonne(nbMaxColonne);
    this.setValue(SEC_INFO_CARROUSEL,
                  SIC_CLEF_NB_COL,
                  nbMaxColonne);
  }
  
  public void checkEpaisseur(int epaisseur) throws ConfigurationException
  {
    if(epaisseur < DEFAULT_MIN_THICKNESS)
    {
      String msg = String.format("the thickness of the carousel's plate cannot be less than %s", DEFAULT_MIN_THICKNESS);
      throw new ConfigurationException(msg);
    }
  }
  
  public int getEpaisseur() throws ConfigurationException
  {
    return (int) this.fetchValue(SEC_INFO_CARROUSEL,
                                 SIC_CLEF_EPAISSEUR,
                                 Integer.class);
  }
  
  public void setEpaisseur(int epaisseur) throws ConfigurationException
  {
    this.checkEpaisseur(epaisseur);
    this.setValue(SEC_INFO_CARROUSEL,
                  SIC_CLEF_EPAISSEUR,
                  epaisseur);
  }
  
  public void checkDiametreCarrousel(int diametreCarrousel) throws ConfigurationException
  {
    if(diametreCarrousel < DEFAULT_MIN_PLATE_DIAMETER)
    {
      String msg = String.format("the diameter of the carousel's plate cannot be less than %s", DEFAULT_MIN_PLATE_DIAMETER);
      throw new ConfigurationException(msg);
    }
  }
  
  public int getDiametreCarrousel() throws ConfigurationException
  {
    return (int) this.fetchValue(SEC_INFO_CARROUSEL,
                                 SIC_CLEF_DIA,
                                 Integer.class);
  }
  
  public void setDiametreCarrousel(int diametreCarrousel) throws ConfigurationException
  {
    this.checkDiametreCarrousel(diametreCarrousel);
    this.setValue(SEC_INFO_CARROUSEL,
                  SIC_CLEF_DIA,
                  diametreCarrousel);
  }
  
  public void checkRefCarrousel(int refCarrousel) throws ConfigurationException
  {
    if(refCarrousel < DEFAULT_MIN_REF_CAROUSEL)
    {
      String msg = String.format("the carousel's reference cannot be less than %s", DEFAULT_MIN_REF_CAROUSEL);
      throw new ConfigurationException(msg);
    }
  }
  
  public int getRefCarrousel() throws ConfigurationException
  {
    return (int) this.fetchValue(SEC_INFO_CARROUSEL,
                                 SIC_CLEF_REF_CARROUSEL,
                                 Integer.class);
  }
  
  public void setRefCarrousel(int refCarrousel) throws ConfigurationException
  {
    this.checkRefCarrousel(refCarrousel);
    this.setValue(SEC_INFO_CARROUSEL,
                  SIC_CLEF_REF_CARROUSEL,
                  refCarrousel);
  }
  
  public void checkNbPasCarrousel(int nbPasCarrousel) throws ConfigurationException
  {
    if(nbPasCarrousel < DEFAULT_MIN_NB_PAS_CARROUSEL)
    {
      String msg = String.format("the number of carousel's steps cannot be less than %s", DEFAULT_MIN_NB_PAS_CARROUSEL);
      throw new ConfigurationException(msg);
    }
  }
  
  public int getNbPasCarrousel() throws ConfigurationException
  {
    return (int) this.fetchValue(SEC_INFO_CARROUSEL,
                                 SIC_CLEF_NB_DEMI_PAS,
                                 Integer.class);
  }
  
  public void setNbPasCarrousel(int nbPasCarrousel) throws ConfigurationException
  {
    this.checkNbPasCarrousel(nbPasCarrousel);
    this.setValue(SEC_INFO_CARROUSEL,
                  SIC_CLEF_NB_DEMI_PAS,
                  nbPasCarrousel);
  }
}
