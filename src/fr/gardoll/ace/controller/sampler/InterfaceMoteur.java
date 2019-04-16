package fr.gardoll.ace.controller.sampler;

import java.io.Closeable;
import java.io.IOException;

import fr.gardoll.ace.controller.comm.SerialCom;

public class InterfaceMoteur implements Closeable
{
  private final SerialCom _port;
  
  public InterfaceMoteur(SerialCom port)
  {
    this._port = port ;
  }
  
  @Override
  public void close() throws IOException
  {
    this._port.close() ;
  }
  
  public static void main(String[] args)
  {
    // TODO Auto-generated method stub

  }
}

/*
  
  const char  NB_BITS = 7 ; //nombre de signaux en sortis de l'interface
  
  InterfaceMoteur::InterfaceMoteur ( const AnsiString & numPort ) : port(numPort)

  {    semaphore = new TCriticalSection ();

     port.setVitesse ( 9600 ) ;
     port.setByteSize (8);
     port.setStopBit (ONESTOPBIT);
     port.setParite ( NOPARITY );
     port.setControlFlux();
     port.setTimeOut ( 100 ) ;

     DecimalSeparator = '.';
  }

//traitement de la réponse de l'interface
  //en cas d'erreur => exception
  void InterfaceMoteur::traitementReponse (const AnsiString & reponse )

  {                     //travail sur Erangeerror et ne pas oublier const puis maj dans les autre directory
  try {
  switch ( reponse [1] )

  {  case '0' : { break ; } // commande bien exécutée par l'interface

     case '1' : { break ; } // toujours en mouvement pour la fonction moving


     case 'E' : { throw EInterfaceMoteur ( IM_ERREUR_RECEPTION_1 + reponse ) ;  
                  break ; }

     //case '-' : { throw EInterfaceMoteur (IM_ERREUR_RECEPTION_2) ; break ;}  //attention incompatibilité avec where et position négative !!!

     case 0   : { throw EInterfaceMoteur ( IM_ERREUR_RECEPTION_3 ) ; break ; }

  }

  } catch ( const ERangeError & e ) { throw EInterfaceMoteur (IM_ERREUR_RECEPTION_3);}
              // si la réponse est une chaine vide alors il y a levé d'une exception ERangeError.
  }

//renvoie la réponse de l'interface
  //en milisecondes
  AnsiString InterfaceMoteur::traitementOrdre ( const AnsiString & ordre, int temporisation )
                                           //ok testé le 20/08/04
  {  AnsiString  reponse = "";

   try { semaphore->Acquire();    //évite l'appel concurrent de cette méthode par plusieurs thread différents

        port.ecrire( ordre ) ;

        Sleep ( temporisation );

        reponse = lectureReponse() ;

        traitementReponse ( reponse ) ;
      }

  __finally { semaphore->Release(); }

  return reponse  ;
  }

  //---------------------------------------------------------------------------

  AnsiString InterfaceMoteur::lectureReponse ()

  {  AnsiString message_brute ;

   message_brute = port.lire () ;

   AnsiString message_renvoye = "";

   for ( int i = 1 ; i <= message_brute.Length() ; i++ )

   {  if ( message_brute [i] != '#' ) message_renvoye += message_brute [i] ; }

   return ( message_renvoye );
  }

  //---------------------------------------------------------------------------

  void InterfaceMoteur::move ( int nbPas1 , int nbPas2 )

  {  AnsiString ordre = "move (" + IntToStr (nbPas1) + "," + IntToStr (nbPas2) + ")\r" ;
   traitementOrdre ( ordre ) ;
  }

  //détection fin de mouvement
  bool InterfaceMoteur::moving (TypeAxe axe )  //modifié et testé 19/08/04

  {  AnsiString ordre = "moving (" + IntToStr (axe) + ")\r" ;

   return  ( traitementOrdre ( ordre )[1] == '1' ) ; // valeur 1 ==> en mouvement
  }

//avance jusqu'à fin de butée
  // 0 :pas bougé, 1 : butée positive, -1 : butée négative
  void InterfaceMoteur::movel ( char axe1, char axe2 )

  {  
   AnsiString ordre = "movel (" + IntToStr(axe1) + "," + IntToStr(axe2) + ")\r" ;
   traitementOrdre ( ordre ) ;
  }


//réinitialisation de l'interface
  void InterfaceMoteur::reset ()

  {   port.ecrire( "new\r" ) ;

    Sleep (800); // attente obligatoire à cause de la lenteur de l'interface
               //  pour cette fonction . 800 ms

    lectureReponse ();

     ATTENTION PAS DE VERIFICATION REPONSE !!! 
  }

  //---------------------------------------------------------------------------

  void InterfaceMoteur::preSecale ( int denominateur)

  {  AnsiString ordre = "prescale (" + IntToStr ( denominateur ) + ")\r" ;
   traitementOrdre ( ordre ) ;
  }

  //---------------------------------------------------------------------------

  void InterfaceMoteur::param ( TypeAxe axe, int base, int top, int accel, int deaccel )

  {  AnsiString ordre ;

   if ( deaccel == 0 )

   { ordre = "param (" + IntToStr ( axe ) + "," + IntToStr ( base ) + "," + IntToStr ( top ) + "," +IntToStr ( accel )  +  ")\r" ;

   }

   else { ordre = "param (" + IntToStr ( axe ) + "," + IntToStr ( base ) + "," + IntToStr ( top ) + "," +IntToStr ( accel )  + "," + IntToStr ( deaccel )  + ")\r" ;

   }
   
  traitementOrdre ( ordre, 2000 ) ; //long traitement par l'interface

  }

  //---------------------------------------------------------------------------

  void InterfaceMoteur::datum ( TypeAxe axe)

  {  AnsiString ordre = "datum (" + IntToStr (axe)  + ")\r" ;
   traitementOrdre ( ordre ) ;
  }

  //---------------------------------------------------------------------------

  void InterfaceMoteur::singleLine ( bool choix)

  {  AnsiString ordre = "singleline (" + BoolToStr (choix) + ")\r" ;
   traitementOrdre ( ordre ) ;
  }

//précondition : le threadSequence doit être détruit ( threadterminate) ou inexistant
  void InterfaceMoteur::stop ()  //ok testé 19/08/04

  { //précondition : les  thread utilisant l'interface doivent être détruits  ou inexistant

  port.ecrire( "stop ()\r" ) ;    //   Ne pas passer par la zone critique !!!! cad le semaphore

  AnsiString & reponse = lectureReponse() ;

  traitementReponse ( reponse ) ;

  delete semaphore ;

  semaphore = new TCriticalSection ();//permet la libération du sémaphore au cas où les thread
                     //  l'auraient acquis pour permettre dans ce cas l'utilisation des autres méthodes.
  }

  //---------------------------------------------------------------------------

  void InterfaceMoteur::manual ()

  {  traitementOrdre ( "manual ()\r" ) ;
  }

  //---------------------------------------------------------------------------

  void InterfaceMoteur::halt()    //ok testé 19/08/04

  { traitementOrdre ( "halt()\r" , 1500 ) ; } //la temporisation dépend directement
                                           //de la vitesse de point et de la déceleration
  //---------------------------------------------------------------------------

  int InterfaceMoteur::where(TypeAxe axe)

  {  AnsiString ordre = "where (" + IntToStr (axe) + ")\r" ;
   return StrToInt ( traitementOrdre ( ordre, 100 ) ) ;
  }

  //---------------------------------------------------------------------------

  void InterfaceMoteur::lock ()

  { semaphore->Acquire() ; }

  //---------------------------------------------------------------------------

  void InterfaceMoteur::unLock ()

  { semaphore->Release() ; }

//voir manuel de l'interface
  void InterfaceMoteur::out ( char octet )

  { AnsiString ordre = "out(" + IntToStr ( octet ) + ")\r" ;

  traitementOrdre( ordre );
  }

//voir manuel de l'interface
  void InterfaceMoteur::out ( char bit, bool valeur )

  { if  ( bit > NB_BITS ) throw EInterfaceMoteur (IM_ERREUR_BIT);

  AnsiString ordre = "out(" + IntToStr ( bit ) + "," + BoolToStr ( valeur ) + ")\r" ;

  traitementOrdre( ordre );
  }

*/