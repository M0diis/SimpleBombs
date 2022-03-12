package me.m0dii.bombs.events;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import me.m0dii.bombs.Bomb;
import me.m0dii.bombs.SimpleBombs;
import me.m0dii.bombs.utils.BombType;
import me.m0dii.bombs.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class EventListener implements Listener
{
    private final SimpleBombs plugin;
    
    public EventListener(final SimpleBombs plugin)
    {
        this.plugin = plugin;
    }
    
    private final List<Item> droppedItems = new ArrayList<>();
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

        if(!bomb.ignorePermission())
        {
            if(!p.hasPermission(bomb.getPermission()))
            {
                p.sendMessage(Utils.format(plugin.getConfig().getString("no-permission-bomb")));
        
                return;
            }
        }
        
        Bukkit.getPluginManager().callEvent(new BombThrowEvent(p, bomb, item, p.getLocation()));
    }
    
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent e)
    {
        if (droppedItems.contains(e.getItem()))
        {
            e.setCancelled(true);
        }
    }
    
    private void applyBlocksFortune(Player p, Bomb bomb, List<Block> blocks)
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
                        stuff.put(type.name(), bomb.getFortune());
        
                        continue;
                    }
    
                    int al = stuff.get(type.name());
    
                    stuff.remove(type.name());
                    stuff.put(type.name(), al + bomb.getFortune());
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
                ItemStack item = new ItemStack(material, amount);

                if(bomb.isSmeltEnabled())
                {
                    ItemStack result = null;
                    Iterator<Recipe> iter = Bukkit.recipeIterator();
                    
                    while (iter.hasNext()) 
                    {
                        Recipe recipe = iter.next();

                        if (!(recipe instanceof FurnaceRecipe)) 
                            continue;

                        if (((FurnaceRecipe) recipe).getInput().getType() != item.getType()) 
                            continue;

                       result = recipe.getResult();
                       break;
                    }
                    result.setAmount(item.getAmount());
                }

                p.getWorld().dropItemNaturally(p.getLocation(), item);
                continue;
            }
            
            int toGive = amount;
            
            for(int b = 1; b <= amount / 64; b++)
            {
                if(toGive > 64)
                {
                    p.getWorld().dropItemNaturally(p.getLocation(), new ItemStack(material, 64));
                    toGive -= 64;
                }
                else
                {
                    p.getWorld().dropItemNaturally(p.getLocation(), new ItemStack(material, toGive));
                }
        
            }
        }
        
        brokenBlocks.clear();
        stuff.clear();
    }
    
    @EventHandler
    public void onBombThrow(BombThrowEvent event)
    {
        if(event.isCancelled())
        {
            return;
        }
    
        Player p = event.getPlayer();
        Bomb bomb = event.getBomb();
        ItemStack item = event.getItem();
        Location loc = event.getInitialLocation();

        Sound throwSound = bomb.getThrowSound();

        if(throwSound != null)
        {
            p.playSound(loc, throwSound, 1, 1);
        }
        
        used.add(p);
    
        ItemStack droppedItemStack = new ItemStack(bomb.getMaterial());
        
        ItemMeta meta = droppedItemStack.getItemMeta();
        
        if(meta != null)
        {
            meta.setDisplayName(bomb.getName());
            droppedItemStack.setItemMeta(meta);
        }
        
        Item drop = p.getWorld().dropItemNaturally(loc, droppedItemStack);
    
        drop.setVelocity(loc.getDirection().multiply(bomb.getThrowStrength()));
    
        droppedItems.add(drop);
    
        final Hologram hg = Utils.createHologram(drop.getLocation(), bomb);
    
        Utils.hologramItem.put(hg, drop);
    
        Bukkit.getScheduler().runTaskLater(plugin, () ->
        {
            Bukkit.getPluginManager().callEvent(new BombExplodeEvent(bomb, p, drop.getLocation()));
        
            drop.getWorld().spawnParticle(bomb.getEffect(), drop.getLocation(), 5);
        
            Utils.removeHologram(hg);
        
            used.remove(p);
            droppedItems.remove(drop);
            drop.remove();
        }, bomb.getTime() * 20L);
    
        if(item == null)
        {
            return;
        }
        
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
    public void onBombExplode(BombExplodeEvent event)
    {
        if(event.isCancelled())
        {
            return;
        }
        
        Bomb bomb = event.getBomb();
        Player player = event.getPlayer();
        Location loc = event.getExplodeLoc();

        Sound explodeSound = bomb.getExplodeSound();

        if(explodeSound != null)
        {
            player.playSound(loc, explodeSound, 1, 1);
        }
        
        List<Block> explodedBlocks = Utils.getCylinder(loc, bomb.getRadius());
    
        applyBlocksFortune(player, bomb, explodedBlocks);
    
        for (Block b : explodedBlocks)
        {
            if(!bomb.doDestroyLiquids())
            {
                String name = b.getType().name().toUpperCase();
        
                if(name.contains("WATER") || name.contains("LAVA")
                        || b.getType().equals(Material.BEDROCK))
                {
                    continue;
                }
            }
            
            if(bomb.hasCheckedBlocks())
            {
                if(bomb.destroyIsWhitelist() && bomb.containsCheckedBlock(b.getType().name().toUpperCase()))
                {
                    b.setType(Material.AIR);
                }
                else if(!bomb.destroyIsWhitelist() && !bomb.containsCheckedBlock(b.getType().name().toUpperCase()))
                {
                    b.setType(Material.AIR);
                }
            }
            else
            {
                b.setType(Material.AIR);
            }
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
        
        if(bomb.getBombType().equals(BombType.SCATTER))
        {
            for(int i = 0; i < Integer.parseInt(bomb.getProperty("amount")); i++)
            {
                Bomb spawnedBomb = plugin.getBomb(Integer.parseInt(bomb.getProperty("bomb-type")));
                
                Bomb copy = spawnedBomb.copy();
                
                Location newLoc = loc.clone();
                
                newLoc.setDirection(getRandomDirection());
    
                copy.setName(bomb.getName() + " " + i);
                
                copy.setIgnorePerm(true);
                copy.setSendMessage(false);
                
                Bukkit.getPluginManager().callEvent(new BombThrowEvent(player, copy, null, newLoc));
            }
        }
        
        if(bomb.doSendMessage())
        {
            player.sendMessage(Utils.format(plugin.getConfig().getString("explosion-message").replace("%tier%",
                    bomb.getId() + "")));
        }
    }
    
    private Vector getRandomDirection()
    {
        Vector direction = new Vector();
        direction.setX(Math.random()*2-1d);
        direction.setY(Math.random()-0.5d);
        direction.setZ(Math.random()*2-1d);
        return direction.normalize();
    }
}
