package fr.gardoll.ace.controller.settings;

import java.nio.file.Path ;

import fr.gardoll.ace.controller.core.ConfigurationException ;
import fr.gardoll.ace.controller.core.InitializationException ;

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
  
  public static final int DEFAULT_MIN_NB_PAS_CARROUSEL = 0;
  
  public CarouselSettings(Path configurationFilePath) throws InitializationException
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
    // TODO
    // > 1
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
    // TODO
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
    // TODO
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
    // TODO
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
    if(nbPasCarrousel <= DEFAULT_MIN_NB_PAS_CARROUSEL)
    {
      String msg = "the number of carousel's steps must be greater than zero";
      throw new ConfigurationException(msg);
    }
    
    // TODO
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
