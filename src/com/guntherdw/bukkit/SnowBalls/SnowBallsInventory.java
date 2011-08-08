package com.guntherdw.bukkit.SnowBalls;

import org.bukkit.Material;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryListener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Furnace;

import java.util.Random;

/**
 * @author GuntherDW
 */
public class SnowBallsInventory extends InventoryListener {

    private SnowBalls plugin;
    private Random rnd = new Random();

    public SnowBallsInventory(SnowBalls instance)
    {
        this.plugin = instance;
    }

    public void onFurnaceBurn(FurnaceBurnEvent event) {
        if(event.isCancelled()) return;

        if(event.getFuel() == null || event.getFuel().getTypeId() == 0) {
            // event.setFuelBurnable(true);
            /* event.getFuel().setTypeId(Material.STICK.getId());
            event.getFuel().setAmount(64);
            event.setBurnTime(100); */
            org.bukkit.block.Furnace f = (org.bukkit.block.Furnace) event.getFurnace().getState();
            f.getInventory().setItem(1, new ItemStack(Material.STICK, 64));
        } else if(event.getFuel().getType() == Material.REDSTONE) {
            event.setBurnTime(3200);
        } else if(event.getFuel().getType() == Material.COAL) {
            event.setBurnTime(0);
        }

        plugin.getServer().broadcastMessage("fuel:"+event.getFuel());
        plugin.getServer().broadcastMessage("fueltime:"+event.getBurnTime());

    }

    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        if(event.isCancelled()) return;

        plugin.getServer().broadcastMessage("s:"+event.getSource());
        plugin.getServer().broadcastMessage("r:"+event.getResult());
    }

}
