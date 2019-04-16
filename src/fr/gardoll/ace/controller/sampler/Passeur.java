package fr.gardoll.ace.controller.sampler;

import java.io.Closeable ;
import java.io.IOException ;

public class Passeur implements Closeable
{
  private final InterfaceMoteur interfaceMoteur;
  
  public Passeur(InterfaceMoteur interfaceMoteur)
  {
    this.interfaceMoteur = interfaceMoteur;
  }
  
  @Override
  public void close() throws IOException
  {
    this.interfaceMoteur.close();
  }
  
  public static void main(String[] args)
  {
    // TODO Auto-generated method stub

  }
}

/*

  private :    //attributs

    int x ; //nombre de demi pas pour l'axe carrousel

    int y ; //nombre de demi pas pour l'axe bras

    int sav_x ; //position du carrousel enregistrée   //est recalculé si appel setOrigineXX

    int sav_y ; //position du bras enregistrée  //est recalculé si appel setOrigineXX

    bool sav_butee ;//flag de butee enregistré

    InterfaceMoteur * interfaceMoteur ;

    int nbPasCarrousel ;

    double rayon ;

    bool _pause ;

    bool _butee ; //ce flag sert en cas de pause à savoir si le bras devait
                  //s'arrêter à une butée ( donc pas de x ou y précisé ) ou non


  bool isPaused () { return _pause; } ;
   
  const int RAPPORT_REDUCTEUR_MOTEUR = 40 ;//DEPENDANT DE LA MECANIQUE
  const int NB_PAS_TOUR_BRAS = 400 ; // en demi pas
  const int NB_PAS_TOUR_MOTEUR = 400 ;//demi pas sans réducteur
  const int NB_PAS_TOUR_CARROUSEL = RAPPORT_REDUCTEUR_MOTEUR * NB_PAS_TOUR_MOTEUR  ; //demi pas
  const int HAUTEUR_TOUR_BRAS = 3 ; // en mm

  const char BIT = 2 ; //numéro du signal utilisé pour faire vibrer le bras
  const int VIBRATION_TEMPS = 500 ; //temps en ms de vibration


  //---------------------------------------------------------------------------

  /* ne pas inclure d'attente de fin de mouvement dans les procédures de mouvement
     car incompatible avec le système de pause 



  // Les arguments donnée aux méthodes ne sont pas vérifiés car grâce aux butée de
  // fin de course, la sécurité est assurée.

//require nbPasCarrousel > 0
  //require diametre > 0
  Passeur::Passeur (int nbPasCarrousel, int diametre)

  {
     AttributionPortSerie attrib ;

     interfaceMoteur = new InterfaceMoteur( "\\\\.\\" + attrib.getAvantDernierNumPort() );

     this->reset() ;
     this->setModeDirect() ;

   //  interfaceMoteur.param(bras, 1000, 1150, 100);    modif du au 2 guides

     x = 0 ; y = 0 ;

     sav_x = 0 ; sav_y = 0 ;
     sav_butee = false ;

     _pause = false ;
     _butee = false ;


     if (  nbPasCarrousel <= 0 || diametre  <= 0 ) throw EPasseur (PASS_ERREUR_NB_PAS_CARROUSEL);

     else {  this->nbPasCarrousel = nbPasCarrousel ;
             this->rayon = diametre / 2.  ;
          }
  }


//le numéro de la position. 0 => poubelle
  //modificateur => ajout d'un nombre de demi pas
  void Passeur::moveCarrousel ( int position, int modificateur)
                                              //modificateur ou en nombre de pas si position = 0
  {
      x = position * nbPasCarrousel + modificateur ;
      interfaceMoteur->move( x , y );
  }

//en nombre de demi pas
  void Passeur::moveBras ( int nbPas)

  {
     y = nbPas ;
     interfaceMoteur->move(x, y);
  }

//effectue un mouvement simultané
  //fait car problème de temps de réponse car halt fait par interface
  void Passeur::moveCarrouselEtBras ( int position, int nbPas )   //ok testé le 20/08/04

  {
      x = position * nbPasCarrousel ;
      y = nbPas ;
      interfaceMoteur->move( x , y );
  }

//le bras revient à son origine
  void Passeur::moveOrigineBras ()

  {
     interfaceMoteur->move(x, 0);
     y = 0 ;
  }

//avance le bras jusqu'à la butée haute sans setOrigineBras
  void Passeur::moveButeBras ()

  {  _butee = true ;
     interfaceMoteur->movel(0,1);
  }

//attente de la fin de mouvement
  void Passeur::finMoveCarrousel ()

  {
    do

    { Sleep (100) ;
      //while ( _pause ) { Sleep (100); }
    }

    while ( interfaceMoteur->moving(carrousel)  ) ;


  }

//attente de la fin de mouvement
  void Passeur::finMoveBras ()

  {
    do

    { Sleep (100) ;
      //while ( _pause ) { Sleep (100); }
    }

    while ( interfaceMoteur->moving(bras)  ) ;

    if ( _butee ) //uniquement pour le bras, utile pour moveCarrouselRelatif

    {  y = interfaceMoteur->where(bras) ; //la position du bras en fin de butée est actualisée
       _butee = false ;               //si occurence d'une pause jusqu'à fin de move du bras

    }


  }


//arrêt des moteurs net avec perte position
  //précondition : le threadSequence doit être détruit ( pthread_cancel ) ou inexistant
  void Passeur::arretUrgence ()  //ok testé le 19/08/04

  { //précondition : les threads utilisant le passeur doivent être détruits ( threadTerminate ) ou inexistant
    interfaceMoteur->stop();
    setOrigine ();
  }

//envoie la commande new à l'interface.
  void Passeur::reset ()

  { interfaceMoteur->reset();
    x = 0 ; y = 0 ;
  }

//la position courante devient l'origine du bras <=> y = 0
  //sav_y est recalculé dans le nouveau référenciel
  void Passeur::setOrigineBras ()

  {  interfaceMoteur->datum(bras);
     sav_y = 0 - y + sav_y ;
     y = 0 ;
  }

//la position courante devient l'origine du carrousel <=> x = 0
  //sav_x est recalculé dans le nouveau référenciel
  void Passeur::setOrigineCarrousel ()

  {  interfaceMoteur->datum(carrousel);
     sav_x = 0 - x + sav_x ;
     x = 0 ;
  }

//sur les deux axes
  void Passeur::setOrigine ()

  { setOrigineBras () ;
    setOrigineCarrousel () ;
  }

//commande directe de l'interface, pas de
  //  mémorisation des commandes envoyées.
  void Passeur::setModeDirect ()

  {  interfaceMoteur->singleLine(true);
  }

//permet d'utiliser le potentiomètre numérique
  void Passeur::setModeManuel ()

  { interfaceMoteur->manual() ; }

//pause du passeur avec enregistrement de la position courante
  void Passeur::pause ()    //ok testé le 20/08/04

  {
    if ( interfaceMoteur->moving(carrousel) || interfaceMoteur->moving(bras) )

       { interfaceMoteur->halt(); }
       
    _pause = true ;
    saveCurrentPosition();
  }

//reprise sur pause avec retour à la position sauvegardée dans pause()
  void Passeur::reprise (bool brasFirst)     //ok testé le 20/08/04

  {  if ( _pause )

     {  if ( ( sav_x == x ) && ( sav_y == y ) && ( sav_butee == _butee ) )  //cas où il n'y a pas eu de manip sur le passeur

        {  if ( _butee ) moveButeBras() ;

           else interfaceMoteur->move(x, y) ; //ancien mécanisme de reprise quand la manip sur le passeur n'était pas possible en pause
        }

        else returnSavedPosition(brasFirst) ; 

        _pause = false ;
     }
  }

  //---------------------------------------------------------------------------

  int Passeur::rapportReducteur () { return RAPPORT_REDUCTEUR_MOTEUR ;}

  //---------------------------------------------------------------------------

  int Passeur::nbPasTourBras () { return NB_PAS_TOUR_BRAS ; }

  //---------------------------------------------------------------------------

  int Passeur::hauteurTourBras () { return HAUTEUR_TOUR_BRAS ; }


//converti une dimension exprimée en mm en nombre de demi pas pour le bras.
  //arrondi au demi pas supérieur
  int Passeur::convertBras( double dimension )  //dimension en mm en rapport avec le bras !

  {
    return ceil ( ( dimension * NB_PAS_TOUR_BRAS ) / HAUTEUR_TOUR_BRAS ) ;
  }

//converti une dimension exprimée en mm en nombre de demi pas pour le carrousel.
  //arrondi au demi pas inférieur
  int Passeur::convertCarrousel( double dimension )  //dimension en mm en rapport avec au carrousel !

  {
    return ( asin ( dimension / rayon ) *  NB_PAS_TOUR_CARROUSEL ) / ( 2 * M_PI ) ;
  }

  //---------------------------------------------------------------------------


  void Passeur::vibration ()    //ok, testé me 06/09/04

  {  interfaceMoteur->out( BIT, true );

     Sleep ( VIBRATION_TEMPS ) ;

     interfaceMoteur->out( BIT, false );
  }

//le carrousel revient à son point d'origine
  void Passeur::moveOrigineCarrousel ()

  {
     interfaceMoteur->move(0 , y);
     x = 0 ;
  }

//bouge de nbPosition
  //!= moveCarrousel où on précise le numéro de la position par rapport à 0 
  void Passeur::moveCarrouselRelatif ( int nbPosition )

  {
     x += nbPosition * nbPasCarrousel  ;

     interfaceMoteur->move( x , y );
  }

//enregistre la position du bras et du carrousel
  //dans les variables save_x et save_y
  //est recalculé si appel setOrigineXX mais attention à son utilité par la suite
  //appel obligatoire si l'on doit manipuler le passeur pendant une pause
  void Passeur::saveCurrentPosition ()

  {  sav_x = x ; sav_y = y ; sav_butee = _butee ; }

//revient à la position enregistrée par saveCurrentPosition
  //détermine si le bras bouge avant le carrousel
                      //appel obligatoire si l'on doit manipuler le passeur pendant une pause
  void Passeur::returnSavedPosition (bool brasFirst)

  {

    
        if ( brasFirst )

        {  if ( sav_butee ) moveButeBras() ; //sav_y est inutile et dangereux en cas de moveButeBras
           else  moveBras ( sav_y ) ;
           finMoveBras();
           moveCarrousel ( 0 , sav_x ) ;
           finMoveCarrousel();
        }

        else {  moveCarrousel ( 0 , sav_x ) ;
                finMoveCarrousel();
                if ( sav_butee ) moveButeBras() ; //sav_y est inutile et dangereux en cas de moveButeBras
                else  moveBras ( sav_y ) ;
                finMoveBras();
             }

  }

  //---------------------------------------------------------------------------

*/