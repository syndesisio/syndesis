/*
 *
 *  Copyright (C) 2016 Red Hat, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */package com.redhat.ipaas.controllers.integration;

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
            private Optional<Integration.Status> newStatus;
            private String message;

            public StatusUpdate(Optional<Integration.Status> newStatus, String message) {
                this.newStatus = newStatus;
                this.message = message;
            }

            public StatusUpdate(Integration.Status currentStatus, String message) {
                this(Optional.of(currentStatus), message);
            }

            public StatusUpdate(Integration.Status status) {
                this(status, status.name());
            }

            public Optional<Integration.Status> getNewStatus() {
                return newStatus;
            }

            public String getStatusMessage() {
                return message;
            }
        }
    }
}
