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

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.syndesis.connector.generator.ConnectorGenerator;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.connection.ConnectorTemplate;
import io.syndesis.model.connection.ConnectorSettings;
import io.syndesis.rest.v1.operations.Violation;

import org.springframework.context.ApplicationContext;

public final class CustomConnectorHandler {

    private final ApplicationContext applicationContext;

    private final String connectorTemplateId;

    private final DataManager dataManager;

    /* default */ CustomConnectorHandler(final String connectorTemplateId, final DataManager dataManager,
        final ApplicationContext applicationContext) {
        this.connectorTemplateId = connectorTemplateId;
        this.dataManager = dataManager;
        this.applicationContext = applicationContext;
    }

    @POST
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation("Creates a new Connector based on the ConnectorTemplate identified by the provided `connector-template-id` and the data given in `customConnector`")
    public Connector info(final ConnectorSettings connectorSettings) {
        final ConnectorTemplate connectorTemplate = dataManager.fetch(ConnectorTemplate.class, connectorTemplateId);

        if (connectorTemplate == null) {
            throw new EntityNotFoundException("Connector template: " + connectorTemplateId);
        }

        final ConnectorGenerator connectorGenerator = applicationContext.getBean(connectorTemplateId,
            ConnectorGenerator.class);

        if (connectorGenerator == null) {
            throw new EntityNotFoundException(
                "Unable to find connector generator for connector template with id: " + connectorTemplateId);
        }

        return connectorGenerator.info(connectorTemplate, connectorSettings);
    }

    @POST
    @Path("/validation")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation("Validates a new Connector based on the ConnectorTemplate identified by the provided `connector-template-id` and the data given in `customConnector`")
    @ApiResponses({//
        @ApiResponse(code = 204, message = "All validations pass"), //
        @ApiResponse(code = 400, message = "Found violations in validation", responseContainer = "Set",
            response = Violation.class)//
    })
    public List<Violation> validate(final ConnectorSettings connectorSettings) {
        // intentionally left blank
        return Collections.emptyList();
    }
}
