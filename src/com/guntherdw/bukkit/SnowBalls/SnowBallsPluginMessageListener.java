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

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

/**
 * @author GuntherDW
 */
public class SnowBallsPluginMessageListener implements PluginMessageListener {
    
    private SnowBalls plugin;
    
    public SnowBallsPluginMessageListener(SnowBalls instance) {
        this.plugin = instance;
    }
    
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if(message.length>0) {
            if(message[0] == 26) {
                plugin.sendRecipes(player);
            }
        }
    }
}
