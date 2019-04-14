package fr.gardoll.ace.controller.comm;

public interface ParaCom
{
  // Open a specific isolation valve (zero based number).
  public void ouvrir(int numEv) ; 

  // ouvre l'EV associé à l'eau
  public void ouvrirH2O() ;

  // ferme toutes les ev
  public void toutFermer() ;

  // en milisecondes
  public final int ATTENTE_EV = 200 ;

  // numéro de l'électrovanne de l'eau
  public final int NUM_EV_H2O = 1 ;

  // refoulement vers le carrousel
  public final int NUMEV_REFOULEMENT = 7 ;

  // nombre d'électrovannes possible
  public final int NB_EV_MAX = 7 ;
}
