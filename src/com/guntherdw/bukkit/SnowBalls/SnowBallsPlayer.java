package com.guntherdw.bukkit.SnowBalls;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;

/**
 * @author GuntherDW
 */
public class SnowBallsPlayer extends PlayerListener {

    private SnowBalls plugin;

    public SnowBallsPlayer(SnowBalls instance) {
        this.plugin = instance;
    }

    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendRawMessage(plugin.CUIPattern);
    }
}
