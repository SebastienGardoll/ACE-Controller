package fr.gardoll.ace.controller.tools.extraction;

import java.nio.file.Path ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.ParametresSession ;
import fr.gardoll.ace.controller.protocol.Protocol ;

public class InitSession
{
  private static final Logger _LOG = LogManager.getLogger(InitSession.class.getName());
  
  // The number of column.
  // Zero means no column.
  final public int nbColonne ; 
  
  final public Protocol protocol ; 
  
  // The carousel position of the first column to begin with.
  // The value must be greater than zero as the first available position is 1,
  // zero is the trash position.
  final public int numColonne ;
  
  // The id of the sequence (line) of the protocol to begin with.
  // The 1 means the first sequence, there is not any "zero" sequence. 
  final public int numSequence ;
  
  public InitSession(int nbColonne, int numColonne, int numSequence,
                     Path cheminFichierProtocole) throws InitializationException
  {
    this.nbColonne = nbColonne;
    this.numColonne = numColonne;
    this.numSequence = numSequence;
    this.protocol = new Protocol(cheminFichierProtocole);
    
    if ( nbColonne <= 0 )
    {
      String msg = String.format("the number of columns cannot be zero or less, instead got '%s'",
          nbColonne);
      throw new InitializationException(msg) ;
    }
    
    if ( numColonne <= 0 )
    {
      String msg = String.format("the column position cannot be zero or less, instead got '%s'",
          numColonne);
      throw new InitializationException(msg) ;
    }
    
    if ( numSequence <= 0 )
    {
      String msg = String.format("the sequence id cannot be zero or less, instead got '%s'",
          numSequence);
      throw new InitializationException(msg) ;
    }
    
    if(this.numColonne > this.nbColonne)
    {
      String msg = String.format("cannot resume to the column #%s out of %s columns",
          numColonne, nbColonne);
      throw new InitializationException(msg) ;
    }
    
    if(this.numSequence > this.protocol.nbMaxSequence)
    {
      String msg = String.format("cannot resume to the sequence #%s out of %s sequences",
          numSequence, this.protocol.nbMaxSequence);
      throw new InitializationException(msg) ;
    }
    
    int maxColumnUtil = ParametresSession.getInstance().nbMaxColonne() - 1;
    if(this.nbColonne > maxColumnUtil)
    {
      String msg = String.format("number of columns cannot be greater than %s", maxColumnUtil);
      throw new InitializationException(msg) ;
    }
  }
  
  @Override
  public String toString()
  {
    String result = String.format("initialization: nbColumn=%s ; numColumn=%s ; numSequence=%s ; protocol=%s",
        this.nbColonne, this.numColonne, this.numSequence,
        this.protocol.protocolFilePath.toAbsolutePath());
    
    return result;
  }
}
