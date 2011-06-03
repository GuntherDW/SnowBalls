package com.guntherdw.bukkit.SnowBalls;

import com.sun.deploy.util.VersionID;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * @author GuntherDW
 */
public class SnowBalls extends JavaPlugin {

    private final SnowBallsBlock blockListener = new SnowBallsBlock(this);
    protected static final Logger log = Logger.getLogger("Minecraft");
    protected static ShapelessRecipe[] woolRecipe = new ShapelessRecipe[15];
    protected static ShapelessRecipe   sandstoneRecipe;

    public void onDisable() {
        //To change body of implemented methods use File | Settings | File Templates.
        log.info("[SnowBalls] SnowBalls version 1.0 shutting down!");
    }

    public void addRecipes() {
        for(int i=1; i<16; i++)
        {
            woolRecipe[i-1] = new ShapelessRecipe(new ItemStack(Material.WOOL, 1, (byte)0));
            woolRecipe[i-1].addIngredient(Material.WOOL, i);
            woolRecipe[i-1].addIngredient(Material.INK_SACK, (byte) 15);
            this.getServer().addRecipe(woolRecipe[i-1]);
        }

        sandstoneRecipe = new ShapelessRecipe(new ItemStack(Material.SAND, 4, (byte) 0));
        sandstoneRecipe.addIngredient(Material.SANDSTONE);
        this.getServer().addRecipe(sandstoneRecipe);
        /* woolRecipe = new ShapelessRecipe(new ItemStack(Material.WOOL, 1));
        woolRecipe.addIngredient(Material.WOOL);
        woolRecipe.addIngredient(Material.INK_SACK, (byte) 15);
        this.getServer().addRecipe(woolRecipe); */

    }

    public void onEnable() {
        log.info("[SnowBalls] SnowBalls version 1.0 loaded!");
        this.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Monitor, this);
        this.getServer().getPluginManager().registerEvent(Event.Type.LEAVES_DECAY, blockListener, Event.Priority.Monitor, this);

        this.addRecipes();
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
