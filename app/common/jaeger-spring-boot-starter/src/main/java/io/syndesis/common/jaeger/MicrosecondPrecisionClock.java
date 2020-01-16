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
package io.syndesis.common.jaeger;

import io.jaegertracing.internal.clock.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*

Microsecond precision clock for more accuracy as there could be span tracing less than 1ms
io.jaegertracing.internal.clock.SystemClock emulates microseconds but is not accurate.

Discussion: https://github.com/jaegertracing/jaeger-client-java/issues/192
Copied from https://gist.github.com/mabn/9587658d78d730917559cd61a274ea4f
 */
public class MicrosecondPrecisionClock implements Clock {

    private static final Logger LOG = LoggerFactory.getLogger(MicrosecondPrecisionClock.class);

    private static final long ONE_MINUTE_IN_NANOSECONDS = 60_000_000_000L;
    private final long systemTimeOffsetNs;
    private long lastWarningLoggedAt;

    public MicrosecondPrecisionClock() {
        systemTimeOffsetNs = System.currentTimeMillis() * 1_000_000 - System.nanoTime();
    }

    @Override
    public long currentTimeMicros() {
        long currentNano = System.nanoTime();
        logInaccuracies(currentNano);
        return (currentNano + systemTimeOffsetNs) / 1000;
    }

    private void logInaccuracies(long currentNano) {
        long systemTimeNs = System.currentTimeMillis() * 1_000_000;
        boolean timeDifference = systemTimeNs > lastWarningLoggedAt + ONE_MINUTE_IN_NANOSECONDS
            && Math.abs(currentNano + systemTimeOffsetNs - systemTimeNs) > 250_000_000;
        if (timeDifference) {
            LOG.warn("Detected significant difference between the clock based on nanoTime+offset = {} and currentTimeMillis={}",
                currentNano + systemTimeOffsetNs, systemTimeNs);
            lastWarningLoggedAt = systemTimeNs;
        }
    }

    @Override
    public long currentNanoTicks() {
        return System.nanoTime();
    }

    @Override
    public boolean isMicrosAccurate() {
        return false;
    }
}
