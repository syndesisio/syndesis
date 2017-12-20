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
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.Api;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.inspector.Inspectors;
import io.syndesis.model.DataShape;
import io.syndesis.model.Kind;
import io.syndesis.model.ListResult;
import io.syndesis.model.filter.FilterOptions;
import io.syndesis.model.filter.Op;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.IntegrationRevision;
import io.syndesis.model.integration.IntegrationRevisionState;
import io.syndesis.model.integration.IntegrationStatus;
import io.syndesis.model.integration.Step;
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

        if (IntegrationRevisionState.Undeployed.equals(integration.getCurrentStatus().get()) ||
            IntegrationRevisionState.Undeployed.equals(integration.getDesiredStatus().get())) {
            //Not sure if we need to do that for both current and desired status,
            //but If we don't do include the desired state, IntegrationITCase is not going to pass anytime soon. Why?
            //Cause that test, is using NoopHandlerProvider, so that means no controllers.
            throw new EntityNotFoundException(String.format("Integration %s has been deleted", integration.getId()));
        }

        //fudging the timesUsed for now
        Optional<IntegrationRevisionState> currentStatus = integration.getCurrentStatus();
        if (currentStatus.isPresent() && currentStatus.get() ==  IntegrationRevisionState.Active) {
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

        if (integration.getDesiredStatus().orElse(IntegrationRevisionState.Draft).equals(IntegrationRevisionState.Active)) {

            IntegrationRevision revision = IntegrationRevision
                .createNewRevision(updatedIntegration)
                .withCurrentState(IntegrationRevisionState.Draft)
                .withTargetState(IntegrationRevisionState.Active);

            getDataManager().create(revision);
        }
        return updatedIntegration;
    }

    @Override
    public void update(String id, @ConvertGroup(from = Default.class, to = AllValidations.class) Integration integration) {
        Integration existing = Getter.super.get(id);

        IntegrationRevisionState currentStatus = determineCurrentState(integration);
        IntegrationRevision latest = latestRevision(integration).orElse(null);

        IntegrationRevision updatedRevision = new IntegrationRevision.Builder()
            .createFrom(IntegrationRevision.createNewRevision(existing))
            .version(latest != null ? latest.getVersion().orElse(0) + 1 : 1)
            .parentVersion(latest != null ? latest.getVersion().orElse(0) : 0)
            .build();

        Integration updatedIntegration = new Integration.Builder()
            .createFrom(encryptionSupport.encrypt(integration))
            .deployedRevisionId(existing.getDeployedRevisionId())
            .lastUpdated(new Date())
            .currentStatus(currentStatus)
            .addRevision(updatedRevision)
            .stepsDone(new ArrayList<>())
            .build();

        Updater.super.update(id, updatedIntegration);
    }


    @Override
    public void delete(String id) {
        Integration existing = Getter.super.get(id);

        //Set all integration status to Undeployed.
        Set<String> revisionIds = getDataManager().fetchIdsByPropertyValue(IntegrationRevision.class, "integrationId", existing.getId().get());

        if (revisionIds != null && !revisionIds.isEmpty()) {
            revisionIds.stream()
                .map(i -> getDataManager().fetch(IntegrationRevision.class, i))
                .filter(r -> r != null)
                .map(r -> r.withTargetState(IntegrationRevisionState.Undeployed))
                .forEach(r -> getDataManager().update(r));
        }

        //for (IntegrationRevision r : getDataManager().fetchAll(IntegrationRevision.class).getItems()) {
        //    getDataManager().delete(IntegrationRevision.class, r.getId().get());
        //}

        Integration updatedIntegration = new Integration.Builder()
            .createFrom(existing)
            .deployedRevisionId(existing.getDeployedRevisionId())
            .lastUpdated(new Date())
            .desiredStatus(IntegrationRevisionState.Undeployed)
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
    private IntegrationRevisionState determineCurrentState(Integration integration) {
        IntegrationRevisionState desiredStatus = integration.getDesiredStatus().orElse(IntegrationRevisionState.Draft);
        return desiredStatus == IntegrationRevisionState.Draft ?
            IntegrationRevisionState.Draft :
            IntegrationRevisionState.Pending;
    }

    // Determine the current status to 'pending' or 'draft' immediately depending on
    // the desired stated. This status will be later changed by the activation handlers.
    // This is not the best place to set but should be done by the IntegrationController
    // However because of how the Controller works (i.e. that any change to the integration
    // within the controller will trigger an event again), the initial status must be set
    // from the outside for the moment.
    private IntegrationRevisionState determineCurrentState(IntegrationRevision integrationRevision) {
        IntegrationRevisionState state = integrationRevision.getTargetState();
        return state == IntegrationRevisionState.Draft ?
            IntegrationRevisionState.Draft :
            IntegrationRevisionState.Pending;
    }

    private Optional<IntegrationRevision> latestRevision(Integration integration) {
         //Set all integration status to Undeployed.
        return getDataManager().fetchIdsByPropertyValue(IntegrationRevision.class, "integrationId", integration.getId().get())
            .stream()
            .map(i -> getDataManager().fetch(IntegrationRevision.class, i))
            .max( (r,l) -> r.getVersion().orElse(0) - l.getVersion().orElse(0) );
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/status")
    public IntegrationStatus status(@Context UriInfo uriInfo) {
        String integrationId = uriInfo.getPathParameters().getFirst("id");

        return new IntegrationStatus.Builder()
            .deployments(getDataManager().fetchAll(IntegrationRevision.class, new IntegrationIdFilter(integrationId)).getItems())
            .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/revisions")
    public ListResult<IntegrationRevision> listRevisions(@Context UriInfo uriInfo) {
        String integrationId = uriInfo.getPathParameters().getFirst("id");

        return getDataManager().fetchAll(IntegrationRevision.class,
            new IntegrationIdFilter(integrationId),
            new ReflectiveSorter<>(IntegrationRevision.class, new SortOptionsFromQueryParams(uriInfo)),
            new PaginationFilter<>(new PaginationOptionsFromQueryParams(uriInfo)));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/revisions/{version}")
    public IntegrationRevision getRevision(@NotNull @PathParam("id") @ApiParam(required = true) String id, @NotNull @PathParam("version") @ApiParam(required = true) Integer version) {
        String compositeId = IntegrationRevision.compositeId(id, version);
        IntegrationRevision integrationRevision = getDataManager().fetch(IntegrationRevision.class, compositeId);
        return integrationRevision;
    }

    @DELETE
    @Consumes("application/json")
    @Path("/{id}/revisions/{version}")
    public void delete(@NotNull @PathParam("id") @ApiParam(required = true) String id, @NotNull @PathParam("version") @ApiParam(required = true) Integer version) {
        String compositeId = IntegrationRevision.compositeId(id, version);
        IntegrationRevision existing = getDataManager().fetch(IntegrationRevision.class, compositeId);

        IntegrationRevisionState currentState = determineCurrentState(existing);
        IntegrationRevision updatedIntegrationRevision = new IntegrationRevision.Builder()
            .createFrom(existing)
            .lastUpdated(new Date())
            .currentState(currentState)
            .targetState(IntegrationRevisionState.Undeployed)
            .stepsDone(new ArrayList<>())
            .build();

        getDataManager().update(updatedIntegrationRevision);
    }

    @Override
    public Validator getValidator() {
        return validator;
    }


    private static class DeletedFilter implements Function<ListResult<Integration>, ListResult<Integration>> {
        @Override
        public ListResult<Integration> apply(ListResult<Integration> list) {
            List<Integration> filtered = list.getItems().stream()
                    .filter(i -> !IntegrationRevisionState.Undeployed.equals(i.getCurrentStatus().get()))
                    .filter(i -> !IntegrationRevisionState.Undeployed.equals(i.getDesiredStatus().get()))
                    .collect(Collectors.toList());

            return new ListResult.Builder<Integration>()
                .totalCount(filtered.size())
                .addAllItems(filtered).build();
        }
    }

    private static class IntegrationIdFilter implements Function<ListResult<IntegrationRevision>, ListResult<IntegrationRevision>> {

        private final String integrationId;

        private IntegrationIdFilter(String integrationId) {
            this.integrationId = integrationId;
        }

        @Override
        public ListResult<IntegrationRevision> apply(ListResult<IntegrationRevision> list) {
            List<IntegrationRevision> filtered = list.getItems().stream()
                .filter(i -> integrationId == null || integrationId.equals(i.getIntegrationId().orElse(null)))
                .collect(Collectors.toList());

            return new ListResult.Builder<IntegrationRevision>()
                .totalCount(filtered.size())
                .addAllItems(filtered).build();
        }
    }
}
