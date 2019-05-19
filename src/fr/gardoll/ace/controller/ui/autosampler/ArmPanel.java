package fr.gardoll.ace.controller.ui.autosampler;

import fr.gardoll.ace.controller.ui.ControlPanel ;

public class ArmPanel implements ControlPanel
{

  @Override
  public void enableControl(boolean isEnable)
  {
    // TODO Auto-generated method stub
    
  }
}

/*

void TF_ControlesPasseur::enableControl ( bool state )

{ BitBtnGo->Enabled = state ;
  BitBtnFermer->Enabled = state ;
  BitBtnGauche->Enabled = state ;
  BitBtnDroite->Enabled = state ;
  BitBtnRef->Enabled = state ;

  BitBtnArretUrgence->Enabled = true ;

  BitBtnGoLibre->Enabled = state ;
  BitBtnGoButee->Enabled = state ;
  BitBtnGoColonne->Enabled = state ;
  BitBtnVibration->Enabled = state ;
  BitBtnPoubelle->Enabled = state ;

  BitBtnFermer->Enabled = state ;

  fermeture = state ;
}

*/

/*

void __fastcall TF_ControlesPasseur::FormCloseQuery(TObject *Sender,
      bool &CanClose)
{
    if ( ! fermeture ) MessageDlg (C_FERMETURE, mtInformation, TMsgDlgButtons() << mbOK , 0 );

   CanClose = fermeture ;        
}


*/