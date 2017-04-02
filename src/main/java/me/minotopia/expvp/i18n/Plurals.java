/*
 * This file is part of Expvp,
 * Copyright (c) 2016-2017.
 *
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt.
 */

package me.minotopia.expvp.i18n;

import li.l1t.common.intake.i18n.Message;

/**
 * Resolves message strings for names that need to be pluralised.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2017-04-02
 */
public class Plurals {
    private Plurals() {

    }

    /**
     * Resolves the correct singular/plural form for a thing. For singular (count = 1), {@code .one} is appended to the
     * base key, and for plural, {@code .many} is appended to the base key. The resolved key is then passed to the
     * {@code core!plural-base} message as a second argument, with the count as the first argument.
     *
     * @param baseKey the base key for the word
     * @param count   the actual count
     * @return a resolved message
     */
    public static Message plural(String baseKey, int count) {
        return Message.of("core!plural-base", count, Message.of(resolveKey(baseKey, count)));
    }

    private static String resolveKey(String baseKey, int count) {
        return baseKey + (count == 1 ? ".one" : ".many");
    }
}