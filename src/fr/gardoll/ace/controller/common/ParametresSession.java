package fr.gardoll.ace.controller.common;

import java.text.DecimalFormatSymbols ;
import java.util.Locale ;

import fr.gardoll.ace.controller.column.Colonne ;
import fr.gardoll.ace.controller.pump.PousseSeringue ;
import fr.gardoll.ace.controller.sampler.Passeur ;

public class ParametresSession
{
  public final static DecimalFormatSymbols DECIMAL_SYMBOLS =
      new DecimalFormatSymbols(Locale.FRANCE);
  
  public final static int NB_POSITION = 6 ;

  public PousseSeringue getPousseSeringue()
  {
    // TODO Auto-generated method stub
    return null ;
  }

  public Passeur getPasseur()
  {
    // TODO Auto-generated method stub
    return null ;
  }

  public Colonne getColonne()
  {
    // TODO Auto-generated method stub
    return null ;
  }

  public double debitMaxPousseSeringue()
  {
    // TODO Auto-generated method stub
    return 0 ;
  }

  public int nbRincage()
  {
    // TODO Auto-generated method stub
    return 0 ;
  }

  public double volumeRincage()
  {
    // TODO Auto-generated method stub
    return 0 ;
  }

  public int epaisseur()
  {
    // TODO Auto-generated method stub
    return 0 ;
  }

  public int refCarrousel()
  {
    // TODO Auto-generated method stub
    return 0 ;
  }

}


/*

  : pousseSeringue (parametresSession.nbSeringue() , parametresSession.diametreSeringue(),
                    parametresSession.volumeMaxSeringue() , parametresSession.debitMaxPousseSeringue() )  ,

    passeur (parametresSession.nbPasCarrousel(), parametresSession.diametreCarrousel())


*/