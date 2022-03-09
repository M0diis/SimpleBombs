package me.m0dii.bombs;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Utils
{
    public static String format(String string)
    {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
    
    private static boolean isWorldGuardExplodeable(Location loc)
    {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(loc));
    
        return set.queryValue(null, Flags.BLOCK_BREAK) == StateFlag.State.ALLOW;
    }
    
    public static boolean isExplodeable(Location paramLocation)
    {
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null)
        {
            return isWorldGuardExplodeable(paramLocation);
        }
        
        return true;
    }
    
    public static int max()
    {
        ConfigurationSection sec = SimpleBombs.getInstance().getConfig().getConfigurationSection("bombs");
        
        if(sec != null)
        {
            return sec.getKeys(false).size();
        }
        
        return 0;
    }
    
    public static String getBombNameFromID(int ID)
    {
        return SimpleBombs.getInstance().getConfig().getString("bombs." + ID + ".name");
    }
    
    public static int getBombID(String itemName)
    {
        for (int a = 1; a <= max(); a++)
        {
            if (format(getBombNameFromID(a)).equalsIgnoreCase(format(itemName)))
                return a;
        }
        
        return 1;
    }
    
    public static boolean isBomb(ItemStack item)
    {
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null && meta.hasDisplayName())
        {
            String name = meta.getDisplayName();
            
            for (int a = 1; a <= max(); a++)
            {
                String n = getBombNameFromID(a);
                
                if (format(n).equalsIgnoreCase(format(name)))
                    return true;
            }
        }
        
        return false;
    }
    
    public static List<Block> getCylinder(Location center, int radius)
    {
        List<Block> blocks = new ArrayList<>();
        
        for (int X = -radius; X < radius; X++)
        {
            for (int Y = -radius; Y < radius; Y++)
            {
                for (int Z = -radius; Z < radius; Z++)
                {
                    if(!(Math.sqrt((X * X + Y * Y + Z * Z)) <= radius))
                    {
                        continue;
                    }
                    
                    Block block = center.getWorld().getBlockAt(
                            X + center.getBlockX(), Y + center.getBlockY(), Z + center.getBlockZ());
    
                    if (isExplodeable(block.getLocation()))
                    {
                        blocks.add(block);
                    }
                }
            }
        }
        
        return blocks;
    }
    

    
    public static HashMap<Hologram, BombTime> hologramTime = new HashMap<>();
    
    public static HashMap<Hologram, Item> hologramItem = new HashMap<>();
    
    public static List<Hologram> holograms = new ArrayList<>();
    
    public static double round(double value, int places)
    {
        if (places < 0)
            throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    
    public static Hologram createHologram(Location loc, int bombID)
    {
        Hologram gram = HologramsAPI.createHologram(SimpleBombs.getInstance(), loc.clone().add(0.0D, 1.0D, 0.0D));
        
        Bomb bomb = SimpleBombs.getInstance().getBomb(bombID);
        
        String text = bomb.getHologramText().replace("%time%", bomb.getTime() + "");
        
        gram.appendTextLine(format(text));
        
        holograms.add(gram);
        
        BombTime time = new BombTime(bombID, bomb.getTime());
        
        hologramTime.put(gram, time);
        
        return gram;
    }
    
    public static void editHologram(Hologram gram, BombTime bombTime)
    {
        gram.removeLine(0);
        
        Bomb bomb = SimpleBombs.getInstance().getBomb(bombTime.getID());
    
        String text = bomb.getHologramText().replace("%time%", bombTime.getTime() + "");
        
        gram.insertTextLine(0, format(text));
    }
    
    public static void removeHologram(Hologram gram)
    {
        holograms.remove(gram);
        hologramTime.remove(gram);
        hologramItem.remove(gram);
        gram.delete();
    }
}
