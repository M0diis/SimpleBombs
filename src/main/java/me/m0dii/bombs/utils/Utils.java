package me.m0dii.bombs.utils;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.m0dii.bombs.Bomb;
import me.m0dii.bombs.BombTime;
import me.m0dii.bombs.SimpleBombs;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class Utils
{
    private static final Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])");
    
    public static String format(String text)
    {
        if(text == null || text.isEmpty())
            return "";
        
        return ChatColor.translateAlternateColorCodes('&', HEX_PATTERN.matcher(text).replaceAll("&x&$1&$2&$3&$4&$5&$6"));
    }
    
    private static boolean canExplodeWorldGuard(Location loc)
    {
        if(Bukkit.getPluginManager().getPlugin("WorldGuard") == null)
        {
            return true;
        }
        
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(loc));
    
        return set.queryValue(null, SimpleBombs.WG_BOMBS) == StateFlag.State.ALLOW;
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
    
                    if (canExplodeWorldGuard(block.getLocation()))
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
    
    public static void logToFile(String file, String text)
    {
        try
        {
            File logFolder = SimpleBombs.getInstance().getDataFolder();
            
            if(!logFolder.exists())
            {
                logFolder.mkdir();
            }
            
            File saveTo = new File(SimpleBombs.getInstance().getDataFolder(), file);
            
            if (!saveTo.exists())
            {
                saveTo.createNewFile();
            }
            
            FileWriter fw = new FileWriter(saveTo, true);
            PrintWriter pw = new PrintWriter(fw);

            pw.println("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "] " + text.trim());
            
            pw.flush();
            pw.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }
}
