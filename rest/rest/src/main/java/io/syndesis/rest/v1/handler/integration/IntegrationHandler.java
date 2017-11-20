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
package io.syndesis.rest.v1.handler.integration;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.persistence.EntityNotFoundException;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.syndesis.core.Json;
import io.syndesis.dao.init.ModelData;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.inspector.Inspectors;
import io.syndesis.model.DataShape;
import io.syndesis.model.Kind;
import io.syndesis.model.ListResult;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.filter.FilterOptions;
import io.syndesis.model.filter.Op;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.Integration.Status;
import io.syndesis.model.integration.IntegrationRevision;
import io.syndesis.model.integration.IntegrationRevisionState;
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

import static io.syndesis.rest.v1.handler.integration.IntegrationSupportHandler.EXPORT_MODEL_FILE_NAME;


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

        if (Status.Deleted.equals(integration.getCurrentStatus().get()) ||
            Status.Deleted.equals(integration.getDesiredStatus().get())) {
            //Not sure if we need to do that for both current and desired status,
            //but If we don't do include the desired state, IntegrationITCase is not going to pass anytime soon. Why?
            //Cause that test, is using NoopHandlerProvider, so that means no controllers.
            throw new EntityNotFoundException(String.format("Integration %s has been deleted", integration.getId()));
        }

        //fudging the timesUsed for now
        Optional<Status> currentStatus = integration.getCurrentStatus();
        if (currentStatus.isPresent() && currentStatus.get() == Integration.Status.Activated) {
            return new Integration.Builder()
                    .createFrom(integration)
                    .timesUsed(BigInteger.valueOf(new Date().getTime()/1000000))
                    .build();
        }

        return integration;
    }


    @GET
    @Path("/{id}/export.zip")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput export(@NotNull @PathParam("id") @ApiParam(required = true) String id) throws IOException {
        ArrayList<ModelData> models = new ArrayList<>();

        Integration integration = this.get(id);
        models.add(new ModelData(Kind.Integration, integration));

        for (Step step : integration.getSteps()) {
            Optional<Connection> c = step.getConnection();
            if( c.isPresent() ) {
                Connection connection = c.get();
                models.add(new ModelData(Kind.Connection, connection));
                Connector connector = getDataManager().fetch(Connector.class, connection.getConnectorId().get());
                if( connector != null ) {
                    models.add(new ModelData(Kind.Connector, connector));
                }
            }
        }

        return out -> {
            try (ZipOutputStream tos = new ZipOutputStream(out) ) {
                addEntry(tos, EXPORT_MODEL_FILE_NAME, Json.mapper().writeValueAsBytes(models));
                // Eventually we might need to add things like tech extensions too..
            }
        };
    }

    private void addEntry(ZipOutputStream os, String path, byte[] content) throws IOException {
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

        IntegrationRevision revision = IntegrationRevision
            .createNewRevision(encryptedIntegration)
            .withCurrentState(IntegrationRevisionState.Draft);

        Integration updatedIntegration = new Integration.Builder()
            .createFrom(encryptedIntegration)
            .deployedRevisionId(revision.getVersion())
            .addRevision(revision)
            .userId(SecurityContextHolder.getContext().getAuthentication().getName())
            .statusMessage(Optional.empty())
            .lastUpdated(rightNow)
            .createdDate(rightNow)
            .currentStatus(determineCurrentStatus(encryptedIntegration))
            .userId(sec.getUserPrincipal().getName())
            .build();

        return Creator.super.create(sec, updatedIntegration);
    }

    @Override
    public void update(String id, @ConvertGroup(from = Default.class, to = AllValidations.class) Integration integration) {
        Integration existing = Getter.super.get(id);

        Status currentStatus = determineCurrentStatus(integration);
        IntegrationRevision currentRevision = IntegrationRevision.deployedRevision(existing)
            .withCurrentState(IntegrationRevisionState.from(currentStatus))
            .withTargetState(IntegrationRevisionState.from(integration.getDesiredStatus().orElse(Status.Pending)));

        Integration updatedIntegration = new Integration.Builder()
            .createFrom(encryptionSupport.encrypt(integration))
            .deployedRevisionId(existing.getDeployedRevisionId())
            .lastUpdated(new Date())
            .currentStatus(currentStatus)
            .addRevision(currentRevision)
            .build();

        Updater.super.update(id, updatedIntegration);
    }


    @Override
    public void delete(String id) {
         Integration existing = Getter.super.get(id);

        Status currentStatus = determineCurrentStatus(existing);
        IntegrationRevision currentRevision = IntegrationRevision.deployedRevision(existing)
            .withCurrentState(IntegrationRevisionState.from(currentStatus))
            .withTargetState(IntegrationRevisionState.from(Status.Deleted));

        Integration updatedIntegration = new Integration.Builder()
            .createFrom(existing)
            .deployedRevisionId(existing.getDeployedRevisionId())
            .lastUpdated(new Date())
            .desiredStatus(Status.Deleted)
            .addRevision(currentRevision)
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
    private Integration.Status determineCurrentStatus(Integration integration) {
        Integration.Status desiredStatus = integration.getDesiredStatus().orElse(Integration.Status.Draft);
        return desiredStatus == Integration.Status.Draft ?
            Integration.Status.Draft :
            Integration.Status.Pending;
    }

    @Override
    public Validator getValidator() {
        return validator;
    }


    private static class DeletedFilter implements Function<ListResult<Integration>, ListResult<Integration>> {
        @Override
        public ListResult<Integration> apply(ListResult<Integration> list) {
            List<Integration> filtered = list.getItems().stream()
                    .filter(i -> !Status.Deleted.equals(i.getCurrentStatus().get()))
                    .filter(i -> !Status.Deleted.equals(i.getDesiredStatus().get()))
                    .collect(Collectors.toList());

            return new ListResult.Builder<Integration>()
                .totalCount(filtered.size())
                .addAllItems(filtered).build();
        }
    }
}
