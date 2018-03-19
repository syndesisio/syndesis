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
    private long checkInterval = 1;

    // Time unit for check interval
    private TimeUnit checkIntervalUnit = TimeUnit.MINUTES;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(long checkInterval) {
        this.checkInterval = checkInterval;
    }

    public TimeUnit getCheckIntervalUnit() {
        return checkIntervalUnit;
    }

    public void setCheckIntervalUnit(TimeUnit checkIntervalUnit) {
        this.checkIntervalUnit = checkIntervalUnit;
    }
}
