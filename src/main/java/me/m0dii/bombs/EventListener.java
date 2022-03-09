package me.m0dii.bombs;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventListener implements Listener
{
    private final SimpleBombs plugin;
    
    public EventListener(final SimpleBombs plugin)
    {
        this.plugin = plugin;
    }
    
    private final List<Item> droppedItems = new ArrayList<>();
    private final HashMap<Player, Item> itemMap = new HashMap<>();
    private final  ArrayList<Player> used = new ArrayList<>();
    
    @EventHandler
    public void onInteract(PlayerInteractEvent e)
    {
        final Player p = e.getPlayer();
        
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_AIR)
        {
            return;
        }
        
        ItemStack item = p.getInventory().getItemInMainHand();
        
        if(!Utils.isBomb(item))
        {
            return;
        }
        
        e.setCancelled(true);
    
        if(used.contains(p))
        {
            return;
        }
        
        Bomb bomb = plugin.getBomb(Utils.getBombID(item.getItemMeta().getDisplayName()));
    
        if(!p.hasPermission(bomb.getPermission()))
        {
            p.sendMessage(Utils.format(plugin.getConfig().getString("no-permission-bomb")));
            
            return;
        }
        
        used.add(p);
        
        final int bombID = Utils.getBombID(item.getItemMeta().getDisplayName());
        
        Item drop = p.getWorld().dropItemNaturally(p.getLocation(), new ItemStack(bomb.getMaterial()));
        
        drop.setVelocity(p.getLocation().getDirection().multiply(bomb.getThrowStrength()));
        
        droppedItems.add(drop);
        itemMap.remove(p);
        itemMap.put(p, drop);
        
        final Hologram hg = Utils.createHologram(drop.getLocation(), bombID);
        
        Utils.hologramItem.put(hg, drop);
    
        Bukkit.getScheduler().runTaskLater(plugin, () ->
        {
            explode(bomb, p, itemMap.get(p).getLocation());
            
            itemMap.get(p).getWorld().spawnParticle(bomb.getEffect(), itemMap.get(p).getLocation(), 5);
            itemMap.get(p).remove();
            
            Utils.removeHologram(hg);
            
            used.remove(p);
        }, bomb.getTime() * 20L);
    
        if (item.getAmount() > 1)
        {
            p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);
            p.updateInventory();
        }
        else if (item.getAmount() == 1)
        {
            p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }
    }
    
    @EventHandler
    public void pickup(PlayerPickupItemEvent e)
    {
        if (droppedItems.contains(e.getItem()))
        {
            e.setCancelled(true);
        }
    }
    
    private void explode(Bomb bomb, Player player, Location startingLocation)
    {
        List<Block> explodedBlocks = Utils.getCylinder(startingLocation, bomb.getRadius());
        
        applyBlocksFortune(player, bomb.getFortune(), explodedBlocks);
        
        for (Block b : explodedBlocks)
        {
            if(!bomb.doDestroyLiquids())
            {
                String name = b.getType().name().toUpperCase();
                
                if(name.contains("WATER") || name.contains("LAVA"))
                {
                    continue;
                }
            }
            
            b.setType(Material.AIR);
        }
        
        if(bomb.getEntityDamage() != 0)
        {
            for(Entity e : player.getNearbyEntities(bomb.getRadius() * 2, bomb.getRadius() * 2, bomb.getRadius() * 2))
            {
                if(e instanceof LivingEntity)
                {
                    ((LivingEntity) e).damage(bomb.getEntityDamage());
                }
            }
        }
        
        player.sendMessage(Utils.format(SimpleBombs.getInstance().getConfig().getString("explosion-message").replace("%tier%",
                bomb.getId() + "")));
    }
    
    public static int getFortune(int bombID)
    {
        return SimpleBombs.getInstance().getConfig().getInt("bombs." + bombID + ".fortune");
    }
    
    public static double getExplode(int bombID)
    {
        return SimpleBombs.getInstance().getConfig().getDouble("bombs." + bombID + ".radius");
    }
    
    
    public static void applyBlocksFortune(Player p, int fortune, List<Block> blocks)
    {
        HashMap<String, Integer> stuff = new HashMap<>();
        ArrayList<String> brokenBlocks = new ArrayList<>();
        
        for (Block b : blocks)
        {
            if (b != null)
            {
                Material type = b.getType();
                
                if(type != Material.AIR)
                {
                    if(!brokenBlocks.contains(type.name()))
                    {
                        brokenBlocks.add(type.name());
                        stuff.put(type.name(), fortune);
        
                        continue;
                    }
    
                    int al = stuff.get(type.name());
    
                    stuff.remove(type.name());
                    stuff.put(type.name(), al + fortune);
                }
            }
        }
        List<String> ingots = new ArrayList<>();
        HashMap<String, Integer> ingot_stuff = new HashMap<>();
        
        for (String blockName : brokenBlocks)
        {
            ingots.add(blockName);
            ingot_stuff.put(blockName, stuff.get(blockName));
        }
        
        for (String id : ingots)
        {
            int amount = ingot_stuff.get(id);
            
            if(id.toUpperCase().contains("LAVA")
            || id.toUpperCase().contains("WATER"))
            {
                continue;
            }
            
            Material material = Material.getMaterial(id);
            
            if(material == null
            || material.equals(Material.AIR)
            || material.equals(Material.BEDROCK)
            || material.equals(Material.FIRE)
            || material.equals(Material.LAVA)
            || material.equals(Material.WATER))
            {
                continue;
            }
            
            if(amount == 0)
            {
                continue;
            }
            
            if(amount <= 64)
            {
                p.getWorld().dropItemNaturally(p.getLocation(), new ItemStack(material, amount));
                continue;
            }
            
            int stacks = amount / 64;
            
            for(int b = 1; b <= stacks; b++)
            {
                p.getWorld().dropItemNaturally(p.getLocation(), new ItemStack(material, 64));
            }
        }
        
        brokenBlocks.clear();
        stuff.clear();
    }
}
