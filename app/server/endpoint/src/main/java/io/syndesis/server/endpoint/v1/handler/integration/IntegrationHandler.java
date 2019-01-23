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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.validation.Validator;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.filter.FilterOptions;
import io.syndesis.common.model.filter.Op;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationOverview;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.common.model.validation.AllValidations;
import io.syndesis.server.api.generator.APIGenerator;
import io.syndesis.server.api.generator.APIIntegration;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.endpoint.util.PaginationFilter;
import io.syndesis.server.endpoint.util.ReflectiveSorter;
import io.syndesis.server.endpoint.v1.handler.BaseHandler;
import io.syndesis.server.endpoint.v1.handler.api.ApiGeneratorHelper;
import io.syndesis.server.endpoint.v1.handler.api.ApiHandler;
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

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static io.syndesis.server.endpoint.v1.handler.integration.IntegrationOverviewHelper.toCurrentIntegrationOverview;

@Path("/integrations")
@Api(value = "integrations")
@Component
public class IntegrationHandler extends BaseHandler implements Lister<IntegrationOverview>, Getter<IntegrationOverview>,
    Creator<Integration>, Deleter<Integration>, Updater<Integration>, Validating<Integration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationHandler.class);

    private final APIGenerator apiGenerator;
    private final EncryptionComponent encryptionSupport;
    private final Inspectors inspectors;
    private final OpenShiftService openShiftService;

    private final Validator validator;

    public IntegrationHandler(final DataManager dataMgr, final OpenShiftService openShiftService,
        final Validator validator, final Inspectors inspectors, final EncryptionComponent encryptionSupport,
        final APIGenerator apiGenerator) {
        super(dataMgr);
        this.openShiftService = openShiftService;
        this.validator = validator;
        this.inspectors = inspectors;
        this.encryptionSupport = encryptionSupport;
        this.apiGenerator = apiGenerator;
    }

    @Override
    public Integration create(@Context final SecurityContext sec,
        @ConvertGroup(from = Default.class, to = AllValidations.class) final Integration integration) {
        final Integration encryptedIntegration = encryptionSupport.encrypt(integration);

        final Integration updatedIntegration = new Integration.Builder().createFrom(encryptedIntegration)
            .createdAt(System.currentTimeMillis()).build();

        // Create the the integration.
        return getDataManager().create(updatedIntegration);
    }

    @Override
    public void delete(final String id) {
        final DataManager dataManager = getDataManager();

        // Set all status to Undeployed and specs as deleted for all deployments
        final Set<String> deploymentNames = dataManager.fetchAllByPropertyValue(IntegrationDeployment.class,
            "integrationId", id).map(deployment -> {
                final IntegrationDeployment unpublishedAndDeleted = deployment.unpublishing().deleted();
                dataManager.update(unpublishedAndDeleted);

                return deployment.getSpec().getName();
            }).collect(Collectors.toSet());

        final Integration existing = getIntegration(id);
        final Integration updatedIntegration = new Integration.Builder().createFrom(existing)
            .updatedAt(System.currentTimeMillis()).isDeleted(true).build();

        // delete ALL versions
        for (final String name : deploymentNames) {
            try {
                openShiftService.delete(name);
            } catch (final KubernetesClientException e) {
                LOGGER.error("Error deleting integration deployment {}: {}", name, e.getMessage());
            }
        }

        Updater.super.update(id, updatedIntegration);
    }

    @Override
    public IntegrationOverview get(final String id) {
        final DataManager dataManager = getDataManager();
        final Integration integration = dataManager.fetch(Integration.class, id);

        if (integration == null) {
            throw new EntityNotFoundException();
        }

        if (integration.isDeleted()) {
            // Not sure if we need to do that for both current and desired
            // status,
            // but If we don't do include the desired state, IntegrationITCase
            // is not going to pass anytime soon. Why?
            // Cause that test, is using NoopHandlerProvider, so that means no
            // controllers.
            throw new EntityNotFoundException(String.format("Integration %s has been deleted", integration.getId()));
        }

        return toCurrentIntegrationOverview(integration, dataManager);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/filters/options")
    public FilterOptions getFilterOptions(final DataShape dataShape) {
        final FilterOptions.Builder builder = new FilterOptions.Builder().addOp(Op.DEFAULT_OPTS);

        final List<String> paths = inspectors.getPaths(dataShape.getKind().toString(), dataShape.getType(),
            dataShape.getSpecification(), dataShape.getExemplar());
        builder.paths(paths);
        return builder.build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/filters/options")
    public FilterOptions getGlobalFilterOptions() {
        return new FilterOptions.Builder().addOp(Op.DEFAULT_OPTS).build();
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

        return ListResult.of(integrations.getItems().stream().map(i -> toCurrentIntegrationOverview(i, dataManager))
            .collect(Collectors.toList()));
    }

    @Override
    public Kind resourceKind() {
        return Kind.Integration;
    }

    @Override
    public void update(final String id,
        @ConvertGroup(from = Default.class, to = AllValidations.class) final Integration integration) {
        final Integration existing = getIntegration(id);

        Integration updatedIntegration = new Integration.Builder().createFrom(encryptionSupport.encrypt(integration))
            .version(existing.getVersion() + 1).updatedAt(System.currentTimeMillis()).build();

        updatedIntegration = apiGenerator.updateFlowExcerpts(updatedIntegration);

        getDataManager().update(updatedIntegration);
    }

    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/specification")
    @ApiOperation("For an integration that is generated from a specification updates it so it conforms to the updated specification")
    public void updateSpecification(@PathParam("id") final String id, @MultipartForm final ApiHandler.APIFormData apiFormData) {
        final APIIntegration apiIntegration = ApiGeneratorHelper.generateIntegrationFrom(apiFormData, getDataManager(), apiGenerator);

        final Integration givenIntegration = apiIntegration.getIntegration();

        final Integration existing = getIntegration(id);

        final Integration updated = updateFlowsAndStartAndEndDataShapes(existing, givenIntegration);

        if (existing.getFlows().equals(updated.getFlows())) {
            // no changes were made to the flows
            return;
        }

        // store the OpenAPI resource, we keep the old one as it might
        // be referenced from Integration's stored in IntegrationDeployent's
        // this gives us a rollback mechanism
        getDataManager().store(apiIntegration.getSpec(), OpenApi.class);

        // perform the regular update
        update(id, updated);
    }

    static Integration updateFlowsAndStartAndEndDataShapes(final Integration existing, final Integration given) {
        // will contain updated flows
        final List<Flow> updatedFlows = new ArrayList<>(given.getFlows().size());

        for (final Flow givenFlow : given.getFlows()) {
            final String flowId = givenFlow.getId().get();

            final Optional<Flow> maybeExistingFlow = existing.findFlowById(flowId);
            if (!maybeExistingFlow.isPresent()) {
                // this is a flow generated from a new operation or it
                // has it's operation id changed, either way we only
                // need to add it, since we don't know what flow we need
                // to update
                updatedFlows.add(givenFlow);
                continue;
            }


            final List<Step> givenSteps = givenFlow.getSteps();
            if (givenSteps.size() != 2) {
                throw new IllegalArgumentException("Expecting to get exactly two steps per flow");
            }

            // this is a freshly minted flow from the specification
            // there should be only two steps (start and end) in the
            // flow
            final Step givenStart = givenSteps.get(0);
            final Optional<DataShape> givenStartDataShape = givenStart.outputDataShape();

            // generated flow has only a start and an end, start is at 0
            // and the end is at 1
            final Step givenEnd = givenSteps.get(1);
            final Optional<DataShape> givenEndDataShape = givenEnd.inputDataShape();

            final Flow existingFlow = maybeExistingFlow.get();
            final List<Step> existingSteps = existingFlow.getSteps();

            // readability
            final int start = 0;
            final int end = existingSteps.size() - 1;

            // now we update the data shapes of the start and end steps
            final Step existingStart = existingSteps.get(start);
            final Step updatedStart = existingStart.updateOutputDataShape(givenStartDataShape);

            final Step existingEnd = existingSteps.get(end);
            final Step updatedEnd = existingEnd.updateInputDataShape(givenEndDataShape);

            final List<Step> updatedSteps = new ArrayList<>(existingSteps);
            updatedSteps.set(start, updatedStart);
            updatedSteps.set(end, updatedEnd);

            final Flow updatedFlow = existingFlow.builder()
                .name(givenFlow.getName())
                .description(givenFlow.getDescription())
                .steps(updatedSteps)
                .build();
            updatedFlows.add(updatedFlow);
        }

        return existing.builder()
            .flows(updatedFlows)
            // we replace all resources counting that the only resource
            // present is the OpenAPI specification
            .resources(given.getResources())
            .build();
    }
}
