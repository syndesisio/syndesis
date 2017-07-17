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

package io.syndesis.rest.v1.handler.connection;

import javax.annotation.Nonnull;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.syndesis.credential.AcquisitionMethod;
import io.syndesis.credential.Credentials;

@Api(value = "credentials")
public class ConnectorCredentialHandler {

    private final String connectorId;

    private final Credentials credentials;

    public ConnectorCredentialHandler(@Nonnull final Credentials credentials, @Nonnull final String connectorId) {
        this.credentials = credentials;
        this.connectorId = connectorId;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public AcquisitionMethod get() {
        return credentials.acquisitionMethodFor(connectorId);
    }

}
