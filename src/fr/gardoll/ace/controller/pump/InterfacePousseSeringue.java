package fr.gardoll.ace.controller.pump;

import java.io.Closeable;

import fr.gardoll.ace.controller.comm.SerialCom;

public class InterfacePousseSeringue  implements Closeable
{
  private final SerialCom _port;
  private final int _debitMaxIntrinseque;
  
  public InterfacePousseSeringue(double diametreSeringue, SerialCom port)
  {
    this._port = port ;
    this._debitMaxIntrinseque = 0 ;
  }
  
  private void traitementReponse(String message)
  {
    
  }
  
  private String traitementOrdre(String ordre)
  {
    return null;
  }
  
  private String lectureReponse()
  {
    return null;
  }
  
  private String formatage(double nombre)
  {
    return null;
  }
  
  public void run()
  {
    
  }
  
  public void stop()
  {
    
  }
  
  public void arretUrgence()
  {
    
  }
  
  public void dia(double diametre)
  {
    
  }
  
  public boolean running()
  {
    return false;
  }
  
  public double deliver()
  {
    return 0;
  }
  
  public void ratei(double debit)
  {
    
  }
  
  public void ratew(double debit)
  {
    
  }
  
  public void voli(double volume)
  {
    
  }
  
  public void volw(double volume)
  {
    
  }
  
  public void modeI()
  {
    
  }
  
  public void modeW()
  {
    
  }
  
  public static int debitMaxIntrinseque(double diametreSeringue)
  {
    return 0 ;
  }
  
  @Override
  public void close()
  {
    this._port.close() ;
  }
  
  public static void main(String[] args)
  {
    // TODO Auto-generated method stub

  }
}
