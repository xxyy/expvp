/*
 * This file is part of Expvp,
 * Copyright (c) 2016-2017.
 *
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt.
 */

package me.minotopia.expvp.extimes.cfg;

import com.google.common.base.Preconditions;
import li.l1t.common.util.config.HashMapConfig;
import li.l1t.common.util.config.MapConfig;
import me.minotopia.expvp.api.extimes.ExTime;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Abstract base class for available time ranges.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2017-05-09
 */
public class AbstractExTime implements ConfigurationSerializable, ExTime {
    protected final LocalTime start;
    protected final LocalTime end;

    public AbstractExTime(Map<String, Object> source) {
        MapConfig config = HashMapConfig.of(source);
        this.start = config.findString("start")
                .map(LocalTime::parse)
                .orElseThrow(missingArgumentException("start"));
        this.end = config.findString("end")
                .map(LocalTime::parse)
                .orElseThrow(missingArgumentException("end"));
    }

    public AbstractExTime(LocalTime start, LocalTime end) {
        this.start = Preconditions.checkNotNull(start, "start");
        this.end = Preconditions.checkNotNull(end, "end");
    }

    protected Supplier<IllegalArgumentException> missingArgumentException(final String description) {
        return () -> new IllegalArgumentException("Missing " + description);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("start", start.toString());
        map.put("end", end.toString());
        return map;
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public Duration getDuration() {
        return Duration.between(start, end);
    }

    public boolean isEmpty() {
        return getDuration().isZero();
    }

    @Override
    public boolean contains(LocalTime time) {
        return time.isAfter(start) && time.isBefore(end);
    }
}