package me.m0dii.bombs.bomb;

import me.clip.placeholderapi.PlaceholderAPI;
import me.m0dii.bombs.utils.Utils;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Bomb
{
    public static class Builder
    {
        private int id;
    
        private String permission;
    
        private String name;
        private Material material;
        private List<String> lore;
    
        private int radius;
        private int fortune;
        private int time;
        private double throwStrength;
    
        private String hologramText;
    
        private Particle effect;
        private Sound throwSound = null;
        private Sound explodeSound = null;
    
        private boolean autoSell = false;
        private boolean destroyBlocks = true;
        private boolean destroyLiquids;
        private int entityDamage;
        private boolean glowing;
    
        private BombType type;
    
        private final Map<String, String> customProperties = new HashMap<>();
        private final List<String> checkedBlocks = new ArrayList<>();
        private final List<String> smeltBlocks = new ArrayList<>();
    
        private boolean ignorePerm;
        private boolean sendMessage;
    
        private boolean destroyIsWhitelist = true;
        private boolean smeltIsWhitelist = true;
        private boolean smeltIsEnabled = false;
    
        public Builder setId(int id)
        {
            this.id = id;
            return this;
        }
        
        public Builder setGlowing(boolean glowing)
        {
            this.glowing = glowing;
            return this;
        }
    
        public Builder setPermission(String permission)
        {
            this.permission = permission;
            return this;
        }
    
        public Builder setName(String name)
        {
            this.name = name;
            return this;
        }
    
        public Builder setMaterial(Material material)
        {
            this.material = material;
            return this;
        }
    
        public Builder setLore(List<String> lore)
        {
            this.lore = lore;
            return this;
        }
    
        public Builder setRadius(int radius)
        {
            this.radius = radius;
            return this;
        }
    
        public Builder setFortune(int fortune)
        {
            this.fortune = fortune;
            return this;
        }
    
        public Builder setTime(int time)
        {
            this.time = time;
            return this;
        }
    
        public Builder setThrowStrength(double throwStrength)
        {
            this.throwStrength = throwStrength;
            return this;
        }
    
        public Builder setHologramText(String hologramText)
        {
            this.hologramText = hologramText;
            return this;
        }
    
        public Builder setEffect(Particle effect)
        {
            this.effect = effect;
            return this;
        }
    
        public Builder setEffect(String effect)
        {
            this.effect = Particle.valueOf(effect.toUpperCase());
            return this;
        }
    
        public Builder setThrowSound(Sound throwSound)
        {
            this.throwSound = throwSound;
            return this;
        }
    
        public Builder setThrowSound(String throwSound)
        {
            this.throwSound = Sound.valueOf(throwSound.toUpperCase());
            return this;
        }
    
        public Builder setExplodeSound(Sound explodeSound)
        {
            this.explodeSound = explodeSound;
            return this;
        }
        
        public Builder setExplodeSound(String explodeSound)
        {
            this.explodeSound = Sound.valueOf(explodeSound.toUpperCase());
            return this;
        }
    
        public Builder setAutoSell(boolean autoSell)
        {
            this.autoSell = autoSell;
            return this;
        }
    
        public Builder setDestroyBlocks(boolean destroyBlocks)
        {
            this.destroyBlocks = destroyBlocks;
            return this;
        }
    
        public Builder setDestroyLiquids(boolean destroyLiquids)
        {
            this.destroyLiquids = destroyLiquids;
            return this;
        }
        
        public Builder setEntityDamage(int entityDamage)
        {
            this.entityDamage = entityDamage;
            return this;
        }
        
        public Builder setIgnorePerm(boolean ignorePerm)
        {
            this.ignorePerm = ignorePerm;
            return this;
        }
        
        public Builder setSendMessage(boolean sendMessage)
        {
            this.sendMessage = sendMessage;
            return this;
        }
        
        public Builder setDestroyIsWhitelist(boolean destroyIsWhitelist)
        {
            this.destroyIsWhitelist = destroyIsWhitelist;
            return this;
        }
        
        public Builder setSmeltIsWhitelist(boolean smeltIsWhitelist)
        {
            this.smeltIsWhitelist = smeltIsWhitelist;
            return this;
        }
        
        public Builder setSmeltIsEnabled(boolean smeltIsEnabled)
        {
            this.smeltIsEnabled = smeltIsEnabled;
            return this;
        }
    
        public Bomb build()
        {
            return new Bomb(this);
        }
    }
    
    public Bomb(Builder builder)
    {
        this.id = builder.id;
        this.permission = builder.permission;
        this.name = builder.name;
        this.material = builder.material;
        this.lore = builder.lore;
        this.radius = builder.radius;
        this.fortune = builder.fortune;
        this.time = builder.time;
        this.throwStrength = builder.throwStrength;
        this.hologramText = builder.hologramText;
        this.effect = builder.effect;
        this.throwSound = builder.throwSound;
        this.explodeSound = builder.explodeSound;
        this.autoSell = builder.autoSell;
        this.destroyBlocks = builder.destroyBlocks;
        this.destroyLiquids = builder.destroyLiquids;
        this.entityDamage = builder.entityDamage;
        this.glowing = builder.glowing;
    }
    
    private final int id;
    
    private final String permission;
    
    private String name;
    private final Material material;
    private final List<String> lore;
    
    private final int radius;
    private final int fortune;
    private final int time;
    private final double throwStrength;
    
    private String hologramText;

    private Particle effect;
    private Sound throwSound = null;
    private Sound explodeSound = null;
    
    private boolean autoSell = false;
    private boolean destroyBlocks = true;
    private boolean destroyLiquids;
    private int entityDamage;
    private boolean glowing;
    
    private BombType type = BombType.BASIC;
    
    private Map<String, String> customProperties = new HashMap<>();
    private List<String> checkedBlocks = new ArrayList<>();
    private List<String> smeltBlocks = new ArrayList<>();

    private boolean ignorePerm;
    private boolean sendMessage;
    
    private boolean destroyIsWhitelist = true;
    private boolean smeltIsWhitelist = true;
    private boolean smeltIsEnabled = false;
    
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
        
        this.destroyIsWhitelist = false;
    }
    
    public void setDestroyIsWhitelist(boolean whitelist)
    {
        this.destroyIsWhitelist = whitelist;
    }

    public void setSmeltEnabled(boolean smelt)
    {
        this.smeltIsEnabled = smelt;
    }
    
    public boolean isSmeltEnabled()
    {
        return smeltIsEnabled;
    }

    public boolean destroyIsWhitelist()
    {
        return destroyIsWhitelist;
    }
    
    public void setSmeltIsWhitelist(boolean whitelist)
    {
        this.smeltIsWhitelist = whitelist;
    }
    
    public boolean smeltIsWhitelist()
    {
        return smeltIsWhitelist;
    }

    public boolean containsSmeltBlock(String block)
    {
        return smeltBlocks.contains(block);
    }
    
    public boolean containsCheckedBlock(String block)
    {
        return checkedBlocks.contains(block);
    }
    
    public boolean hasCheckedBlocks()
    {
        return !checkedBlocks.isEmpty();
    }
    
    public void setCheckedBlocks(List<String> blocks)
    {
        this.checkedBlocks.clear();
        this.checkedBlocks.addAll(blocks);
    }
    
    public void setSmeltBlocks(List<String> blocks)
    {
        this.smeltBlocks.clear();
        this.smeltBlocks.addAll(blocks);
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
        copy.setDestroyIsWhitelist(destroyIsWhitelist);
        copy.setCheckedBlocks(checkedBlocks);
        copy.setSendMessage(sendMessage);
        
        copy.explodeSound = explodeSound;
        copy.throwSound = throwSound;

        return copy;
    }
    
    public static Builder builder()
    {
        return new Builder();
    }

    public void setSendMessage(boolean sendMessage)
    {
        this.sendMessage = sendMessage;
    }
    
    public void setIgnorePerm(boolean ignorePerm)
    {
        this.ignorePerm = ignorePerm;
    }
    
    public boolean ignorePermission()
    {
        return ignorePerm;
    }
    
    public boolean doSendMessage()
    {
        return sendMessage;
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
    
    public void clearProperties()
    {
        customProperties.clear();
    }
    
    public void setEffect(String name)
    {
        this.effect = Particle.valueOf(name);
    }

    public void setExplodeSound(String name)
    {
        this.explodeSound = Sound.valueOf(name);
    }

    public void setThrowSound(String name)
    {
        this.throwSound = Sound.valueOf(name);
    }
    
    public int getId()
    {
        return id;
    }
    
    public String getName(boolean formatted)
    {
        if(formatted)
        {
            return Utils.format(Utils.setPlaceholders(name, null, this));
        }
        
        return name;
    }
    
    public Material getMaterial()
    {
        return material;
    }
    
    public List<String> getLore(boolean formatted)
    {
        if(formatted)
        {
            return lore.stream().map(Utils::format).map(s -> Utils.setPlaceholders(s, null, this)).collect(Collectors.toList());
        }
        
        return lore;
    }
    
    
    public ItemStack getItemStack()
    {
        ItemStack item = new ItemStack(material);
        
        ItemMeta meta = item.getItemMeta();
        
        if(meta != null)
        {
            meta.setDisplayName(Utils.format(Utils.setPlaceholders(name, null, this)));
            
            if(glowing)
            {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }
            
            meta.setLore(getLore(true));
            
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

    public Sound getExplodeSound()
    {
        return explodeSound;
    }

    public Sound getThrowSound()
    {
        return throwSound;
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
    
    public boolean isGlowing()
    {
        return glowing;
    }
    
    public void setDestroyBlocks(boolean destroyBlocks)
    {
        this.destroyBlocks = destroyBlocks;
    }
    
    public boolean doDestroyBlocks()
    {
        return destroyBlocks;
    }
    
    public void setAutoSell(boolean autoSell)
    {
        this.autoSell = autoSell;
    }
    
    public boolean doAutoSell()
    {
        return autoSell;
    }
    
    @Override
    public String toString()
    {
        return "Bomb{" +
                "material=" + material +
                ", name='" + name + '\'' +
                ", radius=" + radius +
                ", time=" + time +
                ", fortune=" + fortune +
                ", effect=" + effect +
                ", throwSound=" + throwSound +
                ", explodeSound=" + explodeSound +
                ", permission='" + permission + '\'' +
                ", hologramText='" + hologramText + '\'' +
                ", throwStrength=" + throwStrength +
                ", destroyBlocks=" + destroyBlocks +
                ", destroyLiquids=" + destroyLiquids +
                ", entityDamage=" + entityDamage +
                ", glowing=" + glowing +
                ", type=" + type +
                ", autoSell=" + autoSell +
                '}';
    }
}
