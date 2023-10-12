package me.m0dii.bombs.utils.config;

import me.clip.placeholderapi.PlaceholderAPI;
import me.m0dii.bombs.SimpleBombs;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

public class LangConfig extends AbstractConfig
{
    private final SimpleBombs plugin;
    
    public LangConfig(SimpleBombs plugin, String locale)
    {
        super(new File(plugin.getDataFolder() + File.separator + "lang"), "lang", "messages_" + locale + ".yml", plugin);
        
        plugin.getLogger().info("Loading language file: messages_" + locale + ".yml");
        
        this.plugin = plugin;
    }
    
    public String getStrfp(String path, CommandSender sender)
    {
        String str = getString(path);
        
        if(sender instanceof Player && plugin.getCfg().placeholdersEnabled())
        {
            Player p = (Player) sender;
            
            str = PlaceholderAPI.setPlaceholders(p, str);
        }
        
        return str;
    }
    
    public String getStrf(String path)
    {
        return getString(path);
    }
    
    public void reload()
    {
        super.reloadConfig();
    }
}