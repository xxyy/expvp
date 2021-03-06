/*
 * Expvp Minecraft game mode
 * Copyright (C) 2016-2017 Philipp Nowak (https://github.com/xxyy)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.minotopia.expvp.respawn;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import li.l1t.common.i18n.Message;
import li.l1t.common.util.task.TaskService;
import me.minotopia.expvp.api.handler.kit.KitService;
import me.minotopia.expvp.api.respawn.RespawnService;
import me.minotopia.expvp.api.score.points.TalentPointService;
import me.minotopia.expvp.api.spawn.SpawnService;
import me.minotopia.expvp.i18n.Format;
import me.minotopia.expvp.i18n.I18n;
import me.minotopia.expvp.score.league.LeagueChangeDisplayQueue;
import me.minotopia.expvp.ui.menu.SelectTreeMenu;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;

/**
 * Creates special effects on respawn using packets.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2017-04-06
 */
@Singleton
public class SpectatorRespawnService implements RespawnService {
    private final TaskService tasks;
    private final SelectTreeMenu.Factory treeMenuFactory;
    private final KitService kitService;
    private final TalentPointService talentPoints;
    private final SpawnService spawns;
    private final LeagueChangeDisplayQueue leagueChangeDisplayQueue;

    @Inject
    public SpectatorRespawnService(TaskService tasks, SelectTreeMenu.Factory treeMenuFactory,
                                   KitService kitService, TalentPointService talentPoints,
                                   SpawnService spawns, LeagueChangeDisplayQueue leagueChangeDisplayQueue) {
        this.tasks = tasks;
        this.treeMenuFactory = treeMenuFactory;
        this.kitService = kitService;
        this.talentPoints = talentPoints;
        this.spawns = spawns;
        this.leagueChangeDisplayQueue = leagueChangeDisplayQueue;
    }

    @Override
    public void startRespawnDelay(Player player) {
        player.getInventory().clear();
        player.setGameMode(GameMode.SPECTATOR);
        I18n.sendLoc(player, Format.result(Message.of("core!respawn.delay-start")));
        tasks.delayed(
                () -> {
                    I18n.sendLoc(player, Format.success(Message.of("core!respawn.delay-end")));
                    startRespawn(player);
                },
                Duration.ofSeconds(5)
        );
    }

    @Override
    public void startRespawn(Player player) {
        //player.setGameMode(GameMode.SURVIVAL); // done in SpawnService below
        player.getInventory().clear();
        spawns.teleportToSpawnIfPossible(player);
        if (talentPoints.getCurrentTalentPointCount(player) > 0) {
            treeMenuFactory.openForResearch(player)
                    .addCloseHandler(ignored -> startPostRespawn(player));
        } else {
            startPostRespawn(player);
        }
    }

    @Override
    public void startPostRespawn(Player player) {
        kitService.applyKit(player);
        if (leagueChangeDisplayQueue.unqueueLeagueChange(player.getUniqueId())) {
            player.getInventory().setHelmet(new ItemStack(Material.PUMPKIN));
            tasks.delayed(
                    () -> kitService.applyKit(player), Duration.ofSeconds(5)
            );
        }
    }
}
