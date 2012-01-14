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
