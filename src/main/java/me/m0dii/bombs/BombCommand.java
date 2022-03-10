package me.m0dii.bombs;

import me.m0dii.bombs.utils.Utils;
import org.bukkit.Bukkit;
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
    
    public BombCommand(SimpleBombs plugin)
    {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args)
    {
        if(!sender.hasPermission("simplebombs.use"))
        {
            sender.sendMessage(Utils.format(plugin.getConfig().getString("no-permission-command")));
        
            return true;
        }
    
        execute(sender, args);
        
        return true;
    }
    
    private void execute(CommandSender sender, String[] args)
    {
        if(args.length == 0)
        {
            sender.sendMessage(Utils.format(plugin.getConfig().getString("usage")));
        
            return;
        }
    
        if(args.length == 1 && args[0].equalsIgnoreCase("reload"))
        {
            if(!sender.hasPermission("simplebombs.command.reload"))
            {
                sender.sendMessage(Utils.format(plugin.getConfig().getString("no-permission-command")));
                
                return;
            }
            
            plugin.reloadConfig();
            
            plugin.generateBombs();
        
            sender.sendMessage(Utils.format(plugin.getConfig().getString("config-reloaded")));
        
            return;
        }
    
        if(args.length >= 3 && args[0].equalsIgnoreCase("give"))
        {
            if(!sender.hasPermission("simplebombs.command.give"))
            {
                sender.sendMessage(Utils.format(plugin.getConfig().getString("no-permission-command")));
        
                return;
            }
            
            if(args[1] == null)
            {
                sender.sendMessage(Utils.format(plugin.getConfig().getString("usage")));
            
                return;
            }
    
            Player receiver = Bukkit.getPlayerExact(args[1]);
    
            if(receiver == null || !receiver.isOnline())
            {
                sender.sendMessage(Utils.format(plugin.getConfig().getString("invalid-player")));
    
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
                sender.sendMessage(Utils.format(plugin.getConfig().getString("invalid-bomb-id")));

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
    
            String msg = plugin.getConfig().getString("bomb-received");
            
            if(msg != null)
            {
                receiver.sendMessage(Utils.format(msg
                        .replace("%tier%", id + "")
                        .replace("%amount%", amount + "")));
            }
            
            return;
        }
    
        sender.sendMessage(Utils.format(plugin.getConfig().getString("usage")));
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
            for(Integer key : plugin.getBombs().keySet())
            {
                completes.add(key + "");
            }
        }
        
        return completes;
    }
}
