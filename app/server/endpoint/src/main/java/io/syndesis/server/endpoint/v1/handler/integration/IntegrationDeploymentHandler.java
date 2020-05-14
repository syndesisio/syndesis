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
package io.syndesis.server.endpoint.v1.handler.integration;

import javax.persistence.EntityNotFoundException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.util.Labels;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.endpoint.util.PaginationFilter;
import io.syndesis.server.endpoint.v1.handler.BaseHandler;
import io.syndesis.server.endpoint.v1.handler.user.UserConfigurationProperties;
import io.syndesis.server.endpoint.v1.operations.PaginationOptionsFromQueryParams;
import io.syndesis.server.openshift.OpenShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/integrations/{id}/deployments")
@Tag(name = "integration-deployments")
@Component
public final class IntegrationDeploymentHandler extends BaseHandler {

    private final OpenShiftService openShiftService;
    private final UserConfigurationProperties properties;

    public static class TargetStateRequest {
        private final IntegrationDeploymentState targetState;

        @JsonCreator
        public TargetStateRequest(@JsonProperty("targetState") IntegrationDeploymentState targetState) {
            this.targetState = Objects.requireNonNull(targetState, "targetState");
        }

        public IntegrationDeploymentState getTargetState() {
            return targetState;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TargetStateRequest)) {
                return false;
            }

            return targetState == ((TargetStateRequest) obj).targetState;
        }

        @Override
        public int hashCode() {
            return targetState.hashCode();
        }
    }

    @Autowired
    public IntegrationDeploymentHandler(final DataManager dataMgr, OpenShiftService openShiftService,
            UserConfigurationProperties properties) {
        super(dataMgr);
        this.openShiftService = openShiftService;
        this.properties = properties;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{version}")
    public IntegrationDeployment get(@NotNull @PathParam("id") @Parameter(required = true) final String id,
        @NotNull @PathParam("version") @Parameter(required = true) final int version) {
        final String compositeId = IntegrationDeployment.compositeId(id, version);
        return getDataManager().fetch(IntegrationDeployment.class, compositeId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ListResult<IntegrationDeployment> list(
        @NotNull @PathParam("id") @Parameter(required = true) final String id,
        @Parameter(required = false, description = "Page number to return") @QueryParam("page") @DefaultValue("1") int page,
        @Parameter(required = false, description = "Number of records per page") @QueryParam("per_page") @DefaultValue("20") int perPage
    ) {
        return getDataManager().fetchAll(IntegrationDeployment.class,
            new IntegrationIdFilter(id),
            new PaginationFilter<>(new PaginationOptionsFromQueryParams(page, perPage))
        );
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public IntegrationDeployment update(@Context final SecurityContext sec,
        @NotNull @PathParam("id") @Parameter(required = true) final String id) {
        final DataManager dataManager = getDataManager();
        final Integration integration = dataManager.fetch(Integration.class, id);

        if (integration == null) {
            throw new EntityNotFoundException();
        }

        // Update previous deployments targetState=Undeployed and make sure
        // nextDeploymentVersion is larger than all previous ones.
        Optional<Integer> maybeLatestVersion = dataManager
            .fetchAllByPropertyValue(IntegrationDeployment.class, "integrationId", id).map(deployment -> {
                if (deployment.getTargetState() != IntegrationDeploymentState.Unpublished) {
                    dataManager.update(deployment.withTargetState(IntegrationDeploymentState.Unpublished));
                }

                return deployment.getVersion();
            }).max(Integer::compareTo);

        final int version = maybeLatestVersion.map(v -> v + 1).orElse(1);
        final Optional<String> validationStatus = validateQuotas(id, sec.getUserPrincipal().getName());
        final IntegrationDeployment deployment = new IntegrationDeployment.Builder()
            .id(IntegrationDeployment.compositeId(id, version)).spec(integration).version(version)
            .userId(sec.getUserPrincipal().getName())
            .createdAt(System.currentTimeMillis())
            .statusMessage(validationStatus).build();

        return getDataManager().create(deployment);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{version}/targetState")
    public void updateTargetState(@NotNull @PathParam("id") @Parameter(required = true) final String id,
        @NotNull @PathParam("version") @Parameter(required = true) final int version,
        @Parameter(required = true) final TargetStateRequest request) {

        final String compositeId = IntegrationDeployment.compositeId(id, version);
        final DataManager dataManager = getDataManager();
        final IntegrationDeployment current = dataManager.fetch(IntegrationDeployment.class, compositeId);

        final IntegrationDeployment updated = new IntegrationDeployment.Builder().createFrom(current)
            .targetState(request.getTargetState()).build();
        dataManager.update(updated);
    }

    private Optional<String> validateQuotas(String integrationId, String username) {
        Optional<String> validationStatus = Optional.empty();
        final int maxDeploymentsPerUser = properties.getMaxDeploymentsPerUser();
        if (maxDeploymentsPerUser != UserConfigurationProperties.UNLIMITED) {
            int userDeployments = countDeployments(integrationId, username);
            if (userDeployments >= maxDeploymentsPerUser) {
                validationStatus = Optional.of(
                   "WARNING: User has currently " + userDeployments +
                   " deployments, while the maximum allowed number is "
                   + maxDeploymentsPerUser + ".");
            }
        }
        return validationStatus;
    }
    /**
     * Count the deployments of the owner of the specified integration.
     *
     * @param deployment The specified IntegrationDeployment.
     * @return The number of deployed integrations (excluding the current).
     */
    private int countDeployments(String integrationId, String username) {

        Map<String, String> labels = new HashMap<>();
        labels.put(OpenShiftService.USERNAME_LABEL, Labels.sanitize(username));

        return (int) openShiftService.getDeploymentsByLabel(labels)
            .stream()
            .filter(d -> !integrationId.equals(d.getMetadata().getLabels().get(OpenShiftService.INTEGRATION_ID_LABEL)))
            .filter(d -> d.getSpec().getReplicas() > 0)
            .count();
    }

}
