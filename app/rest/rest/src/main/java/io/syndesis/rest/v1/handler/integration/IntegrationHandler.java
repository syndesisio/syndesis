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
package io.syndesis.rest.v1.handler.integration;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.persistence.EntityNotFoundException;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.inspector.Inspectors;
import io.syndesis.model.DataShape;
import io.syndesis.model.Kind;
import io.syndesis.model.ListResult;
import io.syndesis.model.filter.FilterOptions;
import io.syndesis.model.filter.Op;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.model.integration.IntegrationDeploymentState;
import io.syndesis.model.integration.IntegrationHistory;
import io.syndesis.model.validation.AllValidations;
import io.syndesis.rest.util.PaginationFilter;
import io.syndesis.rest.util.ReflectiveSorter;
import io.syndesis.rest.v1.handler.BaseHandler;
import io.syndesis.rest.v1.operations.Creator;
import io.syndesis.rest.v1.operations.Deleter;
import io.syndesis.rest.v1.operations.Getter;
import io.syndesis.rest.v1.operations.Lister;
import io.syndesis.rest.v1.operations.PaginationOptionsFromQueryParams;
import io.syndesis.rest.v1.operations.SortOptionsFromQueryParams;
import io.syndesis.rest.v1.operations.Updater;
import io.syndesis.rest.v1.operations.Validating;
import io.syndesis.dao.manager.EncryptionComponent;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Path("/integrations")
@Api(value = "integrations")
@Component
public class IntegrationHandler extends BaseHandler
    implements Lister<Integration>, Getter<Integration>, Creator<Integration>, Deleter<Integration>, Updater<Integration>, Validating<Integration> {

    private final Inspectors inspectors;
    private final EncryptionComponent encryptionSupport;

    private final Validator validator;

    public IntegrationHandler(final DataManager dataMgr, final Validator validator, final Inspectors inspectors, final EncryptionComponent encryptionSupport) {
        super(dataMgr);
        this.validator = validator;
        this.inspectors = inspectors;
        this.encryptionSupport = encryptionSupport;
    }

    @Override
    public Kind resourceKind() {
        return Kind.Integration;
    }

    @Override
    public Integration get(String id) {
        Integration integration = Getter.super.get(id);

        if (IntegrationDeploymentState.Undeployed.equals(integration.getCurrentStatus().get()) ||
            IntegrationDeploymentState.Undeployed.equals(integration.getDesiredStatus().get())) {
            //Not sure if we need to do that for both current and desired status,
            //but If we don't do include the desired state, IntegrationITCase is not going to pass anytime soon. Why?
            //Cause that test, is using NoopHandlerProvider, so that means no controllers.
            throw new EntityNotFoundException(String.format("Integration %s has been deleted", integration.getId()));
        }

        //fudging the timesUsed for now
        Optional<IntegrationDeploymentState> currentStatus = integration.getCurrentStatus();
        if (currentStatus.isPresent() && currentStatus.get() ==  IntegrationDeploymentState.Active) {
            return new Integration.Builder()
                    .createFrom(integration)
                    .timesUsed(BigInteger.valueOf(new Date().getTime()/1000000))
                    .build();
        }

        return integration;
    }

    public static void addEntry(ZipOutputStream os, String path, byte[] content) throws IOException {
        ZipEntry entry = new ZipEntry(path);
        entry.setSize(content.length);
        os.putNextEntry(entry);
        os.write(content);
        os.closeEntry();
    }

    @Override
    public ListResult<Integration> list(UriInfo uriInfo) {
        Class<Integration> clazz = resourceKind().getModelClass();
        return getDataManager().fetchAll(
            Integration.class,
            new DeletedFilter(),
            new ReflectiveSorter<>(clazz, new SortOptionsFromQueryParams(uriInfo)),
            new PaginationFilter<>(new PaginationOptionsFromQueryParams(uriInfo))
        );
    }

    @Override
    public Integration create(@Context SecurityContext sec, @ConvertGroup(from = Default.class, to = AllValidations.class) final Integration integration) {
        Date rightNow = new Date();

        Integration encryptedIntegration = encryptionSupport.encrypt(integration);

        Integration updatedIntegration = new Integration.Builder()
            .createFrom(encryptedIntegration)
            .userId(SecurityContextHolder.getContext().getAuthentication().getName())
            .statusMessage(Optional.empty())
            .lastUpdated(rightNow)
            .createdDate(rightNow)
            .currentStatus(determineCurrentState(encryptedIntegration))
            .userId(sec.getUserPrincipal().getName())
            .stepsDone(new ArrayList<>())
            .build();

        updatedIntegration = Creator.super.create(sec, updatedIntegration);

        if (integration.getDesiredStatus().orElse(IntegrationDeploymentState.Draft).equals(IntegrationDeploymentState.Active)) {
            IntegrationDeployment integrationDeployment = IntegrationDeployment
                .newDeployment(updatedIntegration)
                .withCurrentState(IntegrationDeploymentState.Draft)
                .withTargetState(IntegrationDeploymentState.Active);

            getDataManager().create(integrationDeployment);
        }
        return updatedIntegration;
    }

    @Override
    public void update(String id, @ConvertGroup(from = Default.class, to = AllValidations.class) Integration integration) {
        Integration existing = Getter.super.get(id);

        IntegrationDeploymentState currentState = determineCurrentState(integration);
        IntegrationDeploymentState targetState = integration.getDesiredStatus().orElse(IntegrationDeploymentState.Active);

        IntegrationDeployment latest = latestDeployment(integration).orElse(null);


        switch (targetState) {
            case Active:
                IntegrationDeployment newDeployment = new IntegrationDeployment.Builder()
                    .createFrom(IntegrationDeployment.newDeployment(existing))
                    .version(latest != null ? latest.getVersion().orElse(0) + 1 : 1)
                    .targetState(IntegrationDeploymentState.Active)
                    .currentState(IntegrationDeploymentState.Draft)
                    .build();

                getDataManager().create(newDeployment);
                break;
            case Undeployed:
                String compositeId = IntegrationDeployment.compositeId(id, existing.getDeploymentId().orElse(1));
                IntegrationDeployment activeDeployment = getDataManager().fetch(IntegrationDeployment.class, compositeId);
                if (activeDeployment != null && activeDeployment.getCurrentState() != IntegrationDeploymentState.Undeployed) {
                    getDataManager().create(activeDeployment.withTargetState(IntegrationDeploymentState.Undeployed));
                }
                break;
            default:
               //Just ignore and do nothing
        }

        Integration updatedIntegration = new Integration.Builder()
            .createFrom(encryptionSupport.encrypt(integration))
            .deploymentId(existing.getDeploymentId())
            .lastUpdated(new Date())
            .currentStatus(currentState)
            .stepsDone(new ArrayList<>())
            .build();

        Updater.super.update(id, updatedIntegration);
    }


    @Override
    public void delete(String id) {
        Integration existing = Getter.super.get(id);

        //Set all integration status to Undeployed.
        Set<String> deploymentIds = getDataManager().fetchIdsByPropertyValue(IntegrationDeployment.class, "integrationId", existing.getId().get());

        if (deploymentIds != null && !deploymentIds.isEmpty()) {
            deploymentIds.stream()
                .map(i -> getDataManager().fetch(IntegrationDeployment.class, i))
                .filter(r -> r != null)
                .map(r -> r.withTargetState(IntegrationDeploymentState.Undeployed))
                .forEach(r -> getDataManager().update(r));
        }

        //for (IntegrationDeployment r : getDataManager().fetchAll(IntegrationDeployment.class).getItems()) {
        //    getDataManager().delete(IntegrationDeployment.class, r.getId().get());
        //}

        Integration updatedIntegration = new Integration.Builder()
            .createFrom(existing)
            .deploymentId(existing.getDeploymentId())
            .lastUpdated(new Date())
            .desiredStatus(IntegrationDeploymentState.Undeployed)
            .stepsDone(new ArrayList<>())
            .build();

        Updater.super.update(id, updatedIntegration);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/filters/options")
    public FilterOptions getFilterOptions(DataShape dataShape) {
        FilterOptions.Builder builder = new FilterOptions.Builder().addOp(Op.DEFAULT_OPTS);

        final List<String> paths = inspectors.getPaths(dataShape.getKind(), dataShape.getType(), dataShape.getSpecification(), dataShape.getExemplar());
        builder.paths(paths);
        return builder.build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/filters/options")
    public FilterOptions getGlobalFilterOptions() {
        return new FilterOptions.Builder().addOp(Op.DEFAULT_OPTS).build();
    }

    // Determine the current status to 'pending' or 'draft' immediately depending on
    // the desired stated. This status will be later changed by the activation handlers.
    // This is not the best place to set but should be done by the IntegrationController
    // However because of how the Controller works (i.e. that any change to the integration
    // within the controller will trigger an event again), the initial status must be set
    // from the outside for the moment.
    private IntegrationDeploymentState determineCurrentState(Integration integration) {
        IntegrationDeploymentState desiredStatus = integration.getDesiredStatus().orElse(IntegrationDeploymentState.Draft);
        return desiredStatus == IntegrationDeploymentState.Draft ?
            IntegrationDeploymentState.Draft :
            IntegrationDeploymentState.Pending;
    }

    // Determine the current status to 'pending' or 'draft' immediately depending on
    // the desired stated. This status will be later changed by the activation handlers.
    // This is not the best place to set but should be done by the IntegrationController
    // However because of how the Controller works (i.e. that any change to the integration
    // within the controller will trigger an event again), the initial status must be set
    // from the outside for the moment.
    private IntegrationDeploymentState determineCurrentState(IntegrationDeployment integrationDeployment) {
        IntegrationDeploymentState state = integrationDeployment.getTargetState();
        return state == IntegrationDeploymentState.Draft ?
            IntegrationDeploymentState.Draft :
            IntegrationDeploymentState.Pending;
    }

    private Optional<IntegrationDeployment> latestDeployment(Integration integration) {
         //Set all integration status to Undeployed.
        return getDataManager().fetchIdsByPropertyValue(IntegrationDeployment.class, "integrationId", integration.getId().get())
            .stream()
            .map(i -> getDataManager().fetch(IntegrationDeployment.class, i))
            .max( (r,l) -> r.getVersion().orElse(0) - l.getVersion().orElse(0) );
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/history")
    public IntegrationHistory history(@Context UriInfo uriInfo) {
        String integrationId = uriInfo.getPathParameters().getFirst("id");

        return new IntegrationHistory.Builder()
            .deployments(getDataManager().fetchAll(IntegrationDeployment.class, new IntegrationIdFilter(integrationId)).getItems())
            .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/deployments")
    public ListResult<IntegrationDeployment> listDeployments(@Context UriInfo uriInfo) {
        String integrationId = uriInfo.getPathParameters().getFirst("id");

        return getDataManager().fetchAll(IntegrationDeployment.class,
            new IntegrationIdFilter(integrationId),
            new ReflectiveSorter<>(IntegrationDeployment.class, new SortOptionsFromQueryParams(uriInfo)),
            new PaginationFilter<>(new PaginationOptionsFromQueryParams(uriInfo)));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/deployments/{version}")
    public IntegrationDeployment getDeployment(@NotNull @PathParam("id") @ApiParam(required = true) String id, @NotNull @PathParam("version") @ApiParam(required = true) Integer version) {
        String compositeId = IntegrationDeployment.compositeId(id, version);
        IntegrationDeployment integrationDeployment = getDataManager().fetch(IntegrationDeployment.class, compositeId);
        return integrationDeployment;
    }

    @DELETE
    @Consumes("application/json")
    @Path("/{id}/deployments/{version}")
    public void delete(@NotNull @PathParam("id") @ApiParam(required = true) String id, @NotNull @PathParam("version") @ApiParam(required = true) Integer version) {
        String compositeId = IntegrationDeployment.compositeId(id, version);
        IntegrationDeployment existing = getDataManager().fetch(IntegrationDeployment.class, compositeId);

        IntegrationDeploymentState currentState = determineCurrentState(existing);
        IntegrationDeployment updatedIntegrationDeployment = new IntegrationDeployment.Builder()
            .createFrom(existing)
            .lastUpdated(new Date())
            .currentState(currentState)
            .targetState(IntegrationDeploymentState.Undeployed)
            .stepsDone(new ArrayList<>())
            .build();

        getDataManager().update(updatedIntegrationDeployment);
    }

    @Override
    public Validator getValidator() {
        return validator;
    }


    private static class DeletedFilter implements Function<ListResult<Integration>, ListResult<Integration>> {
        @Override
        public ListResult<Integration> apply(ListResult<Integration> list) {
            List<Integration> filtered = list.getItems().stream()
                    .filter(i -> !IntegrationDeploymentState.Undeployed.equals(i.getCurrentStatus().get()))
                    .filter(i -> !IntegrationDeploymentState.Undeployed.equals(i.getDesiredStatus().get()))
                    .collect(Collectors.toList());

            return new ListResult.Builder<Integration>()
                .totalCount(filtered.size())
                .addAllItems(filtered).build();
        }
    }

    private static class IntegrationIdFilter implements Function<ListResult<IntegrationDeployment>, ListResult<IntegrationDeployment>> {

        private final String integrationId;

        private IntegrationIdFilter(String integrationId) {
            this.integrationId = integrationId;
        }

        @Override
        public ListResult<IntegrationDeployment> apply(ListResult<IntegrationDeployment> list) {
            List<IntegrationDeployment> filtered = list.getItems().stream()
                .filter(i -> integrationId == null || integrationId.equals(i.getIntegrationId().orElse(null)))
                .collect(Collectors.toList());

            return new ListResult.Builder<IntegrationDeployment>()
                .totalCount(filtered.size())
                .addAllItems(filtered).build();
        }
    }
}
