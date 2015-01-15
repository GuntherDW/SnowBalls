/*
 * Copyright (c) 2015 GuntherDW
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

import com.zones.Zones;
import com.zones.persistence.Zone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * @author guntherdw
 */
public class ZonesHelper {

    private SnowBalls plugin;
    private Zones zonesPlugin;

    public ZonesHelper(SnowBalls plugin) {
        this.plugin = plugin;
    }

    public boolean checkForZones() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Zones");
        if(plugin != null && plugin instanceof Zones){
            zonesPlugin = (Zones) plugin;
            return true;
        }
        return false;
    }

    public boolean checkPermission(Player player, Block block) {
        if(!plugin.useZones) return true;
        return zonesPlugin.getApi().canModify(player, block);
    }

}
