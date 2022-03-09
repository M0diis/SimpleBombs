package me.m0dii.bombs;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleBombs extends JavaPlugin
{
    private static SimpleBombs instance;
    
    public static SimpleBombs getInstance()
    {
        return instance;
    }
    
    private Map<Integer, Bomb> bombs;
    
    public Map<Integer, Bomb> getBombs()
    {
        return bombs;
    }
    
    public Bomb getBomb(int id)
    {
        return bombs.getOrDefault(id, null);
    }
    
    public void onEnable()
    {
        instance = this;
        
        getCommand("bomb").setExecutor(new BombCommand(this));
        
        Bukkit.getPluginManager().registerEvents(new EventListener(this), this);
        
        saveDefaultConfig();
    
        scheduleTasks();
        
        generateBombs();
    }
    
    public void generateBombs()
    {
        this.bombs = new HashMap<>();
        
        ConfigurationSection sec = getConfig().getConfigurationSection("bombs");
    
        if(sec != null)
        {
            for(String key : sec.getKeys(false))
            {
                int id = Integer.parseInt(key);
                
                String name = sec.getString(key + ".name");
                Material material = Material.getMaterial(sec.getString(key + ".material"));
                
                int radius = sec.getInt(key + ".radius");
                int fortune = sec.getInt(key + ".fortune");
                
                int time = sec.getInt(key + ".detonation-time");
                String effect = sec.getString(key + ".effect");
                String permission = sec.getString(key + ".permission");
                
                List<String> lore = sec.getStringList(key + ".lore");
                
                String hologram = sec.getString(key + ".detonation-text");
                
                double throwStrength = sec.getDouble(key + ".throw-strength");
                
                boolean destroyLiquids = sec.getBoolean(key + ".destroy-liquids");
                
                int entityDamage = sec.getInt(key + ".entity-damage");
                
                Bomb bomb = new Bomb(id, name, material, lore, throwStrength, radius, fortune, time, permission);
                
                bomb.setEffect(effect);
                bomb.setHologramText(hologram);
                bomb.setDestroyLiquids(destroyLiquids);
                bomb.setDamage(entityDamage);
                
                bombs.put(id, bomb);
            }
        }
    }
    
    private void scheduleTasks()
    {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () ->
        {
            for (Hologram h : Utils.holograms)
            {
                h.teleport(Utils.hologramItem.get(h).getLocation().clone().add(0.0D, 1.0D, 0.0D));
            }
        }, 0L, 0L);
        
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () ->
        {
            for (Hologram gram : Utils.holograms)
            {
                BombTime bombTime = Utils.hologramTime.get(gram);
                
                double time = bombTime.getTime();
                
                time -= 0.125D;
                
                if (time <= 0.0D)
                {
                    time = 0.0D;
                }
                
                bombTime.setTime(Utils.round(time, 1));
                
                Utils.hologramTime.put(gram, bombTime);
                
                Utils.editHologram(gram, bombTime);
            }
        }, 0L, 2L);
    }
    
    public void onDisable()
    {
        for(Hologram h : Utils.holograms)
        {
            h.delete();
        }
    }
}
