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

import com.guntherdw.bukkit.tweakcraft.Packages.Item;
import com.guntherdw.bukkit.tweakcraft.Packages.ItemDB;
import com.guntherdw.bukkit.tweakcraft.Util.TeleportHistory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.guntherdw.bukkit.tweakcraft.TweakcraftUtils;

public class TweakcraftUtilsHelper {

    private SnowBalls plugin;
    private TweakcraftUtils tcUtils;

    public TweakcraftUtilsHelper(SnowBalls plugin) {
        this.plugin = plugin;
    }

    public boolean checkForTweakcraftUtils() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("TweakcraftUtils");
        if (plugin != null && plugin instanceof TweakcraftUtils) {
            tcUtils = (TweakcraftUtils) plugin;
            return true;
        }
        return false;
    }

    public void addTeleportHistory(Player player) {
        this.addTeleportHistory(player, player.getLocation());
    }

    public void addTeleportHistory(Player player, Location loc) {
        if(!plugin.useTCUtils) return;

        TeleportHistory teleportHistory = tcUtils.getTelehistory();
        teleportHistory.addHistory(player.getName(), loc);
    }

    public ItemStack searchItem(String itemName) {
        if(!plugin.useTCUtils) return null;

        ItemDB itemDB = tcUtils.getItemDB();
        Item item = itemDB.getItem(itemName);

        if(item != null) {
            return new ItemStack(item.getItemnumber(), 1, item.getDamage());
        }

        return null;
    }
}
