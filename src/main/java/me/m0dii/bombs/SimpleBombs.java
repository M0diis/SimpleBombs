package me.m0dii.bombs;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import me.m0dii.bombs.bomb.Bomb;
import me.m0dii.bombs.bomb.BombTime;
import me.m0dii.bombs.events.EventListener;
import me.m0dii.bombs.utils.Config;
import me.m0dii.bombs.utils.UpdateChecker;
import me.m0dii.bombs.utils.Utils;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleBombs extends JavaPlugin
{
    /**
     * The plugin instance.
     */
    private static SimpleBombs instance;
    
    /**
     * SimpleBombs instance getter.
     *
     * @return The SimpleBombs instance.
     */
    public static SimpleBombs getInstance()
    {
        return instance;
    }
    
    private Config cfg;
    
    public Config getCfg()
    {
        return cfg;
    }
    
    /**
     * Get bomb by id.
     * @see Bomb for Bomb class.
     *
     * @param id The id of the bomb.
     * @return Bomb or null if not found.
     */
    public Bomb getBomb(int id)
    {
        return cfg.getBomb(id);
    }
    
    public void onEnable()
    {
        instance = this;
        
        cfg = new Config(this);
        
        getCommand("bomb").setExecutor(new BombCommand(this));
        
        Bukkit.getPluginManager().registerEvents(new EventListener(this), this);
        
        saveDefaultConfig();
    
        scheduleTasks();
        
        checkForUpdates();
        
        setupMetrics();
    }
    
    /**
     * Register WorldGuard flag.
     */
    public void onLoad()
    {
        registerWorldGuardFlags();
    }
    
    /**
     * Check for updates.
     *
     * @see UpdateChecker
     */
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
    
    /**
     * Setup data collection.
     *
     * @see Metrics
     */
    private void setupMetrics()
    {
        Metrics metrics = new Metrics(this, 14582);
        
        metrics.addCustomChart(new SingleLineChart("managing_bombs", cfg.getBombs()::size));
    }
    
    public static StateFlag WG_BOMBS;
    
    /**
     * Register custom WorldGuard flag.
     */
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
    
    /**
     * Schedule repeating tasks for holograms.
     */
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
