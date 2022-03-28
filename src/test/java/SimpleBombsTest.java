import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.m0dii.bombs.SimpleBombs;
import me.m0dii.bombs.utils.InventoryUtils;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleBombsTest
{
    private static ServerMock server;
    private static SimpleBombs plugin;
    private static WorldGuardPlugin wg;
    
    @BeforeAll
    static void setUp()
    {
        server = MockBukkit.mock();
        
        SimpleBombs.setTestMode(true);
        
        plugin = MockBukkit.load(SimpleBombs.class);
        
//        wg = MockBukkit.load(WorldGuardPlugin.class);
    }
    
    @AfterAll
    static void tearDown()
    {
        MockBukkit.unmock();
    }
    
    @Test
    public void testOnEnable()
    {
        plugin.onEnable();
    }
    
    @Test
    public void testOnCommand()
    {
        PlayerMock player = server.addPlayer();
        
        assert !player.hasPermission("simplebombs.use");
        assert !player.hasPermission("simplebombs.command.gui");
        
        player.setOp(true);
    
        assert player.hasPermission("simplebombs.use");
        assert player.hasPermission("simplebombs.command.gui");
        
        player.openInventory(InventoryUtils.getPaginatedGui().getInventory());
    
        assertInstanceOf(player.getOpenInventory().getTopInventory().getClass(), PaginatedGui.class);
    }
    
    @Test
    public void testOnDisable()
    {
        plugin.onDisable();
    }
    
}
