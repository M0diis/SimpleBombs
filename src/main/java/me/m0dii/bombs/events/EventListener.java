package me.m0dii.bombs.events;

import me.m0dii.bombs.SimpleBombs;
import me.m0dii.bombs.bomb.Bomb;
import me.m0dii.bombs.bomb.BombType;
import me.m0dii.bombs.utils.config.Config;
import me.m0dii.bombs.utils.HologramUtils;
import me.m0dii.bombs.utils.Utils;
import me.m0dii.bombs.utils.config.LangConfig;
import me.m0dii.bombs.utils.config.PriceConfig;
import net.milkbowl.vault.economy.Economy;
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

import java.util.*;

public class EventListener implements Listener
{
    private final SimpleBombs plugin;
    
    private final Economy economy;
    
    private final PriceConfig priceConfig;
    private final LangConfig langConfig;
    private final Config cfg;
    
    public EventListener(final SimpleBombs plugin)
    {
        this.plugin = plugin;
        this.cfg = plugin.getCfg();
        this.priceConfig = plugin.getPriceCfg();
        this.langConfig = plugin.getLangCfg();
        
        this.economy = plugin.getEconomy();
    }
    
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
    
        if(HologramUtils.used.contains(p))
        {
            return;
        }
        
        Bomb bomb = plugin.getBomb(Utils.getBombID(item.getItemMeta().getDisplayName()));

        if(!bomb.ignorePermission())
        {
            if(!p.hasPermission(bomb.getPermission()))
            {
                p.sendMessage(cfg.getStrf("no-permission-bomb"));
        
                return;
            }
        }
        
        Bukkit.getPluginManager().callEvent(new BombThrowEvent(p, bomb, item, p.getLocation()));
    }
    
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent e)
    {
        if (HologramUtils.droppedItems.contains(e.getItem()))
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
    
                if(type == Material.AIR)
                {
                    continue;
                }
                
                if(bomb.hasCheckedBlocks())
                {
                    if(bomb.destroyIsWhitelist() && bomb.containsCheckedBlock(type.name().toUpperCase()))
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
                    else if(!bomb.destroyIsWhitelist() && !bomb.containsCheckedBlock(type.name().toUpperCase()))
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
                else
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
        
        double totalSum = 0;
        int totalAmount = 0;
        
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
            
            ItemStack item = new ItemStack(material);
            
            if(bomb.isSmeltEnabled())
            {
                if(bomb.smeltIsWhitelist())
                {
                    if(bomb.containsSmeltBlock(id.toUpperCase()))
                    {
                        item = getSmeltedItem(item);
                    }
                }
                else if(!bomb.containsSmeltBlock(id.toUpperCase()))
                {
                    item = getSmeltedItem(item);
                }
            }
    
            double price = priceConfig.getPrice(item.getType().name().toUpperCase());

            if(amount <= 64)
            {
                item.setAmount(amount);
    
                if(bomb.doAutoSell() && price > 0)
                {
                    totalAmount += amount;
    
                    totalSum += price * amount;
                }
                else
                {
                    p.getWorld().dropItemNaturally(p.getLocation(), item);
                }
                
                continue;
            }
            
            int toGive = amount;
            
            for(int b = 1; b <= amount / 64; b++)
            {
                if(toGive > 64)
                {
                    item.setAmount(64);
    
                    if(bomb.doAutoSell() && price > 0)
                    {
                        totalAmount += 64;
    
                        totalSum += price * 64;
                    }
                    else
                    {
                        p.getWorld().dropItemNaturally(p.getLocation(), item);
                    }
                    
                    toGive -= 64;
                }
                else
                {
                    item.setAmount(toGive);
                    
                    if(bomb.doAutoSell())
                    {
                        totalAmount += toGive;
                        
                        totalSum += price * toGive;
                    }
                    else
                    {
                        p.getWorld().dropItemNaturally(p.getLocation(), item);
                    }
                }
        
            }
        }
        
        brokenBlocks.clear();
        stuff.clear();
        
        if(bomb.doAutoSell() && totalSum > 0)
        {
            economy.depositPlayer(p, totalSum);
            
            String msg = langConfig.getStrfp("items-sold", p)
                    .replace("%price%", String.valueOf(totalSum))
                    .replace("%amount%", String.valueOf(totalAmount));
            
            p.sendMessage(msg);
        }
    }
    
    
    private ItemStack getSmeltedItem(ItemStack item) 
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
      
        if(result != null)
        {
            item = result;
        }
        
        return item;
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
        
        HologramUtils.used.add(p);
    
        ItemStack droppedItemStack = new ItemStack(bomb.getMaterial());
        
        ItemMeta meta = droppedItemStack.getItemMeta();
        
        if(meta != null)
        {
            List<String> lore = new ArrayList<>();
            
            lore.add(UUID.randomUUID().toString());
            
            meta.setLore(lore);
            
            droppedItemStack.setItemMeta(meta);
        }
        
        Item drop = p.getWorld().dropItemNaturally(loc, droppedItemStack);
    
        drop.setVelocity(loc.getDirection().multiply(bomb.getThrowStrength()));
    
        HologramUtils.droppedItems.add(drop);
        
        if(HologramUtils.getHologramPlugin() == HologramUtils.Plugin.HOLOGRAPHIC_DISPLAYS)
        {
            HologramUtils.handleHolographicDisplays(p, bomb, drop);
        }
        else if(HologramUtils.getHologramPlugin() == HologramUtils.Plugin.DECENT_HOLOGRAMS)
        {
            HologramUtils.handleDecentHolograms(p, bomb, drop);
        }
        
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
    
        Bukkit.getScheduler().runTask(plugin, () ->
        {
            for (Block b : explodedBlocks)
            {
                String name = b.getType().name().toUpperCase();
                
                if(!bomb.doDestroyBlocks())
                {
                    if(!name.contains("WATER") || !name.contains("LAVA"))
                    {
                        continue;
                    }
                }
                
                if(!bomb.doDestroyLiquids())
                {
                    if(name.contains("WATER") || name.contains("LAVA")
                            || b.getType().equals(Material.BEDROCK))
                    {
                        continue;
                    }
                }
        
                if(bomb.hasCheckedBlocks())
                {
                    if(bomb.destroyIsWhitelist() && bomb.containsCheckedBlock(name))
                    {
                        b.setType(Material.AIR);
                    }
                    else if(!bomb.destroyIsWhitelist() && !bomb.containsCheckedBlock(name))
                    {
                        b.setType(Material.AIR);
                    }
                }
                else
                {
                    b.setType(Material.AIR);
                }
            }
        });
    
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
                
                copy.setIgnorePerm(true);
                copy.setSendMessage(false);
                
                Bukkit.getPluginManager().callEvent(new BombThrowEvent(player, copy, null, newLoc));
            }
        }
        
        if(bomb.doSendMessage())
        {
            player.sendMessage(cfg.getStrf("explosion-message")
                    .replace("%tier%", bomb.getId() + ""));
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
