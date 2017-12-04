/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.controllers.integration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.syndesis.model.integration.Integration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "controllers.integration.enabled", havingValue = "false")
public class DemoHandlerProvider implements StatusChangeHandlerProvider {

    @Override
    public List<StatusChangeHandler> getStatusChangeHandlers() {
        return Arrays.asList(
            new DemoHandler(Integration.Status.Activated, 5000L),
            new DemoHandler(Integration.Status.Deactivated, 5000L),
            new DemoHandler(Integration.Status.Deleted, 5000L)
                            );
    }

    /* default */ static class DemoHandler implements StatusChangeHandler {
        private final Integration.Status status;
        private final long waitMillis;

        /* default */ DemoHandler(Integration.Status status, long waitMillis) {
            this.status = status;
            this.waitMillis = waitMillis;
        }

        @Override
        public Set<Integration.Status> getTriggerStatuses() {
            return Collections.singleton(status);
        }

        @Override
        public StatusUpdate execute(Integration integration) {

            try {
                Thread.sleep(waitMillis);
                return new StatusUpdate(status);
            } catch (InterruptedException e) {
                return null;
            }
        }
    }
}
