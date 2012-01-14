package com.guntherdw.bukkit.SnowBalls;

import com.guntherdw.bukkit.tweakcraft.TweakcraftUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author GuntherDW
 */
public class JsonHelper {

    private SnowBalls plugin;
    private TweakcraftUtils tcutils = null;

    public JsonHelper(SnowBalls instance) {
        this.plugin = instance;
        // this.tcutils = TweakcraftUtils.getInstance();
    }

    public boolean teleportTo(String pstring, Integer x, Integer y, Integer z, String world) {
        if(tcutils==null) tcutils = TweakcraftUtils.getInstance();
        Player player = plugin.getServer().getPlayerExact(pstring);

        World w = null;
        if (player == null) return false;

        if (world == null || world.equals("")) {
            w = player.getWorld();
        } else {
            w = plugin.getServer().getWorld(world);
            if (w == null) w = player.getWorld();
        }
        if (tcutils != null) {
            tcutils.getTelehistory().addHistory(player.getName(), player.getLocation());
        }
        Location loc = new Location(w, x, y, z);
        return player.teleport(loc);
    }

}
