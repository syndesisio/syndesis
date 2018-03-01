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

import static io.syndesis.model.buletin.LeveledMessage.Level.ERROR;
import static io.syndesis.model.buletin.LeveledMessage.Level.WARN;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;
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

import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.syndesis.core.SuppressFBWarnings;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.dao.manager.EncryptionComponent;
import io.syndesis.dao.manager.operators.IdPrefixFilter;
import io.syndesis.dao.manager.operators.ReverseFilter;
import io.syndesis.inspector.Inspectors;
import io.syndesis.model.DataShape;
import io.syndesis.model.Kind;
import io.syndesis.model.ListResult;
import io.syndesis.model.buletin.IntegrationBulletinBoard;
import io.syndesis.model.buletin.LeveledMessage;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.filter.FilterOptions;
import io.syndesis.model.filter.Op;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.model.integration.IntegrationDeploymentState;
import io.syndesis.model.integration.Step;
import io.syndesis.model.validation.AllValidations;
import io.syndesis.rest.util.PaginationFilter;
import io.syndesis.rest.util.ReflectiveSorter;
import io.syndesis.rest.v1.handler.BaseHandler;
import io.syndesis.rest.v1.handler.integration.model.DeploymentOverview;
import io.syndesis.rest.v1.handler.integration.model.IntegrationOverview;
import io.syndesis.rest.v1.operations.Creator;
import io.syndesis.rest.v1.operations.Deleter;
import io.syndesis.rest.v1.operations.Getter;
import io.syndesis.rest.v1.operations.Lister;
import io.syndesis.rest.v1.operations.PaginationOptionsFromQueryParams;
import io.syndesis.rest.v1.operations.SortOptionsFromQueryParams;
import io.syndesis.rest.v1.operations.Updater;
import io.syndesis.rest.v1.operations.Validating;

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
        if ( integration.isDeleted() ) {
            //Not sure if we need to do that for both current and desired status,
            //but If we don't do include the desired state, IntegrationITCase is not going to pass anytime soon. Why?
            //Cause that test, is using NoopHandlerProvider, so that means no controllers.
            throw new EntityNotFoundException(String.format("Integration %s has been deleted", integration.getId()));
        }

        // Get the latest connection configs.
        List<Connection> connections = integration.getConnections().stream()
            .map(this::toCurrentConnection)
            .collect(Collectors.toList());

        List<Step> steps = integration.getSteps().stream()
            .map(this::toCurrentSteps)
            .collect(Collectors.toList());

        return new Integration.Builder()
            .createFrom(integration)
            .connections(connections)
            .steps(steps)
            .build();
    }

    public Connection toCurrentConnection(Connection c) {
        Connection connection = getDataManager().fetch(Connection.class, c.getId().get());
        if (connection.getConnectorId().isPresent()) {
            final Connector connector = getDataManager().fetch(Connector.class, connection.getConnectorId().get());
            connection = new Connection.Builder().createFrom(connection).connector(connector).build();
        }
        return connection;
    }

    public Step toCurrentSteps(Step step) {
        Step.Builder from = new Step.Builder().createFrom(step);
        step.getConnection().ifPresent(c->{
            from.connection(toCurrentConnection(c));
        });
        return from.build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/{id}/overview")
    public IntegrationOverview getOverview(@PathParam("id") final String id) {
        Integration integration = Getter.super.get(id);
        List<IntegrationDeployment> deployments = getDataManager().fetchAll(IntegrationDeployment.class,
            new IdPrefixFilter<>(id+":"), ReverseFilter.getInstance())
            .getItems();

        Optional<IntegrationBulletinBoard> bulletins = Optional
            .ofNullable(getDataManager().fetch(IntegrationBulletinBoard.class, id));

        return new IntegrationOverview(integration, bulletins, deployments.stream().filter(d -> d.getVersion() == integration.getVersion()).findFirst()) {
            @Override
            public List<DeploymentOverview> getDeployments() {
                return deployments.stream()
                    .map(x->new DeploymentOverview(x))
                    .collect(Collectors.toList());
            }
        };
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
        return getDataManager().fetchAll(Integration.class,
            new DeletedFilter(),
            new ReflectiveSorter<>(clazz, new SortOptionsFromQueryParams(uriInfo)),
            new PaginationFilter<>(new PaginationOptionsFromQueryParams(uriInfo))
        );
    }

    @Override
    public Integration create(@Context SecurityContext sec, @ConvertGroup(from = Default.class, to = AllValidations.class) final Integration integration) {

        Integration encryptedIntegration = encryptionSupport.encrypt(integration);

        Integration updatedIntegration = new Integration.Builder()
            .createFrom(encryptedIntegration)
            .createdAt(System.currentTimeMillis())
            .build();

        // Create the the integration.
        return getDataManager().create(updatedIntegration);
    }

    @Override
    public void update(String id, @ConvertGroup(from = Default.class, to = AllValidations.class) Integration integration) {
        Integration existing = Getter.super.get(id);

        Integration updatedIntegration = new Integration.Builder()
            .createFrom(encryptionSupport.encrypt(integration))
            .version(existing.getVersion()+1)
            .updatedAt(System.currentTimeMillis())
            .build();

        getDataManager().update(updatedIntegration);
        updateBulletinBoard(id);
    }


    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/deployments")
    public IntegrationDeployment putDeployment(@Context SecurityContext sec, @NotNull @PathParam("id") @ApiParam(required = true) String id) {
        Integration integration = Getter.super.get(id);

        int nextDeploymentVersion = 1;

        // Update previous deployments targetState=Undeployed and make sure nextDeploymentVersion is larger than all previous ones.
        Set<String> deploymentIds = getDataManager().fetchIdsByPropertyValue(IntegrationDeployment.class, "integrationId", id);
        if (deploymentIds != null && !deploymentIds.isEmpty()) {
            Stream<IntegrationDeployment> deployments = deploymentIds.stream()
                .map(i -> getDataManager().fetch(IntegrationDeployment.class, i))
                .filter(r -> r != null);
            for (IntegrationDeployment d : deployments.toArray(IntegrationDeployment[]::new)) {
                nextDeploymentVersion = Math.max(nextDeploymentVersion, d.getVersion()+1);
                getDataManager().update(d.withTargetState(IntegrationDeploymentState.Unpublished));
            }
        }

        IntegrationDeployment deployment = new IntegrationDeployment.Builder()
            .id(IntegrationDeployment.compositeId(id, nextDeploymentVersion))
            .spec(integration)
            .version(nextDeploymentVersion)
            // .userId(SecurityContextHolder.getContext().getAuthentication().getName())
            .userId(sec.getUserPrincipal().getName())
            .build();

        deployment = getDataManager().create(deployment);
        return deployment;
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
                .map(r -> r.withTargetState(IntegrationDeploymentState.Unpublished))
                .forEach(r -> getDataManager().update(r));
        }

        Integration updatedIntegration = new Integration.Builder()
            .createFrom(existing)
            .updatedAt(System.currentTimeMillis())
            .isDeleted(true)
            .build();

        Updater.super.update(id, updatedIntegration);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/filters/options")
    public FilterOptions getFilterOptions(DataShape dataShape) {
        FilterOptions.Builder builder = new FilterOptions.Builder().addOp(Op.DEFAULT_OPTS);

        final List<String> paths = inspectors.getPaths(dataShape.getKind().toString(), dataShape.getType(), dataShape.getSpecification(), dataShape.getExemplar());
        builder.paths(paths);
        return builder.build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/filters/options")
    public FilterOptions getGlobalFilterOptions() {
        return new FilterOptions.Builder().addOp(Op.DEFAULT_OPTS).build();
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
        return getDataManager().fetch(IntegrationDeployment.class, compositeId);
    }

    @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public static class TargetStateRequest {
        public IntegrationDeploymentState targetState;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/deployments/{version}/targetState")
    @SuppressFBWarnings("UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
    public void setTargetStatus(
        @NotNull @PathParam("id") @ApiParam(required = true) String id,
        @NotNull @PathParam("version") @ApiParam(required = true) Integer version,
        TargetStateRequest request) {

        String compositeId = IntegrationDeployment.compositeId(id, version);
        IntegrationDeployment deployment = getDataManager().fetch(IntegrationDeployment.class, compositeId);
        deployment = new IntegrationDeployment.Builder().createFrom(deployment).targetState(request.targetState).build();
        getDataManager().update(deployment);
    }

    @Override
    public Validator getValidator() {
        return validator;
    }

    static class IntegrationIdFilter implements Function<ListResult<IntegrationDeployment>, ListResult<IntegrationDeployment>> {

        private final String integrationId;

        IntegrationIdFilter(String integrationId) {
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

    /**
     * Update the list of notices for a given integration
     *
     * @param id
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/update-bulletins")
    public IntegrationBulletinBoard updateBulletinBoard(@NotNull @PathParam("id") @ApiParam(required = true) String id) {

        Integration savedIntegration = getDataManager().fetch(Integration.class, id);
        if( savedIntegration == null ) {
            throw new EntityNotFoundException();
        }
        Integration integrationWithUpdatedConnections = get(id);

        List<LeveledMessage> messages = new ArrayList<>();
        final Set<ConstraintViolation<Integration>> constraintViolations = getValidator().validate(integrationWithUpdatedConnections, AllValidations.class);
        for (ConstraintViolation<Integration> violation : constraintViolations) {
            messages.add(LeveledMessage.of(ERROR, violation.getMessage()));
        }

        if( !savedIntegration.toJson().equals(integrationWithUpdatedConnections.toJson()) ) {
            messages.add(LeveledMessage.of(WARN, "Connections updated."));
        }

        IntegrationBulletinBoard bulletinBoard = IntegrationBulletinBoard.of(id, messages);
        getDataManager().set(bulletinBoard);
        return bulletinBoard;
    }

}
