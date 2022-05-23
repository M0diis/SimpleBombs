package me.m0dii.bombs.utils.config;

import me.m0dii.bombs.bomb.Bomb;
import me.m0dii.bombs.SimpleBombs;
import me.m0dii.bombs.bomb.BombType;
import me.m0dii.bombs.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config
{
    private FileConfiguration cfg;
    
    private Map<Integer, Bomb> bombs;
    
    private final SimpleBombs plugin;
    
    private boolean placeholdersEnabled;
    
    public boolean placeholdersEnabled() {
        return this.placeholdersEnabled;
    }
    
    public Config(SimpleBombs plugin)
    {
        this.plugin = plugin;
        
        load();
    }

    public void reload()
    {
        plugin.reloadConfig();
        
        load();
    
        this.placeholdersEnabled = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
    }
    
    public void load()
    {
        this.cfg = plugin.getConfig();
        
        generateBombs();
        
        this.placeholdersEnabled = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
    }
    
    public void generateBombs()
    {
        this.bombs = new HashMap<>();
        
        ConfigurationSection sec = cfg.getConfigurationSection("bombs");
    
        if(sec == null)
        {
            return;
        }
        
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
            boolean destroyBlocks = sec.getBoolean(key + ".destroy-blocks", true);
            
            boolean autoSell = sec.getBoolean(key + ".auto-sell", false);
            
            boolean glowing = sec.getBoolean(key + ".glowing", false);
            
            int entityDamage = sec.getInt(key + ".entity-damage");
            
            String throwSound = sec.getString(key + ".sound.throw", "ENTITY_ARROW_SHOOT");
            String explodeSound = sec.getString(key + ".sound.explode", "ENTITY_GENERIC_EXPLODE");
            
            Bomb bomb = Bomb.builder()
                    .setId(id)
                    .setName(name)
                    .setMaterial(material)
                    .setLore(lore)
                    .setThrowStrength(throwStrength)
                    .setRadius(radius)
                    .setFortune(fortune)
                    .setTime(time)
                    .setPermission(permission)
                    .setEffect(effect)
                    .setHologramText(hologram)
                    .setDestroyLiquids(destroyLiquids)
                    .setDestroyBlocks(destroyBlocks)
                    .setAutoSell(autoSell)
                    .setEntityDamage(entityDamage)
                    .setExplodeSound(explodeSound)
                    .setThrowSound(throwSound)
                    .setGlowing(glowing)
                    .build();
            
            if(sec.contains(key + ".destroy", true))
            {
                List<String> blocks = sec.getStringList(key + ".destroy.blocks");
                
                boolean whitelist = sec.getBoolean(key + ".destroy.whitelist", true);
                
                bomb.setDestroyIsWhitelist(whitelist);
                bomb.setCheckedBlocks(blocks);
            }
            
            if(sec.contains(key + ".smelt", true))
            {
                List<String> blocks = sec.getStringList(key + ".smelt.blocks");
                
                boolean whitelist = sec.getBoolean(key + ".smelt.whitelist", false);
                boolean enabled = sec.getBoolean(key + ".smelt.enable", false);
                
                bomb.setSmeltIsWhitelist(whitelist);
                bomb.setSmeltBlocks(blocks);
                bomb.setSmeltEnabled(enabled);
            }
            
            handleCustomProperties(sec, key, bomb);
            
            bombs.put(id, bomb);
        }
    }
    
    private void handleCustomProperties(ConfigurationSection sec, String key, Bomb bomb)
    {
        if(sec == null || !sec.contains(key + ".custom", true))
        {
            return;
        }
    
        if(!sec.contains(key + ".custom"))
        {
            return;
        }
        
        String type = sec.getString(key + ".custom.explosion-type");
    
        if(type == null)
        {
            return;
        }
    
        if(type.equalsIgnoreCase("scatter"))
        {
            String bombType = sec.getString(key + ".custom.bomb-type");
            String amount = sec.getString(key + ".custom.amount");
            
            bomb.setBombType(BombType.SCATTER);
            
            bomb.addProperty("bomb-type", bombType);
            bomb.addProperty("amount", amount);
        }
    }
    
    public Map<Integer, Bomb> getBombs()
    {
        return bombs;
    }
    
    public Bomb getBomb(int id)
    {
        return bombs.getOrDefault(id, null);
    }
    
    public String getStr(String path)
    {
        return this.cfg.getString(path, "");
    }
    
    public int getInt(String path)
    {
        return this.cfg.getInt(path, 0);
    }

    public String getStrf(String path)
    {
        return Utils.format(this.cfg.getString(path, ""));
    }
}
