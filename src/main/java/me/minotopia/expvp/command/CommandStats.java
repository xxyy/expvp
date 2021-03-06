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

package me.minotopia.expvp.command;

import com.google.inject.Inject;
import li.l1t.common.chat.ComponentSender;
import li.l1t.common.chat.XyComponentBuilder;
import li.l1t.common.command.BukkitExecution;
import li.l1t.common.command.BukkitExecutionExecutor;
import li.l1t.common.exception.InternalException;
import li.l1t.common.exception.UserException;
import li.l1t.common.i18n.Message;
import li.l1t.common.shared.uuid.UUIDRepository;
import li.l1t.common.util.CommandHelper;
import me.minotopia.expvp.EPPlugin;
import me.minotopia.expvp.api.friend.FriendService;
import me.minotopia.expvp.api.i18n.DisplayNameService;
import me.minotopia.expvp.api.misc.ConstructOnEnable;
import me.minotopia.expvp.api.model.PlayerData;
import me.minotopia.expvp.api.model.RankService;
import me.minotopia.expvp.api.score.league.LeagueService;
import me.minotopia.expvp.api.service.PlayerDataService;
import me.minotopia.expvp.i18n.Format;
import me.minotopia.expvp.i18n.I18n;
import me.minotopia.expvp.i18n.Plurals;
import me.minotopia.expvp.i18n.exception.I18nInternalException;
import me.minotopia.expvp.i18n.exception.I18nUserException;
import me.minotopia.expvp.model.player.HibernatePlayerTopRepository;
import me.minotopia.expvp.util.SessionProvider;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Displays a player's public stats.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2017-04-27
 */
@ConstructOnEnable
public class CommandStats extends BukkitExecutionExecutor {
    private final PlayerDataService players;
    private final UUIDRepository uuidRepository;
    private final FriendService friendService;
    private final DisplayNameService names;
    private final RankService rankService;
    private final LeagueService leagues;
    private final SessionProvider sessionProvider;
    private final HibernatePlayerTopRepository topRepository;

    @Inject
    public CommandStats(PlayerDataService players, UUIDRepository uuidRepository, FriendService friendService,
                        DisplayNameService names, RankService rankService, LeagueService leagues, SessionProvider sessionProvider,
                        EPPlugin plugin, HibernatePlayerTopRepository topRepository) {
        this.players = players;
        this.uuidRepository = uuidRepository;
        this.friendService = friendService;
        this.names = names;
        this.rankService = rankService;
        this.leagues = leagues;
        this.sessionProvider = sessionProvider;
        this.topRepository = topRepository;
        plugin.getCommand("stats").setExecutor(this);
    }

    @Override
    public boolean execute(BukkitExecution exec) throws UserException, InternalException {
        try {
            sessionProvider.inSession(ignored -> {
                handle(exec);
            });
        } catch (I18nInternalException | I18nUserException e) {
            I18n.sendLoc(exec.sender(), Message.of(e.getWrapperMessageKey(), Message.of(e.getMessageKey(), e.getMessageParameters())));
        } catch (InternalException | UserException e) {
            exec.sender().sendMessage(e.getColoredMessage());
        }
        return true;
    }

    private void handle(BukkitExecution exec) {
        if (exec.findArg(0).filter("top"::equalsIgnoreCase).isPresent()) {
            showTopTenKillersTo(exec.sender());
            return;
        }
        PlayerData target = exec.findArg(0)
                .map(this::tryFindTarget)
                .orElseGet(provideSelfAsDataOrFail(exec.sender()));
        showStatsOfTo(target, exec.sender());
    }

    private void showTopTenKillersTo(CommandSender sender) {
        List<PlayerData> topPlayers = new ArrayList<>(topRepository.findTopNByExp(10));
        I18n.sendLoc(sender, Format.header("score!stats.top-header"));
        for (int i = 0; i < topPlayers.size(); i++) {
            PlayerData data = topPlayers.get(i);
            String name = uuidRepository.getName(data.getUniqueId());
            I18n.sendLoc(sender, Format.listItem("score!stats.exp-item", i + 1, name, data.getExp()));
        }
    }

