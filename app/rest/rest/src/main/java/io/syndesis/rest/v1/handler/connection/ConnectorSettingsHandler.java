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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.ApiOperation;
import io.syndesis.connector.generator.ConnectorSummary;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.connection.ConnectorSettings;

import org.springframework.context.ApplicationContext;

public final class ConnectorSettingsHandler extends BaseConnectorGeneratorHandler {

    private final String templateId;

    /* default */ ConnectorSettingsHandler(final String templateId, final DataManager dataManager,
        final ApplicationContext applicationContext) {
        super(dataManager, applicationContext);
        this.templateId = templateId;
    }

    @POST
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation("Provides a summary of the connector as it would be built using a ConnectorTemplate identified by the provided `connector-template-id` and the data given in `connectorSettings`")
    public ConnectorSummary info(final ConnectorSettings connectorSettings) {
        return withGeneratorAndTemplate(templateId, (generator, template) -> generator.info(template, connectorSettings));
    }

}
