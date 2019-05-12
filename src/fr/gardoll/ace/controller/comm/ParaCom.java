package fr.gardoll.ace.controller.comm;

import java.io.Closeable ;

import fr.gardoll.ace.controller.common.ParaComException ;

public interface ParaCom  extends Closeable
{
  public String getId();
  
  // Open a specific isolation valve (zero based number).
  // Openning with the id zero, will close all the isolation valve.
  public void ouvrir(int numEv)  throws ParaComException, InterruptedException ;

  // ouvre l'EV associé à l'eau
  public void ouvrirH2O()  throws ParaComException, InterruptedException ;

  // ferme toutes les ev
  public void toutFermer()  throws ParaComException, InterruptedException ;
  
  @Override
  public void close() ;

  public final int NUM_SHUT_IV = 0;
  
  // numéro de l'électrovanne de l'eau
  public final int NUM_EV_H2O = 1 ;

  // refoulement vers le carrousel
  public final int NUM_EV_REFOULEMENT = 7 ;

  // nombre d'électrovannes possible
  public final int NB_EV_MAX = 7 ;
}
