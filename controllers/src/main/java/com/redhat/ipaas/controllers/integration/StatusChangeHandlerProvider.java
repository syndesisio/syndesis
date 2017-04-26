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

package com.redhat.ipaas.controllers.integration;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.redhat.ipaas.model.integration.Integration;

public interface StatusChangeHandlerProvider {

    List<StatusChangeHandler> getStatusChangeHandlers();

    /**
     *
     */
    interface StatusChangeHandler {

        Set<Integration.Status> getTriggerStatuses();

        StatusUpdate execute(Integration model);

        class StatusUpdate {
            private Optional<Integration.Status> status;
            private Optional<String> statusMessage;
            private Optional<Integer> statusStep;

            public StatusUpdate(Optional<Integration.Status> status, Optional<String> statusMessage, Optional<Integer> statusStep) {
                this.status = status;
                this.statusMessage = statusMessage;
                this.statusStep = statusStep;
            }

            public StatusUpdate(Optional<Integration.Status> status, String statusMessage) {
                this(status, Optional.of(statusMessage), Optional.empty());
            }

            public StatusUpdate(Integration.Status status, String statusMessage) {
                this(Optional.of(status), Optional.of(statusMessage), Optional.empty());
            }

            public StatusUpdate(Integration.Status status) {
                this(Optional.of(status), Optional.empty(), Optional.empty());
            }

            public StatusUpdate(Integration.Status status, int step) {
                this(Optional.of(status), Optional.empty(), Optional.of(step));
            }

            public Optional<Integration.Status> getStatus() {
                return status;
            }

            public Optional<String> getStatusMessage() {
                return statusMessage;
            }

            public Optional<Integer> getStatusStep() {
                return statusStep;
            }
        }
    }
}
