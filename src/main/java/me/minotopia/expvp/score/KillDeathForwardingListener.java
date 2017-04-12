/*
 * This file is part of Expvp,
 * Copyright (c) 2016-2017.
 *
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt.
 */

package me.minotopia.expvp.score;

import com.google.inject.Inject;
import li.l1t.common.util.DamageHelper;
import me.minotopia.expvp.api.respawn.RespawnService;
import me.minotopia.expvp.api.score.KillDeathService;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;

/**
 * Forwards kills and deaths to KillDeathService.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2017-04-02
 */
public class KillDeathForwardingListener implements Listener {
    private final KillDeathService killDeathService;
    private final RespawnService respawnService;

    @Inject
    public KillDeathForwardingListener(KillDeathService killDeathService, RespawnService respawnService) {
        this.killDeathService = killDeathService;
        this.respawnService = respawnService;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerHit(EntityDamageEvent event) {
        if (isTheVictimAPlayer(event)) {
            Player victim = (Player) event.getEntity();
            if (isFatalHit(event, victim)) {
                event.setCancelled(true);
                handleFatalHit(victim, event);
            }
        }
    }

    private boolean isTheVictimAPlayer(EntityDamageEvent event) {
        return event.getEntityType() == EntityType.PLAYER;
    }

    private boolean isFatalHit(EntityDamageEvent event, Player victim) {
        return (victim.getHealth() - event.getFinalDamage()) <= 0D;
    }

    private void handleFatalHit(Player victim, EntityDamageEvent event) {
        teleportToSpawn(victim);
        restoreHealthEtc(victim);
        DamageHelper.findActualDamager(event)
                .ifPresent(culprit -> killDeathService.onFatalHit(culprit, victim));
    }

    private void teleportToSpawn(Player victim) {
        respawnService.startRespawnDelay(victim);
    }

    private void restoreHealthEtc(Player victim) {
        victim.setHealth(victim.getMaxHealth());
        victim.setFoodLevel(20);
        victim.setSaturation(20);
        victim.getActivePotionEffects().stream()
                .map(PotionEffect::getType).forEach(victim::removePotionEffect);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.getEntity().spigot().respawn();
        handleFatalHit(event.getEntity(), event.getEntity().getLastDamageCause());
    }
}
