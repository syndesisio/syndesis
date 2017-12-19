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
package io.syndesis.rest.v1.handler.connection;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.ListResult;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.connection.ConnectorSettings;
import io.syndesis.model.connection.ConnectorSummary;
import io.syndesis.rest.util.PaginationFilter;
import io.syndesis.rest.util.ReflectiveSorter;
import io.syndesis.rest.v1.operations.PaginationOptionsFromQueryParams;
import io.syndesis.rest.v1.operations.SortOptionsFromQueryParams;
import io.syndesis.rest.v1.util.PredicateFilter;

import org.springframework.context.ApplicationContext;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Api(tags = {"custom-connector", "connector-template"})
public final class CustomConnectorHandler extends BaseConnectorGeneratorHandler {

    /* default */ CustomConnectorHandler(final DataManager dataManager, final ApplicationContext applicationContext) {
        super(dataManager, applicationContext);
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation("Creates a new Connector based on the ConnectorTemplate identified by the provided `id`  and the data given in`connectorSettings`")
    @ApiResponses(@ApiResponse(code = 200, response = Connector.class, message = "Newly created Connector"))
    public Connector create(final ConnectorSettings connectorSettings) {

        final Connector connector = withGeneratorAndTemplate(connectorSettings.getConnectorTemplateId(),
            (generator, template) -> generator.generate(template, connectorSettings));

        return getDataManager().create(connector);
    }

    @POST
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation("Provides a summary of the connector as it would be built using a ConnectorTemplate identified by the provided `connector-template-id` and the data given in `connectorSettings`")
    public ConnectorSummary info(final ConnectorSettings connectorSettings) {
        return withGeneratorAndTemplate(connectorSettings.getConnectorTemplateId(),
            (generator, template) -> generator.info(template, connectorSettings));
    }
}
