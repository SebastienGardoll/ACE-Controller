package fr.gardoll.ace.controller.protocol;

import java.nio.file.Files ;
import java.nio.file.Path ;
import java.nio.file.Paths ;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.commons.configuration2.INIConfiguration ;
import org.apache.commons.configuration2.SubnodeConfiguration ;
import org.apache.commons.configuration2.builder.fluent.Configurations ;
import org.apache.commons.lang3.tuple.ImmutablePair ;
import org.apache.commons.lang3.tuple.Pair ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.column.Colonne ;
import fr.gardoll.ace.controller.core.Log ;
import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.pump.PousseSeringue ;
import fr.gardoll.ace.controller.settings.ConfigurationException ;
import fr.gardoll.ace.controller.settings.GeneralSettings ;
import fr.gardoll.ace.controller.settings.Names ;

public class Protocol
{
  private static final Logger _LOG = Log.HIGH_LEVEL;
  
  public static final String PROTOCOL_FILE_EXTENTION = "prt";
  public static final String PROTOCOL_DIRNAME  = "protocoles";
  public static final Path PROTOCOL_DIR_PATH = 
      Paths.get(Names.CONFIG_DIRNAME, PROTOCOL_DIRNAME);
  
  // The insertion order is mandatory.
  private final Sequence[] _tabSequence;
  
  public final String nomProtocole;
  
  public final int nbMaxSequence; // nombre de séquences total
  
  public final long tempsTotal ; // somme de temps d'élution
  
  public final Colonne colonne ;
  
  public final String author;
  
  public final String comments;
  
  public final String date;
  
  public final Path protocolFilePath;
  
  public Protocol(Path cheminFichierProtocole) throws ConfigurationException
  {
    if(false == Files.isReadable(cheminFichierProtocole) &&
       false == Files.isRegularFile(cheminFichierProtocole))
    {
      String msg = String.format("cannot read the protocol file '%s'",
                                 cheminFichierProtocole);
      throw new ConfigurationException(msg);
    }
    
    Configurations configs = new Configurations();
    INIConfiguration iniConf = null;
    
    try
    {
      iniConf = configs.ini(cheminFichierProtocole.toFile());
    }
    catch (Exception e)
    {
      String msg = String.format("unable to read the protocol specifications in '%s'",
                                 cheminFichierProtocole.toString());
      throw new ConfigurationException(msg,e);
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
        throw new ConfigurationException(msg);
      }
      
      protocolMetadata.close();
      
      this.protocolFilePath = cheminFichierProtocole;
    }
    
    /***********************************************************************/
    // Intervales :
    //
    // numSequence attributs de la classe panneau  : [1 , numMaxSequence ]
    // tabSequence : [0 , numMaxSequence -1 ]
    // fichier protocole : [1 , numMaxSequence ]
    //
    /***********************************************************************/
    
    long tempsTotal = 0l ;
    
