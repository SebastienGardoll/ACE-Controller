package fr.gardoll.ace.controller.core;

public class Commandes
{
  
  public static void main(String[] args)
  {
    // TODO Auto-generated method stub

  }
}

/*
  private :   //attributs

    const ParametresSession  parametresSession ;

    const Colonne  * colonne ;

    Passeur passeur ;

    PousseSeringue pousseSeringue ;
  
  //requires & colonne != NULL
    Commandes::Commandes (const Colonne & colonne )


    : pousseSeringue (parametresSession.nbSeringue() , parametresSession.diametreSeringue(),
                      parametresSession.volumeMaxSeringue() , parametresSession.debitMaxPousseSeringue() )  ,

      passeur (parametresSession.nbPasCarrousel(), parametresSession.diametreCarrousel())


   {
      this->colonne = & colonne ;
      pousseSeringue.setDebitAspiration(parametresSession.debitMaxPousseSeringue()); //aspiration est toujours au débit max
   }

   //---------------------------------------------------------------------------

   void Commandes::rincage ( char numEv )  // algo testé le 12/01/05

   { pousseSeringue.setDebitRefoulement(parametresSession.debitMaxPousseSeringue()); //le refoulement pour les rinçage se fait toujours
                                                                                     //au débit max
     pousseSeringue.vidange(); //perte du volume de sécurité

     pousseSeringue.finPompage();                                                   

     for ( int i = 0 ; i < parametresSession.nbRincage() ; i++ )

        {  pousseSeringue.rincageAspiration( parametresSession.volumeRincage(), numEv);  //appel aspiration dédié au rinçage à cause
                                           //ok pour deux seringue 07/10/05              //de la gestion du volume de sécu voir aussi vidange
           pousseSeringue.finPompage() ;

           pousseSeringue.vidange();

           pousseSeringue.finPompage() ;
        }


     passeur.vibration();

   }

   //---------------------------------------------------------------------------

   void Commandes::rincageH2O () // algo testé le 12/01/05

   { rincage ( pousseSeringue.numEvH2O() ) ;  }


   //---------------------------------------------------------------------------

   void Commandes::pause ( const OrganiseurThreadSequence * threadOrganiseur )

   {  pousseSeringue.lock(); //évite l'interblocage. attendant la fin de l'execution d'un ordre passé sur le port serie
      passeur.lock();        //concurrence entre l'utilisateur et le thread sequence ou thread organiseur

      TThread * threadSequence = threadOrganiseur->adresseThreadSequence();


      try {  threadOrganiseur->Suspend() ; } //le thread est pausé en dehors de la section critique des interfaces
      catch ( const Exception &e ) {;}      //en cas où le thread n'existe plus <=> soit au moment de sa création/destruction soit avant démarrage ou fin de session

      try {  threadSequence->Suspend() ; }
      catch ( const Exception &e ) {;}

      pousseSeringue.unLock(); //débloque les interfaces
      passeur.unLock();        //à partir de maintenant, plus de concurrence
             
      pousseSeringue.pause();    
      passeur.pause();
   }

   //---------------------------------------------------------------------------

   void Commandes::reprise ( const OrganiseurThreadSequence * threadOrganiseur )

   {  passeur.reprise(false); 

      pousseSeringue.reprise(); //attention la reprise du passeur avant celle du pousse seringue à
                                //cause de la manipulation eventuelle de celui ci

      TThread * threadSequence = threadOrganiseur->adresseThreadSequence();

      try {  threadSequence->Resume(); } catch ( const Exception &e ) {;}  //relance le thread Sequence s'il existe encore

      try {  threadOrganiseur->Resume(); } catch ( const Exception &e ) {;}
   }

   //---------------------------------------------------------------------------

   void Commandes::arretUrgence ()

   {

            try {   passeur.arretUrgence();
                    pousseSeringue.arretUrgence();
                }

                catch ( Exception &e ) { MessageDlg (e.Message + ", arret d'urgence impossible, veuillez actionner le bouton coup de poing", mtError, TMsgDlgButtons() << mbOK , 0 ) ;
                                       }



     //*************************************************************************
     //            + toutes opérations liées à l'arrêt d'urgence                
     //************************************************** requires & colonne != NULL***********************
     


   }

   //---------------------------------------------------------------------------

   void Commandes::deplacementPasseur ( char position, int modificateur )   //ok testé 18/08/04

   {  passeur.moveOrigineBras(); //le bras est juste au dessus du réservoir de la colonne
      passeur.finMoveBras() ;
      passeur.moveCarrousel( position, modificateur );
      passeur.finMoveCarrousel();
   }

   //---------------------------------------------------------------------------

   void Commandes::deplacementPasseurPoubelle () //le bras se retrouve dans la poubelle
                                                 //ok testé 19/08/04
   {
      int correction = 0 ;

      deplacementPasseur ( 0 );

      if ( colonne->hauteurReservoir() > parametresSession.epaisseur() )
      { correction = parametresSession.epaisseur() ; }

      passeur.moveBras(- passeur.convertBras(colonne->hauteurReservoir()+ colonne->hauteurColonne()- correction ) );
      passeur.finMoveBras();
   }

   //---------------------------------------------------------------------------

   void Commandes::referencementBras ()  //fixe l'origine du bras juste au dessus des colonne
   //ok testé 18/08/04
   {  passeur.moveButeBras();//sans setOrigineBras() inclus !
      passeur.finMoveBras() ;
      passeur.setOrigineBras();
      passeur.moveBras( passeur.convertBras ( colonne->hauteurColonne() + colonne->hauteurReservoir()  - parametresSession.refCarrousel()  ));
      passeur.finMoveBras() ;
      passeur.setOrigineBras();
   }

 //rempli la seringue de l'acide à quantité demandée
   //volume en mL                              //sans dépasser la limite de la capacité de la seringue
   //requires vol_demande > 0                  //fait en sorte que le vol ds la seringue soit : limite seringue utile
   //requires numEv <= pousseSeringue.nbEvMax()                                                 valeur demandée - vol restant
   void Commandes::remplissageSeringue ( double vol_demande , char numEv )  // algo testé le 12/01/05
                                                                                     //ok deux seringues 07/10/05
   {
      if ( vol_demande <= pousseSeringue.volumeRestant() ) return ;


      else  { if ( vol_demande + pousseSeringue.volumeRestant() > pousseSeringue.volumeMaxSeringueUtile())//parametresSession.volumeMaxSeringue()) ) //doit considéré le volume max utile !!
      
                 {  vol_demande = pousseSeringue.volumeMaxSeringueUtile() - pousseSeringue.volumeRestant();} //parametresSession.volumeMaxSeringue()) - pousseSeringue.volumeRestant(); }


              pousseSeringue.aspiration ( vol_demande, numEv ) ;

              pousseSeringue.finPompage() ;

            }
   }

 //opérations de distribution du volume de liquide

   //volume en mL
   //requires vol_deja_delivre >= 0
   //requires vol_delivre > 0
   void Commandes::algoDistribution ( double vol_delivre, double vol_deja_delivre )  //ok testé 19/08/04
                                                                                    //ok deux seringues 07/10/05
   {

     double vol_total = vol_delivre + vol_deja_delivre ;


     if ( vol_deja_delivre < colonne->volumeCritique1() )
        { pousseSeringue.setDebitRefoulement ( colonne->pousseSeringueDebitMin() ); }

     else if ( vol_deja_delivre < colonne->volumeCritique2() )
             { pousseSeringue.setDebitRefoulement ( colonne->pousseSeringueDebitInter() ); }

          else { pousseSeringue.setDebitRefoulement ( colonne->pousseSeringueDebitMax() ); }

     
     pousseSeringue.refoulement(vol_delivre, pousseSeringue.numEvRefoulement() );

     if (( vol_total > colonne->volumeCritique1() ) &&
         ( vol_deja_delivre < colonne->volumeCritique1() ) )

     {  while ( (pousseSeringue.volumeDelivre() + vol_deja_delivre)   < colonne->volumeCritique1() )
        { Sleep ( 100); }





        pousseSeringue.setDebitRefoulement ( colonne->pousseSeringueDebitInter() );
     }

     if (( vol_total > colonne->volumeCritique2() ) && ( vol_deja_delivre < colonne->volumeCritique2() ) )

     {  while ( (pousseSeringue.volumeDelivre() + vol_deja_delivre)   < colonne->volumeCritique2() )
        { Sleep ( 100 ) ; }



        pousseSeringue.setDebitRefoulement ( colonne->pousseSeringueDebitMax() );
     }

     pousseSeringue.finPompage() ;

   }

   //---------------------------------------------------------------------------

   void Commandes::distribution ( char numColonne,
                                  double volumeCible,
                                  char numEv,
                                  char nbColonneRestant,
                                  TF_Panneau * form )
                                                                        //ok testé 19/08/04
   {                                                                    //ok deux seringues 07/10/05
        double vol_deja_delivre = 0. ;

        double vol_delivre ;

        form->majActionActuelle(deplacement);

        deplacementPasseur ( numColonne , calculsDeplacement(volumeCible) ) ;

        int nbPasBrasAtteindre = calculsHauteur ( volumeCible ) + passeur.convertBras ( colonne->hauteurMenisque() - colonne->hauteurReservoir() );
        // calculs de nombre de pas à descendre cad hauteur max du liquide dans un réservoir cônique ou cylindrique

        passeur.moveBras(nbPasBrasAtteindre);

        passeur.finMoveBras();


        while ( ! IsZero ( volumeCible - vol_deja_delivre , EPSILON ) )    //évite volumeCible == 0
                                                                           //évite problème de comparaison de réels
        {
              if ( IsZero ( pousseSeringue.volumeRestant(), EPSILON ) )

                 {  form->majActionActuelle(remplissage);

                    if ( nbColonneRestant == 0 ) remplissageSeringue (volumeCible - vol_deja_delivre , numEv );
                    else remplissageSeringue ( ( nbColonneRestant + 1 ) * volumeCible - vol_deja_delivre, numEv ) ;
                 }

              else  {  form->majActionActuelle(distributionEluant);

                       if ( pousseSeringue.volumeRestant() < volumeCible - vol_deja_delivre )

                          {  vol_delivre = pousseSeringue.volumeRestant()  ;
                             algoDistribution ( vol_delivre, vol_deja_delivre ); // avec attente de fin de distribution
                             vol_deja_delivre += vol_delivre ;
                          }

                       else  {  vol_delivre = volumeCible - vol_deja_delivre ;
                                algoDistribution ( vol_delivre, vol_deja_delivre ) ;
                                vol_deja_delivre += vol_delivre ;
                             }
                    }

        }//fin du while

        passeur.vibration(); 
   }

 //revoye le nombre de pas à descendre dans la colonne pour le bras
   // volume en microLitre        en fonction du volume d'éluant donné
   //requires volume > 0
   int Commandes::calculsHauteur ( double volume ) const   //V doit être en mili litre !!!

   {  return  passeur.convertBras ( colonne->calculsHauteur(volume) ) ; }

   //---------------------------------------------------------------------------

   int Commandes::calculsDeplacement ( double volume ) const   //V doit être en mili litre !!!

   {  return  passeur.convertCarrousel ( colonne->calculsDeplacementCarrousel(volume) ) ; }

   //---------------------------------------------------------------------------

   void Commandes::finSession ()

   {  pousseSeringue.fermetureEv() ;
      passeur.moveButeBras();
      passeur.finMoveBras();
   }

   //---------------------------------------------------------------------------

   void Commandes::presentationPasseur ( char sens ) //ok 10/01/06

   {
     passeur.moveButeBras();
     passeur.finMoveBras();

     if ( sens >= 0 ) passeur.moveCarrouselRelatif(NB_POSITION) ; //par la droite
     else  passeur.moveCarrouselRelatif( -1 * NB_POSITION) ; //par la gauche

     passeur.finMoveCarrousel();
   }

*/