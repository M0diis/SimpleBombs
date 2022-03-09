package me.m0dii.bombs;

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
            plugin.reloadConfig();
            
            plugin.generateBombs();
        
            sender.sendMessage(Utils.format(plugin.getConfig().getString("config-reloaded")));
        
            return;
        }
    
        if(args.length >= 3 && args[0].equalsIgnoreCase("give"))
        {
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
    
            int id = Integer.parseInt(args[2]);
    
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
    
            receiver.sendMessage(Utils.format(plugin.getConfig().getString("bomb-received")
                    .replace("%tier%", id + "")
                    .replace("%amount%", amount + "")));
    
            return;
        }
    
        sender.sendMessage(Utils.format(plugin.getConfig().getString("usage")));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        List<String> completes = new ArrayList<>();
        
        if(args.length == 0)
        {
            completes.add("reload");
            completes.add("give");
        }
        
        if(args.length == 1 && args[0].equalsIgnoreCase("give"))
        {
            for(Player p : Bukkit.getOnlinePlayers())
            {
                if(p.getName().toLowerCase().contains(args[0].toLowerCase()))
                {
                    completes.add(p.getName());
                }
            }
        }
        
        
        if(args.length == 2 && args[0].equalsIgnoreCase("give"))
        {
            completes.addAll(plugin.getBombs().keySet().stream().map(String::valueOf).toList());
        }
        
        return completes;
    }
}
