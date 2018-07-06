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
package io.syndesis.server.endpoint.v1.handler.monitoring;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.monitoring.IntegrationDeploymentStateDetails;
import io.syndesis.server.endpoint.monitoring.MonitoringProvider;
import io.syndesis.server.endpoint.v1.operations.Resource;

@Path("/monitoring/integrations")
@Api(value = "integration-monitoring")
@ComponentScan("io.syndesis.server.endpoint.monitoring")
@Component
public class IntegrationDeploymentStateDetailsHandler implements Resource {

    private final MonitoringProvider metricsProvider ;

    public IntegrationDeploymentStateDetailsHandler(MonitoringProvider monitoringProvider) {
        super();
        this.metricsProvider = monitoringProvider;
    }

    @Override
    public Kind resourceKind() {
        return Kind.IntegrationMetricsSummary;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/{integrationId}")
    public IntegrationDeploymentStateDetails get(@NotNull @PathParam("integrationId") @ApiParam(required = true) String integrationId) {
        return metricsProvider.getIntegrationStateDetails(integrationId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Retrieves monitoring state details for all integrations")
    public List<IntegrationDeploymentStateDetails> get() {
        return metricsProvider.getAllIntegrationStateDetails();
    }
}
