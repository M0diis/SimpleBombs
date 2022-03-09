package me.m0dii.bombs;

public class BombTime
{
    private final int id;
    private double time;
    
    public BombTime(int id, double time)
    {
        this.id = id;
        this.time = time;
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
