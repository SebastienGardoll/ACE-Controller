package fr.gardoll.ace.controller.settings;

import java.io.Closeable ;
import java.nio.file.Files ;
import java.nio.file.Path ;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.commons.configuration2.INIConfiguration ;
import org.apache.commons.configuration2.SubnodeConfiguration ;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder ;
import org.apache.commons.configuration2.builder.fluent.Configurations ;
import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.InitializationException ;

public abstract class Settings implements Closeable
{
  private static final Logger _LOG = LogManager.getLogger(Settings.class.getName());
  
  
  private final INIConfiguration _iniConf ;
  private final FileBasedConfigurationBuilder<INIConfiguration> _iniBuilder ;
  private final Map<String, SubnodeConfiguration> _sections = new HashMap<>();
  private final Path configurationFilePath ;
  
  public Settings(Path configurationFilePath) throws InitializationException
  {
    _LOG.debug(String.format("opening %s", configurationFilePath));
    
    if (Files.isReadable(configurationFilePath)    == false ||
        Files.isRegularFile(configurationFilePath) == false)
    {
      String msg = String.format("unable to read the configuration file '%s'",
          configurationFilePath);
      throw new InitializationException(msg);
    }
    
    try
    {
      Configurations configs = new Configurations();
      this._iniBuilder = configs.iniBuilder(configurationFilePath.toFile());
      this._iniConf = this._iniBuilder.getConfiguration();
    }
    catch(org.apache.commons.configuration2.ex.ConfigurationException e)
    {
      String msg = String.format("unable to open property file '%s'", configurationFilePath);
      throw new InitializationException(msg, e);
    }
    
    this.configurationFilePath = configurationFilePath;
  }

  protected void setValue(String sectionName, String key, Object value)
      throws ConfigurationException
  {
    _LOG.trace(String.format("set value '%s' for key '%s' in section '%s'",
        value, key, sectionName));
    
    SubnodeConfiguration sectionNode = fetchSection(sectionName);
    
    try
    {
      sectionNode.setProperty(key, value);
    }
    catch (Exception e)
    {
      String msg = String.format("unable to set value '%s' for key '%s' in section '%s' for '%s'",
          value, key, sectionName, this.configurationFilePath);
      throw new ConfigurationException(msg, e);
    }
  }
  
  protected Object fetchValue (String sectionName, String key, Class<?> klass)
      throws ConfigurationException
  {
    SubnodeConfiguration sectionNode = fetchSection(sectionName);
    
    Object value = sectionNode.get(klass, key, null);
    
    if(value == null)
    {
      String msg = String.format("unable to fetch the value of the key '%s' in section '%s' of %s",
          key, sectionName, this.configurationFilePath);
      throw new ConfigurationException(msg);
    }
    
    return value;
  }
  
  protected SubnodeConfiguration fetchSection(String sectionName)
      throws ConfigurationException
  {
    SubnodeConfiguration sectionNode = null; 
    
    if(false == this._sections.containsKey(sectionName))
    {
      _LOG.trace(String.format("fetch section %s", sectionName));
      try
      {
        sectionNode = this._iniConf.getSection(sectionName);
      }
      catch(Exception e)
      {
        String msg = String.format("unable to open section '%s' in %s",
            sectionName, this.configurationFilePath);
        throw new ConfigurationException(msg, e);
      }
      
      this._sections.put(sectionName, sectionNode);
    }
    else
    {
      sectionNode = this._sections.get(sectionName);
    }
    
    return sectionNode;
  }
  
  public void save() throws ConfigurationException
  {
    _LOG.debug(String.format("save propertyfile '%s'", this.configurationFilePath));
    
    try
    {
      this._iniBuilder.save();
    }
    catch (org.apache.commons.configuration2.ex.ConfigurationException e)
    {
      String msg = String.format("unable to save the propertyfile '%s'", this.configurationFilePath);
      throw new ConfigurationException(msg, e);
    }
  }
  
  @Override
  public void close()
  {
    for(SubnodeConfiguration section: this._sections.values())
    {
      section.close();
    }
  }
}
