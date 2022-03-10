package me.m0dii.bombs;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import me.m0dii.bombs.events.EventListener;
import me.m0dii.bombs.utils.BombType;
import me.m0dii.bombs.utils.UpdateChecker;
import me.m0dii.bombs.utils.Utils;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
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
        
        checkForUpdates();
        
        setupMetrics();
    }
    
    public void onLoad()
    {
        registerWorldGuardFlags();
    }
    
    private void checkForUpdates()
    {
        new UpdateChecker(this, 100596).getVersion(ver ->
        {
            if (!this.getDescription().getVersion().equalsIgnoreCase(ver))
            {
                getLogger().info("You are running an outdated version of SRV-Cron.");
                getLogger().info("You are using: " + getDescription().getVersion() + ".");
                getLogger().info("Latest version: " + ver + ".");
                getLogger().info("You can download the latest version on Spigot:");
                getLogger().info("https://www.spigotmc.org/resources/100382/");
            }
        });
    }
    
    private void setupMetrics()
    {
        Metrics metrics = new Metrics(this, 14582);
        
        metrics.addCustomChart(new SingleLineChart("managing_bombs", bombs::size));
    }
    
    public static StateFlag WG_BOMBS;
    
    private void registerWorldGuardFlags()
    {
        if(Bukkit.getPluginManager().getPlugin("WorldGuard") == null)
        {
            return;
        }
        
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        
        try
        {
            StateFlag flag = new StateFlag("simple-bombs-explode", false);
            registry.register(flag);
            WG_BOMBS = flag;
        }
        catch (Exception ex)
        {
            Flag<?> existing = registry.get("simple-bombs-explode");
            
            if (existing instanceof StateFlag)
            {
                WG_BOMBS = (StateFlag) existing;
            }
        }
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
                Material material = Material.getMaterial(sec.getString(key + ".material", "FIRE_CHARGE"));
                
                int radius = sec.getInt(key + ".radius", 3);
                int fortune = sec.getInt(key + ".fortune", 1);
                
                int time = sec.getInt(key + ".detonation-time", 3);
                String effect = sec.getString(key + ".effect", "LARGE_EXPLOSION");
                String permission = sec.getString(key + ".permission", "simplebombs.bomb." + id);
                
                List<String> lore = sec.getStringList(key + ".lore");
                
                String hologram = sec.getString(key + ".detonation-text", "&f&l%time%");
                
                double throwStrength = sec.getDouble(key + ".throw-strength", 1.5D);
                
                boolean destroyLiquids = sec.getBoolean(key + ".destroy-liquids", false);
                
                boolean glowing = sec.getBoolean(key + ".glowing", false);
                
                int entityDamage = sec.getInt(key + ".entity-damage");
                
                String throwSound = sec.getString(key + ".sound.throw", "ENTITY.ARROW.SHOOT");
                String explodeSound = sec.getString(key + ".sound.explode", "ENTITY.GENERIC.EXPLODE");

                Bomb bomb = new Bomb(id, name, material, lore, throwStrength, radius, fortune, time, permission);
                
                bomb.clearProperties();
                
                bomb.setEffect(effect);
                bomb.setHologramText(hologram);
                bomb.setDestroyLiquids(destroyLiquids);
                bomb.setDamage(entityDamage);
                bomb.setGlowing(glowing);
                bomb.setExplodeSound(throwSound);
                bomb.setThrowSound(explodeSound);
                
                if(sec.contains(key + ".destroy"))
                {
                    List<String> blocks = sec.getStringList(key + ".destroy.blocks");
                    
                    boolean whitelist = sec.getBoolean(key + ".destroy.whitelist", true);
                    
                    bomb.setDestroyIsWhitelist(whitelist);
                    bomb.setCheckedBlocks(blocks);
                }
                
                bomb = handleCustomProperties(sec, key, bomb);
                
                bombs.put(id, bomb);
            }
        }
    }
    
    private Bomb handleCustomProperties(ConfigurationSection sec, String key, Bomb bomb)
    {
        if(sec == null || !sec.contains(key + ".custom", true))
        {
            return bomb;
        }
        
        if(sec.contains(key + ".custom"))
        {
            String type = sec.getString(key + ".custom.explosion-type");
            
            if(type == null)
            {
                return bomb;
            }
            
            if(type.equalsIgnoreCase("scatter"))
            {
                String bombType = sec.getString(key + ".custom.bomb-type");
                String amount = sec.getString(key + ".custom.amount");
    
                bomb.setBombType(BombType.SCATTER);

                bomb.addProperty("bomb-type", bombType);
                bomb.addProperty("amount", amount);
    
                return bomb;
            }
        }
    
        return bomb;
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
