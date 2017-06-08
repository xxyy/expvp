/*
 * This file is part of Expvp,
 * Copyright (c) 2016-2017.
 *
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt.
 */

package me.minotopia.expvp.score.listener;

import com.google.inject.Inject;
import me.minotopia.expvp.api.score.points.TalentPointService;
import me.minotopia.expvp.api.score.points.TalentPointType;
import me.minotopia.expvp.api.score.service.ExpService;
import me.minotopia.expvp.api.service.PlayerDataService;
import me.minotopia.expvp.util.SessionProvider;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Listens for join events and does some initialisation work for the score module.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2017-04-07
 */
public class ScoreJoinListener implements Listener {
    private final ExpService exps;
    private final TalentPointService talentPoints;
    private final PlayerDataService players;
    private final SessionProvider sessionProvider;

    @Inject
    public ScoreJoinListener(ExpService exps, TalentPointService talentPoints,
                             PlayerDataService players, SessionProvider sessionProvider) {
        this.exps = exps;
        this.talentPoints = talentPoints;
        this.players = players;
        this.sessionProvider = sessionProvider;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        sessionProvider.inSession(ignored ->
                players.saveData(players.findOrCreateDataMutable(event.getUniqueId()))
        );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();
        sessionProvider.inSession(ignored -> {
            if (!player.hasPlayedBefore()) {
                exps.incrementExp(player, 100);
            } else {
                player.setLevel(exps.getExpCount(player));
            }
            talentPoints.grantDeservedTalentPoints(player, TalentPointType.DAY_ACTIVITY);
        });
    }
}