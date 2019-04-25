package fr.gardoll.ace.controller.ui;

public interface Observable
{
  public void addObserver(ControlPanel panel) ;

  public void removeObserver(ControlPanel panel) ;

  public void notifyObserver(Action action) ;
}