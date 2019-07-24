package fr.gardoll.ace.controller.core;

import java.util.Optional ;

public class Action
{
  public final ActionType type;
  public final Optional<Object> data;
  
  public Action(ActionType type, Optional<Object> data)
  {
    this.type = type;
    this.data = data;
  }
}
