package fr.gardoll.ace.controller.core;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

public class Log
{
  public static final Logger UI;
  public static final Logger LOW_LEVEL;
  public static final Logger HIGH_LEVEL;
  public static final Logger STUB;
  
  static
  {
    UI         = LogManager.getLogger("UI_LOG");
    LOW_LEVEL  = LogManager.getLogger("LOW_LEVEL_LOG");
    HIGH_LEVEL = LogManager.getLogger("HIGH_LEVEL_LOG");
    STUB       = LogManager.getLogger("STUB_LOG");
  }
}
