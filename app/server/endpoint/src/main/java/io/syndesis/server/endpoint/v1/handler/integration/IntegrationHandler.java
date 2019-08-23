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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import javax.validation.Validator;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.swagger.annotations.Api;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.bulletin.IntegrationBulletinBoard;
import io.syndesis.common.model.filter.FilterOptions;
import io.syndesis.common.model.filter.Op;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationOverview;
import io.syndesis.server.api.generator.APIGenerator;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.endpoint.util.PaginationFilter;
import io.syndesis.server.endpoint.util.ReflectiveSorter;
import io.syndesis.server.endpoint.v1.handler.BaseHandler;
import io.syndesis.server.endpoint.v1.operations.Creator;
import io.syndesis.server.endpoint.v1.operations.Deleter;
import io.syndesis.server.endpoint.v1.operations.Getter;
import io.syndesis.server.endpoint.v1.operations.Lister;
import io.syndesis.server.endpoint.v1.operations.PaginationOptionsFromQueryParams;
import io.syndesis.server.endpoint.v1.operations.SortOptionsFromQueryParams;
import io.syndesis.server.endpoint.v1.operations.Updater;
import io.syndesis.server.endpoint.v1.operations.Validating;
import io.syndesis.server.inspector.Inspectors;
import io.syndesis.server.openshift.OpenShiftService;

@Path("/integrations")
@Api(value = "integrations")
@Component
public class IntegrationHandler extends BaseHandler implements Lister<IntegrationOverview>, Getter<IntegrationOverview>,
    Creator<Integration>, Deleter<Integration>, Updater<Integration>, Validating<Integration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationHandler.class);
    final APIGenerator apiGenerator;
    private final EncryptionComponent encryptionSupport;
    private final Inspectors inspectors;
    private final OpenShiftService openShiftService;
    private final IntegrationOverviewHelper integrationOverviewHelper;


    private final Validator validator;

    public IntegrationHandler(final DataManager dataMgr, final OpenShiftService openShiftService,
                              final Validator validator, final Inspectors inspectors,
                              final EncryptionComponent encryptionSupport, final APIGenerator apiGenerator,
                              final IntegrationOverviewHelper integrationOverviewHelper) {
        super(dataMgr);
        this.openShiftService = openShiftService;
        this.validator = validator;
        this.inspectors = inspectors;
        this.encryptionSupport = encryptionSupport;
        this.apiGenerator = apiGenerator;
        this.integrationOverviewHelper = integrationOverviewHelper;
    }

    @Override
    public Integration create(@Context final SecurityContext sec, final Integration integration) {
        final Integration encryptedIntegration = encryptionSupport.encrypt(integration);

        final Integration updatedIntegration = new Integration.Builder().createFrom(encryptedIntegration)
            .createdAt(System.currentTimeMillis()).build();

        // Create the the integration.
        return getDataManager().create(updatedIntegration);
    }

    @Override
    public void delete(final String id) {
        final DataManager dataManager = getDataManager();

        // Delete all deployments
        final Set<String> deploymentNames = dataManager.fetchAllByPropertyValue(IntegrationDeployment.class,
            "integrationId", id).map(deployment -> {
                String name = deployment.getSpec().getName();
                String depId = deployment.getId().orElse(null);
                if (depId != null) {
                    dataManager.delete(IntegrationDeployment.class, depId);
                }
                return name;
            }).collect(Collectors.toSet());

        // Delete all integration bulletin boards
        dataManager.fetchIdsByPropertyValue(IntegrationBulletinBoard.class, "targetResourceId", id)
            .forEach(ibbId -> dataManager.delete(IntegrationBulletinBoard.class, ibbId));

        // delete ALL versions
        for (final String name : deploymentNames) {
            try {
                openShiftService.delete(name);
            } catch (final KubernetesClientException e) {
                LOGGER.error("Error deleting integration deployment {}: {}", name, e.getMessage());
            }
        }

        Deleter.super.delete(id);
    }

    @Override
    public IntegrationOverview get(final String id) {
        final Integration integration = getIntegration(id);

        if (integration.isDeleted()) {
            // Not sure if we need to do that for both current and desired
            // status,
            // but If we don't do include the desired state, IntegrationITCase
            // is not going to pass anytime soon. Why?
            // Cause that test, is using NoopHandlerProvider, so that means no
            // controllers.
            throw new EntityNotFoundException(String.format("Integration %s has been deleted", integration.getId()));
        }

        return integrationOverviewHelper.toCurrentIntegrationOverview(integration);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/filters/options")
    public FilterOptions getFilterOptions(final DataShape dataShape) {
        final FilterOptions.Builder builder = new FilterOptions.Builder().addOps(Op.DEFAULT_OPTS);

        final List<String> paths = inspectors.getPaths(dataShape.getKind().toString(), dataShape.getType(),
            dataShape.getSpecification(), dataShape.getExemplar());
        builder.paths(paths);
        return builder.build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/filters/options")
    public FilterOptions getGlobalFilterOptions() {
        return new FilterOptions.Builder().addOps(Op.DEFAULT_OPTS).build();
    }

    public Integration getIntegration(final String id) {
        final DataManager dataManager = getDataManager();
        final Integration integration = dataManager.fetch(Integration.class, id);

        if (integration == null) {
            throw new EntityNotFoundException();
        }

        return integration;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/{id}/overview")
    public IntegrationOverview getOverview(@PathParam("id") final String id) {
        return get(id);
    }

    @Override
    public Validator getValidator() {
        return validator;
    }

    @Override
    public ListResult<IntegrationOverview> list(final UriInfo uriInfo) {
        final DataManager dataManager = getDataManager();
        final ListResult<Integration> integrations = dataManager.fetchAll(Integration.class, new DeletedFilter(),
            new ReflectiveSorter<>(Integration.class, new SortOptionsFromQueryParams(uriInfo)),
            new PaginationFilter<>(new PaginationOptionsFromQueryParams(uriInfo)));

        return ListResult.of(integrations.getItems().stream().map(i -> integrationOverviewHelper.toCurrentIntegrationOverview(i))
            .collect(Collectors.toList()));
    }

    @Override
    public Kind resourceKind() {
        return Kind.Integration;
    }

    @Override
    public void update(final String id, final Integration integration) {
        final Integration existing = getIntegration(id);

        Integration updatedIntegration = new Integration.Builder().createFrom(encryptionSupport.encrypt(integration))
            .version(existing.getVersion() + 1).updatedAt(System.currentTimeMillis()).build();

        updatedIntegration = apiGenerator.updateFlowExcerpts(updatedIntegration);

        getDataManager().update(updatedIntegration);
    }

}
