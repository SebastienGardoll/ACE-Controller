package fr.gardoll.ace.controller.protocol;

import java.nio.file.Files ;
import java.nio.file.Path ;

import org.apache.commons.configuration2.INIConfiguration ;
import org.apache.commons.configuration2.SubnodeConfiguration ;
import org.apache.commons.configuration2.builder.fluent.Configurations ;
import org.apache.commons.configuration2.ex.ConfigurationException ;
import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.column.Colonne ;
import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.Names ;

public class Protocol
{
  private static final Logger _LOG = LogManager.getLogger(Protocol.class.getName());
  
  // The insertion order is mandatory.
  private final Sequence[] _tabSequence;
  
  public final String nomProtocole;
  
  public final int nbMaxSequence; // nombre de séquences total
  
  public final int tempsTotal ; // somme de temps d'élution
  
  public final Colonne colonne ;
  
  public Protocol(Path cheminFichierProtocole) throws InitializationException
  {
    if(false == Files.isReadable(cheminFichierProtocole) &&
       false == Files.isRegularFile(cheminFichierProtocole))
    {
      String msg = String.format("cannot read the protocol file '%s'",
                                 cheminFichierProtocole);
      _LOG.fatal(msg);
      throw new InitializationException(msg);
    }
    
    Configurations configs = new Configurations();
    INIConfiguration iniConf = null;
    
    try
    {
      iniConf = configs.ini(cheminFichierProtocole.toFile());
    }
    catch (ConfigurationException e)
    {
      String msg = String.format("unable to read the protocol specifications in '%s'",
                                 cheminFichierProtocole.toString());
      _LOG.fatal(msg, e);
      throw new InitializationException(msg,e);
    }
    
    SubnodeConfiguration colSection = iniConf.getSection(Names.SEC_INFO_PROTOCOLE);
    
    this.nbMaxSequence = colSection.size() - 1; // - 1 due à la section informations
    
    this.nomProtocole = colSection.getString(Names.SIP_CLEF_NOM_PROTO, "");
    
    this._tabSequence  = new Sequence[this.nbMaxSequence];
    
    String cheminColonne = colSection.getString(Names.SIP_CLEF_CHEMIN_FICHIER_COL, ""); 
        
    this.colonne = Colonne.getInstance(cheminColonne);
    
    if(this.nomProtocole.isEmpty())
    {
      String msg = String.format("corrupted protocol metadata file '%s'",
                                 cheminFichierProtocole);
      _LOG.fatal(msg);
      throw new InitializationException(msg);
    }
    
    /***********************************************************************/
    // Intervales :
    //
    // numSequence attributs de la classe panneau  : [1 , numMaxSequence ]
    // tabSequence : [0 , numMaxSequence -1 ]
    // fichier protocole : [1 , numMaxSequence ]
    //
    /***********************************************************************/
    
    int tempsTotal = 0 ;
    
    for ( int i = 0 ; i < this.nbMaxSequence ; i ++ )
    {
      colSection = iniConf.getSection(String.valueOf((i+1)));
      
      String nomAcide = colSection.getString(Names.SIP_CLEF_ACIDE, "");
      
      int numEv = colSection.getInt(Names.SIP_CLEF_NUM_EV, -1);
      
      double volume = colSection.getDouble(Names.SIP_CLEF_VOL, -1.);

      int temps = colSection.getInt(Names.SIP_CLEF_TEMPS, -1);
     
      boolean pause = colSection.getBoolean(Names.SIP_CLEF_PAUSE, null);
      
      if (nomAcide.isEmpty() ||
          numEv < 0          ||
          volume < 0.        ||
          temps < 0)
      {
        String msg = String.format("unable to read the speficication of sequence '%s'", (i+1));
        _LOG.fatal(msg);
        throw new InitializationException(msg);
      }
      
      tempsTotal += this._tabSequence[i].temps;
      
      this._tabSequence[i] = new Sequence(nomAcide, numEv, volume, temps, pause);
    }
    
    this.tempsTotal = tempsTotal;
  }
  
  // de 1 à nbMaxSequence.
  public Sequence sequence(int numSequence)
  {
    return this._tabSequence[numSequence-1]; //car 1er indice = 0 !!!
  }
}
