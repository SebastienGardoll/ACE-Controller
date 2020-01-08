package fr.gardoll.ace.controller.ui;

import javax.swing.JTextField ;

import fr.gardoll.ace.controller.core.ConfigurationException ;

public class TextFieldRealNumber
{
  public final String displayName;
  public final JTextField textField;
  public final double min;
  public final double max;
  
  public TextFieldRealNumber(String displayName, JTextField textField,
                             double min, double max)
  {
    this.displayName = displayName;
    this.textField = textField;
    this.min = min;
    this.max = max;
  }
  
  public double parse() throws ConfigurationException
  {
    String rawText = this.textField.getText().strip();
    
    if(rawText.isEmpty())
    {
      String msg = String.format("field %s is empty", this.displayName);
      throw new ConfigurationException(msg);
    }
    
    double result;
    
    try
    {
      result = Double.valueOf(rawText);
    }
    catch(Exception e)
    {
      String msg = String.format("field %s doesn't containt a number (got '%s')",
                                 this.displayName, rawText);
      throw new ConfigurationException(msg);
    }
    
    if(result <= this.min)
    {
      String msg = String.format("field %s cannot be less or equal than %s (got '%s')",
          this.displayName, this.min, result);
      throw new ConfigurationException(msg);
    }
    
    if(result >= this.max)
    {
      String msg = String.format("field %s cannot be greater or equal than %s (got '%s')",
          this.displayName, this.max, result);
      throw new ConfigurationException(msg);
    }
    
    return result;
  }
}