package fr.gardoll.ace.controller.comm;

import java.io.Closeable ;

import fr.gardoll.ace.controller.common.SerialComException ;

public interface ParaCom  extends Closeable
{
  // Open a specific isolation valve (zero based number).
  // Openning with the id zero, will close all the isolation valve.
  public void ouvrir(int numEv)  throws SerialComException ;

  // ouvre l'EV associé à l'eau
  public void ouvrirH2O()  throws SerialComException ;

  // ferme toutes les ev
  public void toutFermer()  throws SerialComException ;
  
  public void close() ;

  // en milisecondes
  public final int ATTENTE_EV = 200 ;

  // numéro de l'électrovanne de l'eau
  public final int NUM_EV_H2O = 1 ;

  // refoulement vers le carrousel
  public final int NUM_EV_REFOULEMENT = 7 ;

  // nombre d'électrovannes possible
  public final int NB_EV_MAX = 7 ;
}
