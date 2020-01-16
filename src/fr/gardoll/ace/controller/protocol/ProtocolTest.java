package fr.gardoll.ace.controller.protocol;

//import org.hamcrest.collection.IsMapContaining;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals ;

import java.nio.file.Path ;
import java.util.HashMap ;
import java.util.Map ;
import java.util.Map.Entry ;

import org.apache.commons.lang3.tuple.Pair ;
import org.hamcrest.core.IsCollectionContaining ;
import org.junit.jupiter.api.BeforeAll ;
import org.junit.jupiter.api.Test ;

import fr.gardoll.ace.controller.column.TypeColonne ;

class ProtocolTest
{
  private static Protocol _PROTOCOL = null;

  @BeforeAll
  static void setUpBeforeClass() throws Exception
  {
    String protocolFileName = "sr-spec.prt";
    Path protocolFilePath = Protocol.computeProtocolFilePath(protocolFileName); 
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
  
  @Test
  void test5()
  {
    Map<String, Integer> expectedAcidList = new HashMap<>();
    expectedAcidList.put("HNO3 7N", 5);
    expectedAcidList.put("HNO3 0,05N", 2);
    expectedAcidList.put("HCl 6N", 3);
    expectedAcidList.put("HNO3 2N", 4);
    
    Map<String, Integer> actualAcidList = _PROTOCOL.getAcidList();
    
    assertThat(actualAcidList.size(), is(expectedAcidList.size()));

    for(Entry<String, Integer> expectedEntry: expectedAcidList.entrySet())
    {
      assertThat(actualAcidList.entrySet(),
          IsCollectionContaining.hasItem(expectedEntry));
    }
  }
  
  @Test
  void test6()
  {
    int nbColumn = 2;
    
    Map<String, Double> expectedAcidVolums = new HashMap<>();
    expectedAcidVolums.put("HNO3 7N", 1.5*nbColumn);
    expectedAcidVolums.put("HNO3 0,05N", 5.*nbColumn);
    expectedAcidVolums.put("HCl 6N", 1.*nbColumn);
    expectedAcidVolums.put("HNO3 2N", 1.7*nbColumn);
    
    Map<String, Double> actualAcidVolums = _PROTOCOL.protocolVolume(nbColumn);
    
    assertThat(actualAcidVolums.size(), is(expectedAcidVolums.size()));
    
    for(Entry<String, Double> expectedEntry: expectedAcidVolums.entrySet())
    {
      assertThat(actualAcidVolums.entrySet(),
          IsCollectionContaining.hasItem(expectedEntry));
    }
  }
  
  @Test
  void test7()
  {
    Map<String, Double> expectedRinseVolumes = new HashMap<>();
    
    expectedRinseVolumes.put("HNO3 7N", 1.5);
    expectedRinseVolumes.put("HNO3 0,05N", 3.);
    expectedRinseVolumes.put("HCl 6N", 1.5);
    expectedRinseVolumes.put("HNO3 2N", 3.);
    
    double expectedH2OVolume = 7.;
    
    Pair<Double, Map<String, Double>> data = _PROTOCOL.rinseVolume();
    Map<String, Double> actualRinseVolumes = data.getRight();
    double actualH2OVolume = data.getLeft();
    
    for(Entry<String, Double> expectedEntry: expectedRinseVolumes.entrySet())
    {
      assertThat(actualRinseVolumes.entrySet(),
          IsCollectionContaining.hasItem(expectedEntry));
    }
    
    assertEquals(expectedH2OVolume, actualH2OVolume);
  }
}
