package fr.gardoll.ace.controller.tools.settings.general;

import fr.gardoll.ace.controller.core.ConfigurationException ;

public interface Panel
{
  public void save() throws ConfigurationException;
  public String getName();
  public void check() throws ConfigurationException;
}
