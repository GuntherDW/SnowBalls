package com.guntherdw.bukkit.SnowBalls;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Random;

/**
 * @author GuntherDW
 */
public class SnowBallsBlock extends BlockListener {

    private SnowBalls plugin;
    private Random    rnd = new Random();
    private int[] lijp = new int[20];

    public SnowBallsBlock(SnowBalls instance)
    {
        this.plugin = instance;
    }

    public void onBlockBreak(BlockBreakEvent event)
    {
        if(event.getBlock().getTypeId() == 79 // Material.ICE
                && !event.isCancelled()) // ice
        {
            // String worldname = event.getPlayer().getLocation().getWorld().getName();
            Location loc = event.getBlock().getLocation();
            loc.setY(loc.getY() + 1);
            loc.getWorld().dropItem(loc, new ItemStack(79, 1));

        } else if(event.getBlock().getTypeId() ==  18 // Material.LEAVES.getId()
                && !event.isCancelled()
                && event.getBlock().getData() == ((byte) 2)) { // birch trees
            if(rnd.nextInt(1000) == 997) // low chance!
            {
                plugin.log.info("[SnowBalls] BONUS for "+event.getPlayer().getName()+" @ x:"
                        +event.getBlock().getX()+" y:"+event.getBlock().getY()+" z: "+event.getBlock().getZ());
                Location loc = event.getBlock().getLocation();
                loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.INK_SACK, 1, (short) 3));
                // INK SAC, data(3) == cacao beans!
            }
        } else if(event.getBlock().getTypeId() == 47 && !event.isCancelled()) { // Material.BOOKSHELF) {
            Location loc = event.getBlock().getLocation();
            loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.BOOKSHELF,1));
        } /* else if(event.getBlock().getTypeId() == 89 && !event.isCancelled()) { // Material.GLOWSTONE.getId()) {
            Location loc = event.getBlock().getLocation();
            for(int i=1; i<=8;i++)
                loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.GLOWSTONE_DUST, 1));
        } */
    }

    public void onLeavesDecay(LeavesDecayEvent event) {
        if(event.isCancelled())
            return;
        int data = (event.getBlock().getData()&2);
        if(data == ((byte) 2)) { // birch trees
            // System.out.println("It is birch!");
            if(rnd.nextInt(1000) == 997) // low chance!
            {
                plugin.log.info("[SnowBalls] Leaf decay BONUS @ x:"
                        +event.getBlock().getX()+" y:"+event.getBlock().getY()+" z: "+event.getBlock().getZ());
                Location loc = event.getBlock().getLocation();
                loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.INK_SACK, 1, (short) 3));
                // INK SAC, data(3) == cacao beans!
            }
        }
    }
}
