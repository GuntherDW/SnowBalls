/*
 * Copyright (c) 2012 GuntherDW
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.guntherdw.bukkit.SnowBalls;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * @author GuntherDW
 */
public class SnowBallsBlock implements Listener {

    private SnowBalls plugin;
    private Random    rnd = new Random();
    private int[] lijp = new int[20];

    public SnowBallsBlock(SnowBalls instance) {
        this.plugin = instance;
    }

    public boolean wasShears(ItemStack stack) {
        return stack!=null && stack.getTypeId() == Material.SHEARS.getId();
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event){
        if(!plugin.enableInfiniteLava) return;
        if(event.isCancelled())        return;

        if(event.getBlock().getTypeId() == Material.STATIONARY_LAVA.getId()
                && event.getToBlock().getTypeId() == Material.AIR.getId()) {
            Block start = event.getToBlock();
            Block toCheck;
            BlockFace[] faces = {BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH};
            for(int dx = -1; dx<=1; dx++){
                for(int dz = -1; dz<=1; dz++){

                    if(dz == 0 && dx == 0) {
                        continue;
                    }
                    toCheck = start.getRelative(dx,0,dz);
                    if(toCheck.getTypeId() != Material.STATIONARY_LAVA.getId()) {
                        boolean checked = false;
                        for(BlockFace bf : faces) {
                            if(toCheck.getRelative(bf).equals(start) && toCheck.getType() == Material.LAVA)
                                checked = true;
                        }
                        if(!checked) return;
                    }
                }
            }
            event.getToBlock().setType(Material.STATIONARY_LAVA);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        if(event.isCancelled()) return;

        if(event.getBlock().getTypeId() == 79 // Material.ICE
                && plugin.icedrop) // ice
        {
            // String worldname = event.getPlayer().getLocation().getWorld().getName();
            Location loc = event.getBlock().getLocation();
            loc.setY(loc.getY() + 1);
            loc.getWorld().dropItem(loc, new ItemStack(79, 1));

        } else if(event.getBlock().getTypeId() ==  18 // Material.LEAVES.getId()
                && plugin.leavesLoot
                && event.getBlock().getData() == ((byte) 2)
                && !wasShears(event.getPlayer().getItemInHand())) { // birch trees
            if(rnd.nextInt(1000) == 997) // low chance!
            {
                plugin.log.info("[SnowBalls] BONUS for "+event.getPlayer().getName()+" @ x:"
                        +event.getBlock().getX()+" y:"+event.getBlock().getY()+" z: "+event.getBlock().getZ());
                Location loc = event.getBlock().getLocation();
                loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.INK_SACK, 1, (short) 3));
                // INK SAC, data(3) == cacao beans!
            }
        } else if(event.getBlock().getTypeId() == 47
                && plugin.bookshelvesdrop) { // Material.BOOKSHELF) {
            Location loc = event.getBlock().getLocation();
            loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.BOOKSHELF,1));
        }
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        if(!plugin.leavesLoot)  return;
        if(event.isCancelled()) return;
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
