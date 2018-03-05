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
package io.syndesis.server.endpoint.v1.handler.metrics;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;
import org.springframework.context.annotation.ComponentScan;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.metrics.IntegrationMetricsSummary;
import io.syndesis.server.endpoint.metrics.MetricsProvider;
import io.syndesis.server.endpoint.v1.operations.Resource;

@Path("/metrics/integrations")
@Api(value = "integration-metrics")
@ComponentScan("io.syndesis.server.endpoint.metrics")
@Component
public class IntegrationMetricsHandler implements Resource {

    private final MetricsProvider metricsProvider ;

    public IntegrationMetricsHandler(MetricsProvider metricsProvider) {
        super();
        this.metricsProvider = metricsProvider;
    }

    @Override
    public Kind resourceKind() {
        return Kind.IntegrationMetricsSummary;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/{integrationId}")
    public IntegrationMetricsSummary get(@NotNull @PathParam("integrationId") @ApiParam(required = true) String integrationId) {
        return metricsProvider.getIntegrationMetricsSummary(integrationId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Retrieves a rolled up metrics summary for all integrations over their lifetime")
    public IntegrationMetricsSummary get() {
        return metricsProvider.getTotalIntegrationMetricsSummary();
    }
}
