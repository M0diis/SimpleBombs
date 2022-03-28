package me.m0dii.bombs.bomb;

import org.bukkit.entity.Player;

public class BombTime
{
    private final int id;
    private double time;
    
    Player p = null;
    
    public BombTime(int id, double time)
    {
        this.id = id;
        this.time = time;
    }
    
    public void setPlayer(Player p)
    {
        this.p = p;
    }
    
    public Player getPlayer()
    {
        return p;
    }
    
    public int getID()
    {
        return id;
    }
    
    public double getTime()
    {
        return time;
    }
    
    public void setTime(double time)
    {
        this.time = time;
    }
}
