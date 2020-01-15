package fr.gardoll.ace.controller.core;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

public class Log
{
  public static final Logger UI;
  public static final Logger COM;
  public static final Logger CONTROLLER;
  public static final Logger STUB;
  public static final Logger HIGH_LEVEL;
  
  
  static
  {
    UI         = LogManager.getLogger("UI_LOG");
    COM        = LogManager.getLogger("COM_LOG");
    STUB       = LogManager.getLogger("STUB_LOG");
    CONTROLLER = LogManager.getLogger("CONTROLLER_LOG");
    HIGH_LEVEL = LogManager.getLogger("HIGH_LEVEL_LOG");
  }
}
