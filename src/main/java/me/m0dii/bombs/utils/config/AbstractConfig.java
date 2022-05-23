package me.m0dii.bombs.utils.config;

import me.m0dii.bombs.SimpleBombs;

import me.m0dii.bombs.utils.Utils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;

public class AbstractConfig
{
    private final SimpleBombs plugin;
    private final File file;
    
    protected String resourceFolder;
    
    FileConfiguration config;
    
    protected AbstractConfig(File parentFile, String resourceFolder, String configName, SimpleBombs plugin)
    {
        this.plugin = plugin;
        
        this.file = new File(parentFile, configName);
        
        this.resourceFolder = resourceFolder;
        
        loadConfig();
    }
    
    protected FileConfiguration loadConfig()
    {
        config = createConfig();
        
        saveConfig();
        
        return config;
    }
    
    protected void saveConfig()
    {
        try
        {
            config.save(file);
        }
        catch (IOException ex)
        {
            plugin.getLogger().warning("Error while saving " + file.getName() + ".");
        }
    }
    
    public void reloadConfig()
    {
        config = loadConfig();
    }
    
    protected void deleteConfig()
    {
        if(file.exists())
        {
            file.delete();
        }
    }
    
    protected FileConfiguration createConfig()
    {
        if(!file.exists())
        {
            file.getParentFile().mkdirs();
            
            if(resourceFolder.isEmpty())
                plugin.saveResource(file.getName(), false);
            else plugin.saveResource(resourceFolder + File.separator + file.getName(), false);
        }
        
        FileConfiguration config = new YamlConfiguration();
        
        try
        {
            config.load(file);
            
            plugin.getLogger().info("Successfully loaded " + file.getName() + ".");
        }
        catch (IOException | InvalidConfigurationException ex)
        {
            plugin.getLogger().warning("Error while loading " + file.getName() + ".");
        }
        
        return config;
    }
    
    protected String getString(String path)
    {
        String text = config.getString(path);
        
        if(text == null || text.isEmpty())
        {
            plugin.getLogger().warning("The result of '" + path + "' is empty or null.");
            
            return "";
        }
        
        return Utils.format(text);
    }
}