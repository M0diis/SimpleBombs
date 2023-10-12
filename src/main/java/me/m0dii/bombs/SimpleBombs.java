package me.m0dii.bombs;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import eu.decentsoftware.holograms.api.DHAPI;
import me.m0dii.bombs.bomb.Bomb;
import me.m0dii.bombs.bomb.BombTime;
import me.m0dii.bombs.commands.BombCommand;
import me.m0dii.bombs.events.EventListener;
import me.m0dii.bombs.utils.config.Config;
import me.m0dii.bombs.utils.HologramUtils;
import me.m0dii.bombs.utils.UpdateChecker;
import me.m0dii.bombs.utils.Utils;
import me.m0dii.bombs.utils.config.LangConfig;
import me.m0dii.bombs.utils.config.PriceConfig;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;

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
    
    private static boolean testMode = false;
    
    public static void setTestMode(boolean mode)
    {
        testMode = mode;
    }
    
    public static boolean isTestMode()
    {
        return testMode;
    }
    
    private LangConfig langCfg;
    
    public LangConfig getLangCfg()
    {
        return langCfg;
    }
    
    private PriceConfig priceCfg;
    
    public PriceConfig getPriceCfg()
    {
        return priceCfg;
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
        
        setupEconomy();
    
        setupConfigurations();
    
        PluginCommand cmd = getCommand("bomb");
        
        if(cmd != null)
        {
            cmd.setExecutor(new BombCommand(this));
        }
    
        Bukkit.getPluginManager().registerEvents(new EventListener(this), this);
        
        saveDefaultConfig();
    
        scheduleTasks();
        
        checkForUpdates();
        
        if(!isTestMode())
        {
            setupMetrics();
        }
    }
    
    private void setupConfigurations()
    {
        cfg = new Config(this);
        
        langCfg = new LangConfig(this, getConfig().getString("locale"));
        
        priceCfg = new PriceConfig(this);
    }
    
    private Economy econ;
    
    public Economy getEconomy()
    {
        return econ;
    }
    
    private void setupEconomy()
    {
        if(getServer().getPluginManager().getPlugin("Vault") == null)
        {
            getLogger().warning("Vault not found. Auto-Sell will not work.");
            
            return;
        }
    
        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager()
                .getRegistration(Economy.class);
        
        if (provider != null)
        {
            econ = provider.getProvider();
        }
    }
    
    /**
     * Register WorldGuard flag.
     */
    @Override
    public void onLoad()
    {
        if(!isTestMode())
        {
            registerWorldGuardFlags();
        }
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
                getLogger().info("You are running an outdated version of SimpleBombs.");
                getLogger().info("You are using: " + getDescription().getVersion() + ".");
                getLogger().info("Latest version: " + ver + ".");
                getLogger().info("You can download the latest version on Spigot:");
                getLogger().info("https://www.spigotmc.org/resources/100596/");
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
    
            if(HologramUtils.getHologramPlugin() == HologramUtils.Plugin.HOLOGRAPHIC_DISPLAYS)
            {
                for (com.gmail.filoghost.holographicdisplays.api.Hologram h : HologramUtils.hdHolograms)
                {
                    h.teleport(HologramUtils.hdHologramItem.get(h).getLocation().clone().add(0.0D, 1.0D, 0.0D));
                }
            }
            else if(HologramUtils.getHologramPlugin() == HologramUtils.Plugin.DECENT_HOLOGRAMS)
            {
                for (eu.decentsoftware.holograms.api.holograms.Hologram h : HologramUtils.dhHolograms)
                {
                    DHAPI.moveHologram(h, HologramUtils.dhHologramItem.get(h).getLocation().clone().add(0.0D, 1.0D, 0.0D));
                }
            }
        }, 0L, 0L);
        
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () ->
        {
            if(HologramUtils.getHologramPlugin() == HologramUtils.Plugin.HOLOGRAPHIC_DISPLAYS)
            {
                for (com.gmail.filoghost.holographicdisplays.api.Hologram h : HologramUtils.hdHolograms)
                {
                    BombTime bombTime = HologramUtils.hdHologramTime.get(h);
        
                    double time = bombTime.getTime();
        
                    time -= 0.125D;
        
                    if (time <= 0.0D)
                    {
                        time = 0.0D;
                    }
        
                    bombTime.setTime(Utils.round(time, 1));
        
                    HologramUtils.hdHologramTime.put(h, bombTime);
        
                    HologramUtils.editHdHologram(h, bombTime);
                }
            }
            else if(HologramUtils.getHologramPlugin() == HologramUtils.Plugin.DECENT_HOLOGRAMS)
            {
                for (eu.decentsoftware.holograms.api.holograms.Hologram h : HologramUtils.dhHolograms)
                {
                    BombTime bombTime = HologramUtils.dhHologramTime.get(h);
        
                    double time = bombTime.getTime();
        
                    time -= 0.125D;
        
                    if (time <= 0.0D)
                    {
                        time = 0.0D;
                    }
        
                    bombTime.setTime(Utils.round(time, 1));
        
                    HologramUtils.dhHologramTime.put(h, bombTime);
        
                    HologramUtils.editDhHologram(h, bombTime);
                }
            }

        }, 0L, 2L);
    }
    
    public void onDisable()
    {
        for(com.gmail.filoghost.holographicdisplays.api.Hologram h : HologramUtils.hdHolograms)
        {
            h.delete();
        }
        
        for (eu.decentsoftware.holograms.api.holograms.Hologram h : HologramUtils.dhHolograms)
        {
            h.delete();
        }
    }
}
