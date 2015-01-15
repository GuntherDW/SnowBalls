/*
 * Copyright (c) 2015 GuntherDW
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License.
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

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * @author guntherdw
 */
public class SearchCommand implements CommandExecutor {

    private SnowBalls plugin;

    public SearchCommand(SnowBalls instance) {
        this.plugin = instance;
    }

    public boolean doesInventoryHaveItem(Inventory inventory, ItemStack itemStack) {
        for (ItemStack is : inventory.getContents()) {
            if (is != null && is.isSimilar(itemStack))
                return true;
        }
        return false;
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Why exactly do you need this as a console?");
            return true;
        }
        Player player = (Player) commandSender;
        if (!player.hasPermission("snowballs.searchinventories")) {
            commandSender.sendMessage(ChatColor.RED + "You don't have the correct permission to search nearby inventories");
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.GOLD + "Give me an item to look for!");
            return true;
        }
        int range = Math.min(args.length == 2 ? Integer.parseInt(args[1]) : plugin.defaultSearchRange, plugin.maximumSearchRange);

        player.sendMessage(ChatColor.GOLD + "Going to look for " + args[0] + " in a range of " + range + " blocks!");
        Location loc = player.getLocation().clone();

        ItemStack toSearch = null;
        String[] parts = args[0].split(":");


        try { // Is it an ItemID?
            Integer i = Integer.parseInt(parts[0]);
            Integer i2 = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;

            toSearch = new ItemStack(i, 1, i2.shortValue());

        } catch (NumberFormatException e) { // It isn't, search as a string!
            // Does it include a ':'?
            String owner = "minecraft";
            String itemName;
            Integer data;

            itemName = parts[0];
            data = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;

            // Try as a Material
            Material m = Material.valueOf(itemName.toUpperCase());
            if (m != null) {
                toSearch = new ItemStack(m, 1, data.shortValue());
            }
        }

        if (toSearch == null) {
            player.sendMessage(ChatColor.GOLD + "Didn't find any item assigned with your search value.");
            return true;
        }

        Set<Location> foundInventories = new HashSet<Location>();

        for (int x = loc.getBlockX() - range; x < loc.getBlockX() + range; x++) {
            for (int y = loc.getBlockY() - range; y < loc.getBlockY() + range; y++) {
                for (int z = loc.getBlockZ() - range; z < loc.getBlockZ() + range; z++) {
                    if (y > 0 && y < loc.getWorld().getMaxHeight()) {
                        Block b = loc.getWorld().getBlockAt(x, y, z);
                        if(b!=null) {
                            BlockState blockState = b.getState();
                            if (blockState instanceof InventoryHolder) {
                                if (plugin.useZones && !plugin.getZonesHelper().checkPermission(player, b)) continue;
                                InventoryHolder inv = (InventoryHolder) blockState;
                                if (doesInventoryHaveItem(inv.getInventory(), toSearch)) {
                                    foundInventories.add(b.getLocation().clone());
                                }
                            }
                        }
                    }
                }
            }
        }

        if (foundInventories.size() > 0) {
            player.sendMessage(ChatColor.GOLD + "Found your item on these locations");

            if (player.getListeningPluginChannels().contains(plugin.pluginMessageChannel)) {

                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                buf.write((byte) 20);

                for (Location location : foundInventories) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(location.getBlockX()).append(",")
                      .append(location.getBlockY()).append(",")
                      .append(location.getBlockZ());
                    try {
                        buf.write(sb.toString().getBytes("UTF-8"));
                        buf.write((byte) 0);
                    } catch (Exception ex) { }
                }

                player.sendPluginMessage(plugin, plugin.pluginMessageChannel, buf.toByteArray());
            } else {
                for(Location location : foundInventories) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(location.getBlockX()).append(",")
                      .append(location.getBlockY()).append(",")
                      .append(location.getBlockZ());

                    player.sendMessage(ChatColor.YELLOW+" "+sb.toString());
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Didn't find any item you were looking for nearby!");
        }

        return true;
    }
}