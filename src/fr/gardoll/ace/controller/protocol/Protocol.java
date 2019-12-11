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
  
  public final String author;
  
  public final String comments;
  
  public final String date;
  
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
    
    // - 1 due à la section informations
    this.nbMaxSequence = iniConf.getSections().size() - 1;
    
    {
      SubnodeConfiguration protocolMetadata = iniConf.getSection(Names.SEC_INFO_PROTOCOLE);
      
      this.nomProtocole = protocolMetadata.getString(Names.SIP_CLEF_NOM_PROTO, "");
      
      this._tabSequence  = new Sequence[this.nbMaxSequence];
      
      String cheminColonne = protocolMetadata.getString(Names.SIP_CLEF_CHEMIN_FICHIER_COL, ""); 
          
      this.colonne = Colonne.getInstance(cheminColonne);
      
      this.author = protocolMetadata.getString(Names.SIP_CLEF_AUTEUR, "");
      this.comments = protocolMetadata.getString(Names.SIP_CLEF_COMMENTAIRES, "");
      this.date = protocolMetadata.getString(Names.CLEF_DATE, "");
      
      if(this.nomProtocole.isEmpty() ||
         this.comments.isEmpty()     ||
         this.author.isEmpty()       ||
         this.date.isEmpty())
      {
        String msg = String.format("corrupted protocol metadata file '%s'",
                                   cheminFichierProtocole);
        _LOG.fatal(msg);
        throw new InitializationException(msg);
      }
      
      protocolMetadata.close();
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
      SubnodeConfiguration sequenceSection = iniConf.getSection(String.valueOf((i+1)));
      
      String nomAcide = sequenceSection.getString(Names.SIP_CLEF_ACIDE, "");
      
      int numEv = sequenceSection.getInt(Names.SIP_CLEF_NUM_EV, -1);
      
      double volume = sequenceSection.getDouble(Names.SIP_CLEF_VOL, -1.);

      int temps = sequenceSection.getInt(Names.SIP_CLEF_TEMPS, -1);
     
      int pauseCode = sequenceSection.getInteger(Names.SIP_CLEF_PAUSE, -1);

      if (nomAcide.isEmpty() ||
          numEv < 0          ||
          volume < 0.        ||
          temps < 0          ||
          pauseCode < 0)
      {
        String msg = String.format("unable to read the speficication of sequence '%s'", (i+1));
        _LOG.fatal(msg);
        throw new InitializationException(msg);
      }
      
      boolean pause = (pauseCode == 1)?true:false;
      
      this._tabSequence[i] = new Sequence(nomAcide, numEv, volume, temps, pause);
      
      tempsTotal += temps;
      
      sequenceSection.close();
    }
    
    this.tempsTotal = tempsTotal;
  }
  
  // de 1 à nbMaxSequence.
  public Sequence sequence(int numSequence)
  {
    return this._tabSequence[numSequence-1]; //car 1er indice = 0 !!!
  }
}
