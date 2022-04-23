package me.m0dii.bombs.utils;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import eu.decentsoftware.holograms.api.DHAPI;
import me.m0dii.bombs.SimpleBombs;
import me.m0dii.bombs.bomb.Bomb;
import me.m0dii.bombs.bomb.BombTime;
import me.m0dii.bombs.events.BombExplodeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class HologramUtils
{
    public enum Plugin
    {
        HOLOGRAPHIC_DISPLAYS,
        DECENT_HOLOGRAMS
    }
    
    private static final SimpleBombs plugin = SimpleBombs.getInstance();
    
    public static Plugin getHologramPlugin()
    {
        if (plugin.getServer().getPluginManager().isPluginEnabled("HolographicDisplays"))
        {
            return Plugin.HOLOGRAPHIC_DISPLAYS;
        }
        else if (plugin.getServer().getPluginManager().isPluginEnabled("DecentHolograms"))
        {
            return Plugin.DECENT_HOLOGRAMS;
        }
        
        return null;
    }
    
    public static HashMap<eu.decentsoftware.holograms.api.holograms.Hologram, BombTime> dhHologramTime = new HashMap<>();
    
    public static HashMap<eu.decentsoftware.holograms.api.holograms.Hologram, Item> dhHologramItem = new HashMap<>();
    
    public static List<eu.decentsoftware.holograms.api.holograms.Hologram> dhHolograms = new ArrayList<>();
    
    public static void editDhHologram(eu.decentsoftware.holograms.api.holograms.Hologram hg, BombTime bombTime)
    {
        Bomb bomb = SimpleBombs.getInstance().getBomb(bombTime.getID());
    
        String text = bomb.getHologramText().replace("%time%", bombTime.getTime() + "");
    
        text = Utils.setPlaceholders(text, bombTime.getPlayer(), bomb);
    
        DHAPI.setHologramLines(hg, 0, Collections.singletonList(text));
    }
    
    public static eu.decentsoftware.holograms.api.holograms.Hologram createDhHologram(Player p, Location loc, Bomb bomb)
    {
        String text = Utils.setPlaceholders(bomb.getHologramText(), p, bomb);
    
        eu.decentsoftware.holograms.api.holograms.Hologram hd =
                DHAPI.createHologram(bomb.getId() + "", loc, false, Collections.singletonList(text));
        
        dhHolograms.add(hd);
    
        BombTime time = new BombTime(bomb.getId(), bomb.getTime());
    
        time.setPlayer(p);
    
        dhHologramTime.put(hd, time);
        
        hd.enable();
    
        return hd;
    }
    
    public static void removeDhHologram(eu.decentsoftware.holograms.api.holograms.Hologram hg)
    {
        dhHologramTime.remove(hg);
        dhHolograms.remove(hg);
        dhHologramItem.remove(hg);
    
        hg.delete();
    }
    
    public static final List<Item> droppedItems = new ArrayList<>();
    public static final ArrayList<Player> used = new ArrayList<>();
    
    public static void handleDecentHolograms(Player p, Bomb bomb, Item drop)
    {
        eu.decentsoftware.holograms.api.holograms.Hologram hg =
                HologramUtils.createDhHologram(p, drop.getLocation(), bomb);
    
        HologramUtils.dhHologramItem.put(hg, drop);
    
        Bukkit.getScheduler().runTaskLater(plugin, () ->
        {
            Bukkit.getPluginManager().callEvent(new BombExplodeEvent(bomb, p, drop.getLocation()));
        
            drop.getWorld().spawnParticle(bomb.getEffect(), drop.getLocation(), 5);
        
            HologramUtils.removeDhHologram(hg);
        
            used.remove(p);
            droppedItems.remove(drop);
            drop.remove();
        }, bomb.getTime() * 20L);
    }
    
    public static void handleHolographicDisplays(Player p, Bomb bomb, Item drop)
    {
        com.gmail.filoghost.holographicdisplays.api.Hologram hg =
                HologramUtils.createHdHologram(p, drop.getLocation(), bomb);
    
        HologramUtils.hdHologramItem.put(hg, drop);
    
        Bukkit.getScheduler().runTaskLater(plugin, () ->
        {
            Bukkit.getPluginManager().callEvent(new BombExplodeEvent(bomb, p, drop.getLocation()));
        
            drop.getWorld().spawnParticle(bomb.getEffect(), drop.getLocation(), 5);
        
            HologramUtils.removeHdHologram(hg);
        
            used.remove(p);
            droppedItems.remove(drop);
            drop.remove();
        }, bomb.getTime() * 20L);
    }
    
    public static HashMap<Hologram, BombTime> hdHologramTime = new HashMap<>();
    
    public static HashMap<Hologram, Item> hdHologramItem = new HashMap<>();
    
    public static List<Hologram> hdHolograms = new ArrayList<>();

    public static com.gmail.filoghost.holographicdisplays.api.Hologram createHdHologram(Player p, Location loc, Bomb bomb)
    {
        com.gmail.filoghost.holographicdisplays.api.Hologram gram = HologramsAPI.createHologram(SimpleBombs.getInstance(), loc.clone().add(0.0D, 1.0D, 0.0D));
    
        String text = Utils.setPlaceholders(bomb.getHologramText(), p, bomb);
    
        gram.appendTextLine(Utils.format(text));
    
        hdHolograms.add(gram);
    
        BombTime time = new BombTime(bomb.getId(), bomb.getTime());
    
        time.setPlayer(p);
    
        hdHologramTime.put(gram, time);
    
        return gram;
    }
    
    public static void editHdHologram(com.gmail.filoghost.holographicdisplays.api.Hologram gram, BombTime bombTime)
    {
        gram.removeLine(0);
        
        Bomb bomb = SimpleBombs.getInstance().getBomb(bombTime.getID());
        
        String text = bomb.getHologramText().replace("%time%", bombTime.getTime() + "");
        
        text = Utils.setPlaceholders(text, bombTime.getPlayer(), bomb);
        
        gram.insertTextLine(0, Utils.format(text));
    }
    
    public static void removeHdHologram(com.gmail.filoghost.holographicdisplays.api.Hologram hg)
    {
        hdHologramTime.remove(hg);
        hdHolograms.remove(hg);
        hdHologramItem.remove(hg);
        
        hg.delete();
    }
}
