package me.m0dii.bombs.commands;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.m0dii.bombs.SimpleBombs;
import me.m0dii.bombs.bomb.Bomb;
import me.m0dii.bombs.utils.Config;
import me.m0dii.bombs.utils.InventoryUtils;
import me.m0dii.bombs.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class BombCommand implements CommandExecutor, TabCompleter
{
    private final SimpleBombs plugin;
    
    private final Config cfg;
    
    public BombCommand(SimpleBombs plugin)
    {
        this.plugin = plugin;
        
        this.cfg = plugin.getCfg();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args)
    {
        if(!sender.hasPermission("simplebombs.use"))
        {
            sender.sendMessage(cfg.getStrfp("no-permission-command", sender));
        
            return true;
        }
    
        execute(sender, args);
        
        return true;
    }
    
    private void execute(CommandSender sender, String[] args)
    {
        if(args.length == 0)
        {
            sender.sendMessage(cfg.getStrfp("usage", sender));
        
            return;
        }
    
        if(args.length == 1)
        {
            if(args[0].equalsIgnoreCase("reload"))
            {
                if(!sender.hasPermission("simplebombs.command.reload"))
                {
                    sender.sendMessage(cfg.getStrfp("no-permission-command", sender));
        
                    return;
                }
    
                plugin.getCfg().reload();
    
                sender.sendMessage(cfg.getStrf("config-reloaded"));
    
                return;
            }
            
            if(args[0].equalsIgnoreCase("gui"))
            {
                if(!sender.hasPermission("simplebombs.command.gui"))
                {
                    sender.sendMessage(cfg.getStrfp("no-permission-command", sender));
        
                    return;
                }

                if(sender instanceof Player)
                {
                    InventoryUtils.getPaginatedGui().open((Player) sender);
                }

                return;
            }
        }
    
        if(args.length >= 3 && args[0].equalsIgnoreCase("give"))
        {
            if(!sender.hasPermission("simplebombs.command.give"))
            {
                sender.sendMessage(cfg.getStrfp("no-permission-command", sender));
        
                return;
            }
            
            if(args[1] == null)
            {
                sender.sendMessage(cfg.getStrfp("usage", sender));
            
                return;
            }
    
            Player receiver = Bukkit.getPlayerExact(args[1]);
    
            if(receiver == null || !receiver.isOnline())
            {
                sender.sendMessage(cfg.getStrfp("invalid-player", sender));
    
                return;
            }
    
            int id = 1;
            
            try
            {
                id = Integer.parseInt(args[2]);
            }
            catch(NumberFormatException ignored) { }
    
            Bomb bomb = plugin.getBomb(id);
    
            if(bomb == null)
            {
                sender.sendMessage(cfg.getStrfp("invalid-bomb-id", sender));

                return;
            }
    
            int amount = 1;
            
            if(args.length == 4)
            {
                try
                {
                    amount = Integer.parseInt(args[3]);
                }
                catch(NumberFormatException ignored) { }
            }
    
            ItemStack item = plugin.getBomb(id).getItemStack();
    
            item.setAmount(amount);
    
            receiver.getInventory().addItem(item);
    
            String msg = cfg.getStrfp("bomb-received", sender);
            
            if(msg != null)
            {
                receiver.sendMessage(msg.replace("%tier%", id + "")
                        .replace("%amount%", amount + ""));
            }
            
            return;
        }
    
        sender.sendMessage(cfg.getStrfp("usage", sender));
    }
    
    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command,
                                      @Nonnull String label, String[] args)
    {
        List<String> completes = new ArrayList<>();
        
        if(args.length == 1)
        {
            completes.add("reload");
            completes.add("give");
            completes.add("gui");
        }
        
        if(args.length == 2 && args[0].equalsIgnoreCase("give"))
        {
            for(Player p : Bukkit.getOnlinePlayers())
            {
                if(p.getName().toLowerCase().contains(args[1].toLowerCase()))
                {
                    completes.add(p.getName());
                }
            }
        }
        
        if(args.length == 3 && args[0].equalsIgnoreCase("give"))
        {
            for(Integer key : cfg.getBombs().keySet())
            {
                completes.add(key + "");
            }
        }
        
        return completes;
    }
}
