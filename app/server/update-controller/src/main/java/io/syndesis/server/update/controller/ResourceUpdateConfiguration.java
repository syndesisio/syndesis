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
package io.syndesis.server.update.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = ResourceUpdateConstants.CONFIGURATION_PREFIX)
public class ResourceUpdateConfiguration {
    // Enable/Disable connector upgrades checks
    private boolean enabled;

    // Interval between check
    private Scheduler scheduler = new Scheduler();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public static class Scheduler {
        // Enable/Disable connector upgrades scheduler
        private boolean enabled = true;

        // Interval between check
        private long interval = 60;

        // Time unit for check interval
        private TimeUnit intervalUnit = TimeUnit.SECONDS;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getInterval() {
            return interval;
        }

        public void setInterval(long interval) {
            this.interval = interval;
        }

        public TimeUnit getIntervalUnit() {
            return intervalUnit;
        }

        public void setIntervalUnit(TimeUnit intervalUnit) {
            this.intervalUnit = intervalUnit;
        }
    }
}
