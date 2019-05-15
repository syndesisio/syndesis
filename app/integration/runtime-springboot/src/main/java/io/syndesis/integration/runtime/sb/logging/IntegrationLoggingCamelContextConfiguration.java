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

package io.syndesis.integration.runtime.sb.logging;

import io.syndesis.common.util.KeyGenerator;
import io.syndesis.integration.runtime.logging.ActivityTracker;
import io.syndesis.integration.runtime.logging.IntegrationLoggingListener;
import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;

/**
 * @author Christoph Deppisch
 */
public class IntegrationLoggingCamelContextConfiguration implements CamelContextConfiguration {
    private final ActivityTracker tracker;

    public IntegrationLoggingCamelContextConfiguration(ActivityTracker activityTracker) {
        this.tracker = activityTracker;
    }

    @Override
    public void beforeApplicationStart(CamelContext camelContext) {
        // Lets generates always incrementing lexically sortable unique
        // uuids. These uuids are also more compact than the camel default
        // and contain an embedded timestamp.
        camelContext.setUuidGenerator(KeyGenerator::createKey);

        // Log listener
        camelContext.addLogListener(new IntegrationLoggingListener(tracker));
    }

    @Override
    public void afterApplicationStart(CamelContext camelContext) {
        // no ops
    }
}
