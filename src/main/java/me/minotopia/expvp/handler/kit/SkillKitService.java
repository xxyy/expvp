/*
 * This file is part of Expvp,
 * Copyright (c) 2016-2017.
 *
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt.
 */

package me.minotopia.expvp.handler.kit;

import com.google.inject.Inject;
import me.minotopia.expvp.api.handler.HandlerService;
import me.minotopia.expvp.api.handler.kit.KitService;
import me.minotopia.expvp.api.handler.kit.compilation.KitCompilation;
import me.minotopia.expvp.api.handler.kit.compilation.KitCompiler;
import me.minotopia.expvp.api.handler.kit.compilation.KitElement;
import me.minotopia.expvp.api.model.PlayerData;
import me.minotopia.expvp.api.service.PlayerDataService;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Creates player kits from their skill sets.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2017-03-17
 */
public class SkillKitService implements KitService {
    private final KitCompiler compiler;
    private final PlayerDataService players;
    private final HandlerService handlerService;

    @Inject
    public SkillKitService(KitCompiler compiler, PlayerDataService players, HandlerService handlerService) {
        this.compiler = compiler;
        this.players = players;
        this.handlerService = handlerService;
    }

    @Override
    public void applyKit(Player player) {
        PlayerData data = players.findOrCreateData(player.getUniqueId());
        KitCompilation kit = compiler.compile(player, data);
        applyCompilation(player, kit);
    }

    private void applyCompilation(Player player, KitCompilation kit) {
        kit.getResult().entrySet()
                .forEach(e -> applyItem(player, e.getKey(), e.getValue()));
    }

    private void applyItem(Player player, int slotId, KitElement element) {
        player.getInventory().setItem(slotId, element.toItemStack());
    }

    @Override
    public void invalidateCache(UUID playerId) {
        PlayerData data = players.findOrCreateData(playerId);
        handlerService.unregisterHandlers(data);
        handlerService.registerHandlers(data);
    }
}