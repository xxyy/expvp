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

package me.minotopia.expvp.handler.factory.damage;

import me.minotopia.expvp.handler.damage.CulpritEffectHandler;
import me.minotopia.expvp.handler.factory.HandlerArgs;
import me.minotopia.expvp.skill.meta.Skill;

/**
 * Creates culprit effect handlers.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2017-03-23
 */
public class CulpritEffectHandlerFactory extends AbstractDamageHandlerFactory {
    public CulpritEffectHandlerFactory(String ownHandlerSpec) {
        super(ownHandlerSpec);
    }

    @Override
    public String getDescription() {
        return "culprit effect handler: probability,effect_type,duration_in_seconds,potion_level";
    }

    @Override
    protected CulpritEffectHandler createHandler(Skill skill, HandlerArgs args) {
        return new CulpritEffectHandler(
                skill, probabilityPerCent(args), potionEffectType(args),
                durationSeconds(args), potionLevel(args)
        );
    }
}
