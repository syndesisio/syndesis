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
package io.syndesis.server.endpoint.v1.handler.credential;

final class CallbackStatus {

    private final String connectorId;

    private final String message;

    private final Status status;

    enum Status {
        FAILURE, SUCCESS
    }

    private CallbackStatus(final Status status, final String connectorId, final String message) {
        this.status = status;
        this.connectorId = connectorId;
        this.message = message;
    }

    public String getConnectorId() {
        return connectorId;
    }

    public String getMessage() {
        return message;
    }

    public Status getStatus() {
        return status;
    }

    public static CallbackStatus failure(final String connectorId, final String message) {
        return new CallbackStatus(Status.FAILURE, connectorId, message);
    }

    public static CallbackStatus success(final String connectorId, final String message) {
        return new CallbackStatus(Status.SUCCESS, connectorId, message);
    }

    @Override
    public String toString() {
        return "Connector: " + connectorId + ", status: " + status + ", message: " + message;
    }
}
