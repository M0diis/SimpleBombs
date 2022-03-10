package me.m0dii.bombs;

import me.m0dii.bombs.utils.BombType;
import me.m0dii.bombs.utils.Utils;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bomb
{
    private final int id;
    
    private final String permission;
    
    private String name;
    private final Material material;
    private final List<String> lore;
    
    private int radius;
    private int fortune;
    private int time;
    private double throwStrength;
    
    private String hologramText;
    
    private Particle effect;
    private boolean destroyLiquids;
    private int entityDamage;
    private boolean glowing;
    
    private BombType type;
    
    private final Map<String, String> customProperties = new HashMap<>();
    
    public Bomb(int id, String name, Material material, List<String> lore, double throwStrength, int radius, int fortune,
                int time,
                String permission)
    {
        this.id = id;
        
        this.name = name;
        this.material = material;
        this.lore = lore;
        
        this.permission = permission;
        
        this.radius = radius;
        this.fortune = fortune;
        this.time = time;
        this.throwStrength = throwStrength;
        
        this.type = BombType.BASIC;
    }
    
    public Bomb copy()
    {
        Bomb copy = new Bomb(id, name, material, new ArrayList<>(lore), throwStrength, radius, fortune, time, permission);
        copy.setHologramText(hologramText);
        copy.setEffect(effect.name());
        copy.setDestroyLiquids(destroyLiquids);
        copy.setDamage(entityDamage);
        copy.setGlowing(glowing);
        copy.setBombType(type);
        return copy;
    }
    
    public void setHologramText(String text)
    {
        this.hologramText = text;
    }
    
    public void addProperty(String key, String value)
    {
        customProperties.put(key, value);
    }
    
    public String getProperty(String key)
    {
        return customProperties.get(key);
    }
    
    public void setEffect(String name)
    {
        this.effect = Particle.valueOf(name);
    }
    
    public int getId()
    {
        return id;
    }
    
    public String getName()
    {
        return name;
    }
    
    public Material getMaterial()
    {
        return material;
    }
    
    public List<String> getLore()
    {
        return lore;
    }
    
    public ItemStack getItemStack()
    {
        ItemStack item = new ItemStack(material);
        
        ItemMeta meta = item.getItemMeta();
        
        if(meta != null)
        {
            meta.setDisplayName(Utils.format(name));
            
            List<String> newLore = new ArrayList<>();
            
            for(String s : lore)
            {
                String newLine = Utils.format(s);
                
                newLine = newLine
                        .replaceAll("%radius%", String.valueOf(radius))
                        .replaceAll("%damage%", String.valueOf(entityDamage))
                        .replaceAll("%time%", String.valueOf(time))
                        .replaceAll("%throw_strength%", String.valueOf(throwStrength))
                        .replaceAll("%fortune%", String.valueOf(fortune));
                
                newLore.add(newLine);
            }
            
            if(glowing)
            {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }
            
            meta.setLore(newLore);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public int getRadius()
    {
        return radius;
    }
    
    public int getTime()
    {
        return time;
    }
    
    public int getFortune()
    {
        return fortune;
    }
    
    public Particle getEffect()
    {
        return effect;
    }
    
    public String getPermission()
    {
        return permission;
    }
    
    public String getHologramText()
    {
        return hologramText;
    }
    
    public double getThrowStrength()
    {
        return throwStrength;
    }
    
    public void setDestroyLiquids(boolean destroyLiquids)
    {
        this.destroyLiquids = destroyLiquids;
    }
    
    public boolean doDestroyLiquids()
    {
        return destroyLiquids;
    }
    
    public void setDamage(int entityDamage)
    {
        this.entityDamage = entityDamage;
    }
    
    public int getEntityDamage()
    {
        return entityDamage;
    }
    
    public void setGlowing(boolean glowing)
    {
        this.glowing = glowing;
    }
    
    public void setBombType(BombType type)
    {
        this.type = type;
    }
    
    public BombType getBombType()
    {
        return type;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
}
