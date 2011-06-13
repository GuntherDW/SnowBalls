package com.guntherdw.bukkit.SnowBalls;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author GuntherDW
 */
public class JsonHelper {

    private SnowBalls plugin;

    public JsonHelper(SnowBalls instance) {
        this.plugin = instance;
    }

    public boolean teleportTo(String pstring, Integer x, Integer y, Integer z, String world) {
        List<Player> plist = plugin.getServer().matchPlayer(pstring);
        Player player = null;
        if(plist.size()!=1)
            return false;
        player = plist.get(0);
        World w = null;
        if(player==null) {
            return false;
        }
        if(world == null || world.equals("")) {
            w = player.getWorld();
        } else {
            w = plugin.getServer().getWorld(world);
            if(w == null) w = player.getWorld();
        }
        if(plugin.getTcutils()!=null) {
            plugin.getTcutils().getTelehistory().addHistory(player.getName(), player.getLocation());
        }
        Location loc = new Location(w, x, y, z);
        return player.teleport(loc);
    }

}
