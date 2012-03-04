package com.guntherdw.bukkit.SnowBalls;

import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * @author GuntherDW
 */
public class SnowBallsPlayer implements Listener {

    private SnowBalls plugin;

    public SnowBallsPlayer(SnowBalls instance) {
        this.plugin = instance;
    }

    /* public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendRawMessage(plugin.CUIPattern);
    } */
}
