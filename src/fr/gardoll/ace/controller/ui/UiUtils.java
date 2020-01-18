package fr.gardoll.ace.controller.ui;

import java.awt.Container ;
import java.awt.Dialog ;
import java.awt.Window ;

import javax.swing.JFormattedTextField.AbstractFormatterFactory ;
import javax.swing.text.DefaultFormatterFactory ;
import javax.swing.text.NumberFormatter ;

import fr.gardoll.ace.controller.settings.GeneralSettings ;

public class UiUtils
{
  public static final AbstractFormatterFactory FORMATTER_FACTORY;
  
  static
  {
    NumberFormatter numberFormatter = 
        new NumberFormatter(GeneralSettings.DECIMAL_FORMAT);
    FORMATTER_FACTORY = new DefaultFormatterFactory(numberFormatter);
  }
  
  private UiUtils() {}
  
  public static Window getParentFrame(Container c)
  {
    while(false == c instanceof Window)
    {
      c = c.getParent();
    }
    
    return (Window) c;
  }
  
  public static Dialog getParentDialog(Container c)
  {
    while(false == c instanceof Dialog)
    {
      c = c.getParent();
    }
    
    return (Dialog) c;
  }
}
