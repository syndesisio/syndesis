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

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.syndesis.credential.Credentials;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.Kind;
import io.syndesis.model.connection.Connector;
import io.syndesis.rest.v1.handler.BaseHandler;
import io.syndesis.rest.v1.operations.Getter;
import io.syndesis.rest.v1.operations.Lister;
import io.syndesis.verifier.Verifier;

import org.springframework.stereotype.Component;

@Path("/connectors")
@Api(value = "connectors")
@Component
public class ConnectorHandler extends BaseHandler implements Lister<Connector>, Getter<Connector> {

    private final Verifier verifier;

    private final Credentials credentials;

    public ConnectorHandler(final DataManager dataMgr, final Verifier verifier, final Credentials credentials) {
        super(dataMgr);
        this.verifier = verifier;
        this.credentials = credentials;
    }

    @Override
    public Kind resourceKind() {
        return Kind.Connector;
    }

    @Path("/{id}/actions")
    public ConnectorActionHandler getActions(@PathParam("id") final String connectorId) {
        return new ConnectorActionHandler(getDataManager(), connectorId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}/verifier")
    public List<Verifier.Result> verifyConnectionParameters(@NotNull @PathParam("id") final String connectorId,
        final Map<String, String> props) {
        return verifier.verify(connectorId, props);
    }

    @Path("/{id}/credentials")
    public ConnectorCredentialHandler credentials(@NotNull final @PathParam("id") String connectorId) {
        return new ConnectorCredentialHandler(credentials, connectorId);
    }
}
