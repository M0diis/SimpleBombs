package me.m0dii.bombs.utils.config;

import me.m0dii.bombs.SimpleBombs;

public class PriceConfig extends AbstractConfig
{
    private final SimpleBombs plugin;
    
    public PriceConfig(SimpleBombs plugin)
    {
        super(plugin.getDataFolder(), "", "prices.yml", plugin);
        
        plugin.getLogger().info("Loading price file.");
        
        this.plugin = plugin;
    }
    
    public double getPrice(String item)
    {
        return config.getDouble(item, 0.0);
    }
    
    public void reload()
    {
        super.reloadConfig();
    }
}