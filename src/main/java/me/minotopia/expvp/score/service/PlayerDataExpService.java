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

package me.minotopia.expvp.score.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import me.minotopia.expvp.api.model.MutablePlayerData;
import me.minotopia.expvp.api.model.PlayerData;
import me.minotopia.expvp.api.score.league.League;
import me.minotopia.expvp.api.score.league.LeagueService;
import me.minotopia.expvp.api.score.service.ExpService;
import me.minotopia.expvp.api.service.PlayerDataService;
import me.minotopia.expvp.util.SessionProvider;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * An ExpService implementation that uses PlayerDataService as Exp data source.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2017-03-27
 */
public class PlayerDataExpService implements ExpService {
    private final PlayerDataService players;
    private final SessionProvider sessionProvider;
    private final LeagueService leagues;

    @Inject
    public PlayerDataExpService(PlayerDataService players, SessionProvider sessionProvider, LeagueService leagues) {
        this.players = players;
        this.sessionProvider = sessionProvider;
        this.leagues = leagues;
    }

    @Override
    public void incrementExp(Player player, int exp) {
        Preconditions.checkArgument(exp > 0, "Exp can only be incremented by a positive amount, got: ", exp, player);
        modifyExp(player, exp);
    }

    private void modifyExp(Player player, int expModifier) {
        sessionProvider.inSession(ignored -> {
            MutablePlayerData playerData = players.findOrCreateDataMutable(player.getUniqueId());
            int newExp = playerData.getExp() + expModifier;
            if (expModifier < 0 && newExp < -100) {
                return;
            }
            playerData.setExp(newExp);
            players.saveData(playerData);
            leagues.updateLeague(player);
            player.setLevel(playerData.getExp()); //The client displays negative numbers as zero
            displayLeagueProgress(player, newExp, leagues.getPlayerLeague(playerData));
        });
    }

    private void displayLeagueProgress(Player player, int currentExp, League current) {
        Optional<League> next = current.next();
        if (next.isPresent()) {
            int currentMin = current.getMinExp();
            int expSinceLeagueChange = currentExp - currentMin;
            int diffToCurrent = next.get().getMinExp() - currentMin;
            float progress = ((float) expSinceLeagueChange / (float) diffToCurrent);
            player.setExp(progress);
        } else {
            player.setExp(1F);
        }
    }

    @Override
    public void decrementExp(Player player, int exp) {
        Preconditions.checkArgument(exp > 0, "Exp can only be decremented by a positive amount, got: ", exp, player);
        modifyExp(player, exp * -1);
    }

    @Override
    public int getExpCount(Player player) {
        return players.findData(player.getUniqueId())
                .map(PlayerData::getExp)
                .orElse(0);
    }
}
