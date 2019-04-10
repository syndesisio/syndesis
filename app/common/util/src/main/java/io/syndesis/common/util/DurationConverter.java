/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.common.util;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts from String to a Duration.
 *
 * I wish spring boot could load and apply to config props automatically.
 */
public class DurationConverter {

    private static final List<ConverterCheck> CHECKS = Arrays.asList(
        new ConverterCheck(patternFor("ns"), Duration::ofNanos),
        new ConverterCheck(patternFor("ms"), Duration::ofMillis),
        new ConverterCheck(patternFor("s"), Duration::ofSeconds),
        new ConverterCheck(patternFor("seconds?"), Duration::ofSeconds),
        new ConverterCheck(patternFor("m"), Duration::ofMinutes),
        new ConverterCheck(patternFor("minutes?"), Duration::ofMinutes),
        new ConverterCheck(patternFor("h"), Duration::ofHours),
        new ConverterCheck(patternFor("hours?"), Duration::ofHours),
        new ConverterCheck(patternFor("d"), Duration::ofDays),
        new ConverterCheck(patternFor("days?"), Duration::ofDays)
    );

    private static class ConverterCheck {
        private final Pattern pattern;
        private final Function<Long, Duration> converter;
        ConverterCheck(Pattern pattern, Function<Long, Duration> converter) {
            this.pattern = pattern;
            this.converter = converter;
        }

        public Duration apply(String source) {
            Matcher m = pattern.matcher(source);
            if( m.matches() ) {
                return converter.apply(Long.parseLong(m.group(1)));
            }
            return null;
        }
    }

    public Duration convert(String source) {
        if( source == null ) {
            return  null;
        }
        for (ConverterCheck check : CHECKS) {
            Duration d = check.apply(source);
            if( d!=null ) {
                return d;
            }
        }
        return Duration.parse(source);
    }

    private static Pattern patternFor(String units) {
        return Pattern.compile("^\\s*(\\d+)\\s*("+units+")?\\s*$", Pattern.CASE_INSENSITIVE);
    }

}
