package fr.gardoll.ace.controller.ui;

import javax.swing.JTextField ;

import fr.gardoll.ace.controller.settings.ConfigurationException ;

public class TextFieldRealNumber
{
  public final String displayName;
  public final JTextField textField;
  
  public TextFieldRealNumber(String displayName, JTextField textField)
  {
    this.displayName = displayName;
    this.textField = textField;
  }
  
  public double parse() throws ConfigurationException
  {
    String rawText = this.textField.getText().strip();
    
    if(rawText.isEmpty())
    {
      String msg = String.format("field '%s' is empty", this.displayName);
      textField.requestFocus();
      throw new ConfigurationException(msg);
    }
    
    double result = 0.;
    
    try
    {
      result = Double.valueOf(rawText);
    }
    catch(Exception e)
    {
      String msg = String.format("field '%s' doesn't containt a number (got '%s')",
                                 this.displayName, rawText);
      textField.requestFocus();
      throw new ConfigurationException(msg);
    }
    
    return result;
  }
}