    for ( int i = 0 ; i < this.nbMaxSequence ; i++ )
    {
      SubnodeConfiguration sequenceSection = iniConf.getSection(String.valueOf((i+1)));
      
      String nomAcide = sequenceSection.getString(Names.SIP_CLEF_ACIDE, "");
      
      int numEv = sequenceSection.getInt(Names.SIP_CLEF_NUM_EV, -1);
      
      double volume = sequenceSection.getDouble(Names.SIP_CLEF_VOL, -1.);

      long temps = sequenceSection.getLong(Names.SIP_CLEF_TEMPS, -1l);
     
      int pauseCode = sequenceSection.getInteger(Names.SIP_CLEF_PAUSE, -1);

      if (nomAcide.isEmpty() ||
          numEv < 0          ||
          volume < 0.        ||
          temps < 0l         ||
          pauseCode < 0)
      {
        String msg = String.format("unable to read the speficication of sequence '%s'", (i+1));
        throw new ConfigurationException(msg);
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
  
//Returns the list of the procol's acids and their associated valve.
 public Map<String, Integer> getAcidList() throws ConfigurationException
 {
   Map<String, Integer> result = new HashMap<>();
   
   for(int index = 1 ; index <= this.nbMaxSequence ; index++)
   {
     Sequence sequence = this.sequence(index);
     
     if(result.containsKey(sequence.nomAcide))
     {
       if(sequence.numEv != result.get(sequence.nomAcide))
       {
         String msg = String.format("sequence %s is not consistent about the valve id (got '%s')",
             index, sequence.numEv);
         throw new ConfigurationException(msg);
       }
       else
       {
         // Nothing to do.
       }
     }
     else
     {
       result.put(sequence.nomAcide, sequence.numEv);
     }
   }
   
   return result;
 }
  
  // Return the volume for each acid of the protocol without rinsing volume.
  public Map<String, Double> protocolVolume(int nbColumn)
  {
    Map<String, Double> result = new HashMap<>();
    
    for(int index = 1 ; index <= this.nbMaxSequence ; index++)
    {
      Sequence sequence = this.sequence(index);
      
      String acidName = sequence.nomAcide;
      
      double volume = 0.;
      
      if(result.containsKey(acidName))
      {
        volume = result.get(acidName);
      }
      else
      {
        volume = 0.;
      }
      
      //volume total utilisé pour l'élution dans la séquence courante
      volume += sequence.volume * nbColumn ;
      result.put(acidName, volume);
    }
    
    Utils.round(result);
    
    return result;
  }  
 
  public Pair<Double, Map<String, Double>> rinseVolume() throws ConfigurationException
  {
    Map<String, Double> acideVolumes = new HashMap<>();
    
    _LOG.debug(String.format("analyzing the rince volume of the protocol %s", this.nomProtocole));
    
    GeneralSettings settings = GeneralSettings.instance();
    
    double totalVolumeRincageH2O = 0.;
    
    //car le volume n'est pas divisé par nbSeringue pour le rinçage !!!
    double volumeRincageH2O =  settings.getNbRincage() * 
                               settings.getNbSeringue() *
              (settings.getVolumeRincage() + PousseSeringue.volumeAjustement());
    
    // à chaque rinçage perte du volume de sécurité qui a été aspiré pendant la distribution
    // mais ce volume ne dépend pas du volume de rinçage mais est induit au fait du rinçage
    // volume de sécurité n'est pas divisé par nbSeringue
    double volumeRincageAcide =  volumeRincageH2O + 
                   (PousseSeringue.volumeSecurite() * settings.getNbSeringue());
    
    for(int index = this.nbMaxSequence ; index > 1 ; index--)
    {
      Sequence currentSequence = this.sequence(index); 
      Sequence previousSequence = this.sequence(index - 1);
      
      if(currentSequence.numEv != previousSequence.numEv)
      {
        double volume = 0.;
        
        if(acideVolumes.containsKey(currentSequence.nomAcide))
        {
          volume = acideVolumes.get(currentSequence.nomAcide);
        }
        
        volume += volumeRincageAcide  ;
        
        acideVolumes.put(currentSequence.nomAcide, volume);
        
        // Rinçage H2O avant le Rinçage de l'acide suivant.
        totalVolumeRincageH2O += volumeRincageH2O  ;
      }
      else
      {
        // Nothing to add.
      }
    }
    
    // Process the special operations for the first sequence.
    double volume = 0.;
    if(acideVolumes.containsKey(this.sequence(1).nomAcide))
    {
      volume = acideVolumes.get(this.sequence(1).nomAcide);
    }
    volume += volumeRincageAcide  ;
    acideVolumes.put(this.sequence(1).nomAcide, volume);
    totalVolumeRincageH2O += volumeRincageH2O  ;
    
    // Process the special operations for the last sequence.
    //pour le rinçage pour la fin de sequence
    totalVolumeRincageH2O += volumeRincageH2O  ;
    
    totalVolumeRincageH2O = Utils.round(totalVolumeRincageH2O);
    Utils.round(acideVolumes);
    
    return new ImmutablePair<>(totalVolumeRincageH2O, acideVolumes);
  }
  
  public static Path computeProtocolFilePath(String protocolFileName)
  {
    Path protocolFilePath = Paths.get(Names.CONFIG_DIRNAME,
                                      Protocol.PROTOCOL_DIRNAME,
                                      protocolFileName);
    return protocolFilePath;
  }
}
