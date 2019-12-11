package fr.gardoll.ace.controller.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals ;

import java.nio.file.Path ;

import org.junit.jupiter.api.BeforeAll ;
import org.junit.jupiter.api.Test ;

import fr.gardoll.ace.controller.column.TypeColonne ;
import fr.gardoll.ace.controller.core.Names ;
import fr.gardoll.ace.controller.core.Utils ;

class ProtocolTest
{
  private static Protocol _PROTOCOL = null;

  @BeforeAll
  static void setUpBeforeClass() throws Exception
  {
    String protocolFileName = "sr-spec.prt";
    
    Path rootDir = Utils.getInstance().getRootDir();
    Path protocolFilePath = rootDir.resolve(Names.CONFIG_DIRNAME)
                             .resolve(Names.PROTOCOL_DIRNAME)
                             .resolve(protocolFileName);
    
    _PROTOCOL = new Protocol(protocolFilePath);
  }

  @Test
  void test1()
  {
    assertEquals("Sr - SPEC", _PROTOCOL.nomProtocole);
    assertEquals(16, _PROTOCOL.nbMaxSequence);
    assertEquals(9180, _PROTOCOL.tempsTotal);
    assertEquals(TypeColonne.HYBRIDE, _PROTOCOL.colonne.getType());
    assertEquals("Sebastien GARDOLL", _PROTOCOL.author);
    assertEquals("Protocole d'extraction du Sr par la resine Sr-SPEC ( Eichrom ). Attentes non optimisees : elles correspondent  aux attentes des colonnes entonnoirs.", _PROTOCOL.comments);
    assertEquals("11/05/2006", _PROTOCOL.date); 
  }
  
  @Test
  void test2()
  {
    int numSequence = 1;
    assertEquals("HCl 6N", _PROTOCOL.sequence(numSequence).nomAcide);
    assertEquals(3, _PROTOCOL.sequence(numSequence).numEv);
    assertEquals(1., _PROTOCOL.sequence(numSequence).volume);
    assertEquals(900, _PROTOCOL.sequence(numSequence).temps);
    assertEquals(false, _PROTOCOL.sequence(numSequence).pause);
  }
  
  @Test
  void test3()
  {
    int numSequence = _PROTOCOL.nbMaxSequence;
    assertEquals("HNO3 0,05N", _PROTOCOL.sequence(numSequence).nomAcide);
    assertEquals(2, _PROTOCOL.sequence(numSequence).numEv);
    assertEquals(0.5, _PROTOCOL.sequence(numSequence).volume);
    assertEquals(480, _PROTOCOL.sequence(numSequence).temps);
    assertEquals(false, _PROTOCOL.sequence(numSequence).pause);
  }
  
  @Test
  void test4()
  {
    int numSequence = 5;
    assertEquals("HNO3 2N", _PROTOCOL.sequence(numSequence).nomAcide) ;
    assertEquals(4, _PROTOCOL.sequence(numSequence).numEv) ;
    assertEquals(0.5, _PROTOCOL.sequence(numSequence).volume) ;
    assertEquals(480, _PROTOCOL.sequence(numSequence).temps) ;
    assertEquals(true, _PROTOCOL.sequence(numSequence).pause) ;
  }
}
