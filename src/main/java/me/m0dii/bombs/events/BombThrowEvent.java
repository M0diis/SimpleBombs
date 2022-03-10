package me.m0dii.bombs.events;

import me.m0dii.bombs.Bomb;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class BombThrowEvent extends Event implements Cancellable
{
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancelled;
    
    private final Bomb bomb;
    private final Player player;
    private final ItemStack item;
    
    public BombThrowEvent(Player player, Bomb bomb, ItemStack item)
    {
        this.player = player;
        this.bomb = bomb;
        this.item = item;
    }
    
    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancel)
    {
        this.cancelled = cancel;
    }
    
    @Override
    public HandlerList getHandlers()
    {
        return HANDLER_LIST;
    }
    
    public static HandlerList getHandlerList()
    {
        return HANDLER_LIST;
    }
    
    public Bomb getBomb()
    {
        return bomb;
    }
    
    public Player getPlayer()
    {
        return player;
    }
    
    public ItemStack getItem()
    {
        return item;
    }
}
