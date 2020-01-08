package fr.gardoll.ace.controller.settings;

import fr.gardoll.ace.controller.core.ConfigurationException ;

public interface Panel
{
  public void save() throws ConfigurationException;
  public String getName();
}
