package fr.gardoll.ace.controller.tools.settings.general;

import fr.gardoll.ace.controller.settings.ConfigurationException ;

public interface Panel
{
  public void set() throws ConfigurationException;
  public String getName();
  public void check() throws ConfigurationException;
}
