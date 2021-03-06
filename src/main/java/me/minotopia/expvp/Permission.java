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

package me.minotopia.expvp;

import org.bukkit.permissions.Permissible;

/**
 * The enumeration of permissions used by ExPvP to limit who can do what.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-08-15
 */
public enum Permission {
    /**
     * Allows to perform basic administrative tasks, like view skills and trees.
     */
    ADMIN_BASIC("expvp.admin.basic"),
    /**
     * Allows to create and edit skills.
     */
    ADMIN_SKILL("expvp.admin.skill"),
    /**
     * Allows to create and edit skill trees.
     */
    ADMIN_TREE("expvp.admin.tree"),
    /**
     * Allows to override certain safety measures and delete content such as skills and skill
     * trees.
     */
    ADMIN_OVERRIDE("expvp.admin.override"),
    /**
     * Allows to ignore all chat filters.
     */
    ADMIN_CHAT_IGNORE("expvp.chat.ignore"),
    /**
     * Allows to administer player data.
     */
    ADMIN_PLAYERS("expvp.admin.players"),
    /**
     * Allows to create and edit map spawns.
     */
    ADMIN_SPAWN("expvp.admin.spawn"),
    /**
     * Allows to bypass the ExTimes timed whitelist.
     */
    ADMIN_EXTIMES_BYPASS("expvp.admin.etby"),
    /**
     * Allows to manage ExTimes activation times.
     */
    ADMIN_EXTIMES("expvp.admin.extimes"),
    /**
     * Allows to receive information about advertisements caught by the filter.
     */
    CHAT_AD_INFO("expvp.chat.adinfo"),
    /**
     * Allows to read all private messages from all players on the server.
     */
    CHAT_SPY("expvp.chat.spy"),
    /**
     * Allows to clear the chat for everybody.
     */
    CHAT_CLEAR("expvp.chat.clear"),
    /**
     * Allows to mute the global chat.
     */
    CHAT_GLOBAL_MUTE("expvp.chat.glomu"),
    /**
     * Allows to use the /mv command to vote for spawns.
     */
    COMMAND_MAP_VOTE("expvp.spawn.vote");

    private final String value;

    Permission(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public boolean has(Permissible permissible) {
        return permissible.hasPermission(value());
    }

    @Override
    public String toString() {
        return value();
    }
}
