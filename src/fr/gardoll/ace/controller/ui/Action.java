package fr.gardoll.ace.controller.ui;

public class Action
{
  public final ActionType type;
  public final Object data;
  
  public Action(ActionType type, Object data)
  {
    this.type = type;
    this.data = data;
  }
}
