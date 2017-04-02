/*
 * This file is part of Expvp,
 * Copyright (c) 2016-2017.
 *
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt.
 */

package me.minotopia.expvp.score;

import com.google.inject.Inject;
import me.minotopia.expvp.api.score.KillDeathService;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Forwards kills and deaths to KillDeathService.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2017-04-02
 */
public class KillDeathForwardingListener implements Listener {
    private final KillDeathService killDeathService;

    @Inject
    public KillDeathForwardingListener(KillDeathService killDeathService) {
        this.killDeathService = killDeathService;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (isTheVictimAPlayer(event)) {
            Player victim = (Player) event.getEntity();
            if (isFatalHit(event, victim)) {
                handleFatalHit(event, victim);
            }
        }
    }

    private void handleFatalHit(EntityDamageByEntityEvent event, Player victim) {
        cancelAndTeleportToSpawn(event, victim);
        if (isTheCulpritAPlayer(event)) {
            Player culprit = (Player) event.getDamager();
            killDeathService.onFatalHit(culprit, victim);
        }
    }

    private void cancelAndTeleportToSpawn(EntityDamageByEntityEvent event, Player victim) {
        event.setCancelled(true);
        victim.sendMessage("This is the point where you'd be teleported to spawn if that was already implemented.");
        //FIXME: Teleport to spawn
    }

    private boolean isFatalHit(EntityDamageByEntityEvent event, Player victim) {
        return (victim.getHealth() - event.getFinalDamage()) <= 0D;
    }

    private boolean isTheCulpritAPlayer(EntityDamageByEntityEvent event) {
        return event.getDamager().getType() != EntityType.PLAYER;
    }

    private boolean isTheVictimAPlayer(EntityDamageByEntityEvent event) {
        return event.getEntityType() == EntityType.PLAYER;
    }
}
