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
import com.sk89q.intake.Command;
import li.l1t.common.exception.UserException;
import li.l1t.common.i18n.Message;
import li.l1t.common.intake.provider.annotation.Sender;
import me.minotopia.expvp.Permission;
import me.minotopia.expvp.api.handler.kit.KitService;
import me.minotopia.expvp.api.misc.RepairService;
import me.minotopia.expvp.api.model.MutablePlayerData;
import me.minotopia.expvp.api.model.ObtainedSkill;
import me.minotopia.expvp.api.model.PlayerData;
import me.minotopia.expvp.command.permission.EnumRequires;
import me.minotopia.expvp.command.service.CommandService;
import me.minotopia.expvp.i18n.Format;
import me.minotopia.expvp.i18n.I18n;
import me.minotopia.expvp.model.player.HibernateResetService;
import me.minotopia.expvp.util.SessionProvider;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Command used to administer Expvp.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-09-14
 */
@AutoRegister("epa")
public class CommandEPAdmin extends AbstractServiceBackedCommand<CommandService> {
    private final KitService kitService;
    private final SessionProvider sessionProvider;
    private final Server server;

    @Inject
    CommandEPAdmin(CommandService commandService, KitService kitService, SessionProvider sessionProvider, Server server) {
        super(commandService);
        this.kitService = kitService;
        this.sessionProvider = sessionProvider;
        this.server = server;
    }

    @Command(aliases = "settp", min = 2,
            desc = "Setzt Talentpunkte",
            help = "Setzt die Talentpunkte\neines Spielers.",
            usage = "[uuid|name] [tp]")
    @EnumRequires(Permission.ADMIN_PLAYERS)
    public void setBooks(CommandSender sender, String playerSpec, int newTalentPoints)
            throws IOException {
        modifyProperty(sender, playerSpec, "Talentpunkte", playerData -> {
            playerData.setTalentPoints(newTalentPoints);
            return playerData.getAvailableTalentPoints();
        });
    }

    @Command(aliases = "addtp", min = 2,
            desc = "Gibt Talentpunkte",
            help = "Gibt einem Spieler Talentpunkte.",
            usage = "[uuid|name] [tp]")
    @EnumRequires(Permission.ADMIN_PLAYERS)
    public void addBooks(CommandSender sender, String playerSpec, int addTalentPoints)
            throws IOException {
        modifyProperty(sender, playerSpec, "Talentpunkte", playerData -> {
            playerData.setTalentPoints(playerData.getAvailableTalentPoints() + addTalentPoints);
            return playerData.getAvailableTalentPoints();
        });
    }

    private void modifyProperty(CommandSender sender, String playerInput, String property,
                                Function<MutablePlayerData, Integer> mutator) {
        UUID playerId = service().findPlayerByNameOrIdOrFail(playerInput);
        int newValue = service().modifyPlayerData(playerId, mutator);
        sender.sendMessage(String.format( //TODO: player name -> xyc profile api
                "§e§l➩ §aDieser Spieler hat jetzt %d " + property + ".",
                newValue
        ));
    }

    @Command(aliases = "whois", min = 2,
            desc = "Zeigt Infos zu einem Spieler",
            usage = "[uuid|name]")
    @EnumRequires(Permission.ADMIN_BASIC)
    public void whoIs(CommandSender sender, String playerInput)
            throws IOException {
        sessionProvider.inSession(ignored -> {
            UUID playerId = service().findPlayerByNameOrIdOrFail(playerInput);
            PlayerData playerData = service().findPlayerData(playerId);
            sender.sendMessage("§a»»» §eSpielerinfo §a«««"); //TODO: player name -> xyc profile api
            formatMessage(sender,
                    "§e§l➩ §eLiga: §a%s §eExp: §a%d §eTP: §a%d §eSprache: §a%s",
                    playerData.getLeagueName(), playerData.getExp(), playerData.getAvailableTalentPoints(), playerData.getLocale().getDisplayName()
            );
            double totalKD = computeKDRatio(playerData.getTotalKills(), playerData.getTotalDeaths());
            formatMessage(sender,
                    "§e§l➩ §aGesamte §eKills: §a%d §eDeaths: §a%d §eK/D: §a%.2f",
                    playerData.getTotalKills(), playerData.getTotalDeaths(), totalKD
            );
            double currentKD = computeKDRatio(playerData.getCurrentKills(), playerData.getCurrentDeaths());
            formatMessage(sender,
                    "§e§l➩ §aAktuelle §eKills: §a%d §eDeaths: §a%d §eK/D: §a%.2f",
                    playerData.getCurrentKills(), playerData.getCurrentDeaths(), currentKD
            );
            formatMessage(sender,
                    "§e§l➩ §eSkills: §a%s",
                    playerData.getSkills().stream().map(ObtainedSkill::getSkillId).collect(Collectors.joining("§e, §a"))
            );
        });
    }

    private double computeKDRatio(int kills, int deaths) {
        return (double) kills / (deaths == 0 ? 1D : (double) deaths);
    }

    private void formatMessage(CommandSender sender, String format, Object... args) {
        sender.sendMessage(String.format(format, args));
    }

    @Command(aliases = "testkit", desc = "Testet dein Kit")
    @EnumRequires(Permission.ADMIN_BASIC)
    public void testKit(@Sender Player player) {
        kitService.invalidateCache(player.getUniqueId());
        kitService.applyKit(player);
        I18n.sendLoc(player, Format.success(Message.ofText(
                "Viel Spaß mit deinem Kit!"
        )));
    }

    @Command(aliases = "clearcache", desc = "Leert diverse Caches")
    @EnumRequires(Permission.ADMIN_BASIC)
    public void clearCache(CommandSender sender) {
        sessionProvider.getSessionFactory().getCache().evictAllRegions();
        I18n.clearCache();
        server.getOnlinePlayers().stream()
                .map(Player::getUniqueId)
                .forEach(kitService::invalidateCache);
        I18n.sendLoc(sender, Format.success(Message.ofText(
                "Ja mehr oder weniger sollten jetzt zumindest die schlimmsten Caches geleert sein."
        )));
    }

    @Command(aliases = "repair", desc = "Repaired dein Kit")
    @EnumRequires(Permission.ADMIN_BASIC)
    public void repairKit(RepairService repairService, @Sender Player player) {
        repairService.repair(player);
        I18n.sendLoc(player, Format.success(Message.ofText(
                "oke"
        )));
    }

    @Command(aliases = "__forcereset", desc = "Forcefully resets temporary stats")
    @EnumRequires({Permission.ADMIN_OVERRIDE, Permission.ADMIN_PLAYERS})
    public void forceReset(CommandSender sender, HibernateResetService resetService, String arg) {
        if (!(sender instanceof ConsoleCommandSender)) {
            throw new UserException("Can only be executed by console");
        }
        if (!arg.equalsIgnoreCase("ireallyknowwhatimdoing")) {
            throw new UserException("This command is only for use by authorised personnel. Provide the secret access code.");
        }
        resetService.resetAllTemporaryStats();
        I18n.sendLoc(sender, Format.success(Message.ofText("lol ok")));
    }
}
