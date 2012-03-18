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
