package com.guntherdw.bukkit.SnowBalls;

import org.bukkit.Material;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.InventoryListener;
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
            event.setBurnTime(200);
        } else if(event.getFuel().getType() == Material.REDSTONE) {
            event.setBurnTime(3200);
        } else if(event.getFuel().getType() == Material.COAL) {
            event.setBurnTime(0);
        }

        plugin.getServer().broadcastMessage("fuel:"+event.getFuel());
        plugin.getServer().broadcastMessage("fueltime:"+event.getBurnTime());

    }

}
