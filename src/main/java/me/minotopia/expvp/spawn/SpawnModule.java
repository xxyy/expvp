/*
 * This file is part of Expvp,
 * Copyright (c) 2016-2017.
 *
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt.
 */

package me.minotopia.expvp.spawn;

import com.google.inject.AbstractModule;
import me.minotopia.expvp.api.spawn.SpawnService;

/**
 * Provides the dependency wiring for the spawn module.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2017-04-12
 */
public class SpawnModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(SpawnManager.class);
        bind(SpawnService.class).to(YamlSpawnService.class);
    }
}