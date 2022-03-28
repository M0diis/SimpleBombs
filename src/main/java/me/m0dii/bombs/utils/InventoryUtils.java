package me.m0dii.bombs.utils;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.m0dii.bombs.SimpleBombs;
import me.m0dii.bombs.bomb.Bomb;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class InventoryUtils
{
    private static final SimpleBombs plugin = SimpleBombs.getInstance();
    
    public static PaginatedGui getPaginatedGui()
    {
        PaginatedGui gui = Gui.paginated()
                .title(Component.text(Utils.format("&8&lSimpleBombs List")))
                .rows(6)
                .pageSize(45)
                .create();
    
        gui.setItem(6, 3, ItemBuilder.from(Material.PAPER).setName(Utils.format("&aPrevious")).asGuiItem(e -> {
            gui.previous();
            e.setCancelled(true);
        }));
        
        gui.setItem(6, 7, ItemBuilder.from(Material.PAPER).setName(Utils.format("&aNext")).asGuiItem(e -> {
            gui.next();
            e.setCancelled(true);
        }));
    
        for(Bomb bomb : plugin.getCfg().getBombs().values())
        {
            Component name = Component.text(Utils.format(bomb.getName(true) + " &8[&7" + bomb.getId() + "&8]"));
        
            ItemBuilder builder = ItemBuilder.from(bomb.getMaterial()).name(name).setLore(bomb.getLore(true));
        
            if(bomb.isGlowing()) builder.glow();
        
            gui.addItem(builder.asGuiItem(e ->
            {
                if(e.getWhoClicked() instanceof Player)
                {
                    Player clickee = (Player) e.getWhoClicked();
                
                    String itemName = Utils.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
                
                    int id = Integer.parseInt(itemName.charAt(itemName.length() - 2) + "");
                
                    Bomb b = plugin.getCfg().getBomb(id);
                
                    if(b != null)
                    {
                        clickee.getInventory().addItem(b.getItemStack());
                    }
                }
            
                e.setCancelled(true);
            }));
        }
        
        return gui;
    }
}
