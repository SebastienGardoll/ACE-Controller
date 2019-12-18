package fr.gardoll.ace.controller.ui;

import java.awt.Container ;
import java.awt.Dialog ;
import java.awt.Window ;

public class UiUtils
{
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
