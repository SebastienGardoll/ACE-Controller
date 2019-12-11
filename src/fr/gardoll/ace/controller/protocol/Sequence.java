package fr.gardoll.ace.controller.protocol;

import java.util.Optional ;

public class Sequence
{
  public String nomAcide ;
  public final int numEv ;
  public final double volume ; //en mL
  public final int temps ;   // en seconde
  public final boolean pause ;
  public final boolean derniereSequence ; //indique si la séquence considérée est la dernière

  // An empty value means that the current sequence is the last sequence of
  // a given protocol.
  public final Optional<Integer> numEvSuivant ;  // pointeur sur le numéro de l'EV de la séquence suivante
  public final Optional<Double> volumeSuivant ;   //idem
  public final Optional<Integer> tempsPrecedent ;
  
  public Sequence(String nomAcide, int numEv, double volume, int temps, boolean pause,
                  boolean derniereSequence, Optional<Integer> numEvSuivant,
                  Optional<Double> volumeSuivant, Optional<Integer> tempsPrecedent)
  {
    this.nomAcide = nomAcide;
    this.numEv = numEv;
    this.volume = volume;
    this.temps = temps;
    this.pause = pause;
    this.derniereSequence = derniereSequence;
    this.numEvSuivant = numEvSuivant;
    this.volumeSuivant = volumeSuivant;
    this.tempsPrecedent = tempsPrecedent;
  }
}