    private PlayerData tryFindTarget(String arg) {
        Optional<? extends PlayerData> target = Optional.ofNullable(uuidRepository.forName(arg))
                .flatMap(players::findData);
        if (target.isPresent()) {
            return target.get();
        } else {
            throw new I18nUserException("error!stats.unknown", arg);
        }
    }

    private Supplier<PlayerData> provideSelfAsDataOrFail(CommandSender sender) {
        return () -> {
            if (sender instanceof Player) {
                return players.findData(((Player) sender).getUniqueId())
                        .orElseThrow(() -> new I18nUserException("error!stats.unknown", sender.getName()));
            } else {
                throw new I18nUserException("error!stats.need-player");
            }
        };
    }

    private void showStatsOfTo(PlayerData target, CommandSender receiver) {
        I18n.sendLoc(receiver, Format.header("score!stats.header", names.displayName(target)));
        showCurrentKillsDeathsTo(target, receiver);
        showTotalKillsDeathsTo(target, receiver);
        showOwnExpRelated(target, receiver);
        showFriendInfo(target, receiver);
        showStreakAndSkills(target, receiver);
    }

    private void showCurrentKillsDeathsTo(PlayerData target, CommandSender receiver) {
        int kills = target.getCurrentKills();
        int deaths = target.getCurrentDeaths();
        double kdRatio = computeKDRatio(kills, deaths);
        I18n.sendLoc(receiver, Format.result("score!stats.current-kds",
                Plurals.killPlural(kills), Plurals.deathPlural(deaths), kdRatio)
        );
    }

    private void showTotalKillsDeathsTo(PlayerData target, CommandSender receiver) {
        int kills = target.getTotalKills();
        int deaths = target.getTotalDeaths();
        int assists = target.getTotalKillAssists();
        double kdRatio = computeKDRatio(kills, deaths);
        I18n.sendLoc(receiver, Format.result("score!stats.overall-kds",
                Plurals.killPlural(kills), Plurals.deathPlural(deaths), kdRatio, assists
        ));
    }

    private double computeKDRatio(int kills, int deaths) {
        return (double) kills / (deaths == 0 ? 1D : (double) deaths);
    }

    private void showOwnExpRelated(PlayerData target, CommandSender receiver) {
        Message leagueName = names.displayName(leagues.getPlayerLeague(target));
        I18n.sendLoc(receiver, Format.result("score!stats.exp-related",
                target.getExp(), Format.rank(rankService.getExpRank(target)), leagueName
        ));
    }

    private void showFriendInfo(PlayerData target, CommandSender receiver) {
        Optional<PlayerData> friend = friendService.findFriend(target);
        if (friend.isPresent()) {
            showFriendStatsTo(receiver, friend.get());
        } else if (!target.getUniqueId().equals(CommandHelper.getSenderId(receiver))) {
            ComponentSender.sendTo(
                    receiver,
                    TextComponent.fromLegacyText(I18n.loc(receiver, Format.result("score!stats.no-friend"))),
                    new XyComponentBuilder(" ")
                            .append(I18n.loc(receiver, "score!stats.friend-add"))
                            .hintedCommand("/fs add " + target.getUniqueId())
                            .color(ChatColor.DARK_GREEN).create()
            );
        }
    }

    private void showFriendStatsTo(CommandSender receiver, PlayerData friend) {
        double kdRatio = computeKDRatio(friend.getTotalKills(), friend.getTotalDeaths());
        BaseComponent[] components = TextComponent.fromLegacyText(I18n.loc(
                receiver, Format.result("score!stats.friend-stats",
                        names.displayName(friend), friend.getExp(), kdRatio
                )));
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/stats " + friend.getUniqueId());
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                TextComponent.fromLegacyText(I18n.loc(receiver, "score!stats.friend-tooltip"))
        );
        Arrays.stream(components)
                .forEach(component -> {
                    component.setClickEvent(clickEvent);
                    component.setHoverEvent(hoverEvent);
                });
        ComponentSender.sendTo(components, receiver);
    }

    private void showStreakAndSkills(PlayerData target, CommandSender receiver) {
        I18n.sendLoc(receiver, Format.result("score!stats.best-streak-skills",
                target.getBestKillStreak(), target.getSkills().size()
        ));
    }
}
