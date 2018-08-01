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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityNotFoundException;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ActionDescriptor;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.action.StepDescriptor;
import io.syndesis.common.model.bulletin.IntegrationBulletinBoard;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.filter.FilterOptions;
import io.syndesis.common.model.filter.Op;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentOverview;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.model.integration.IntegrationEndpoint;
import io.syndesis.common.model.integration.IntegrationOverview;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.validation.AllValidations;
import io.syndesis.common.util.SuppressFBWarnings;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.dao.manager.operators.IdPrefixFilter;
import io.syndesis.server.dao.manager.operators.ReverseFilter;
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
import io.syndesis.server.endpoint.v1.util.DataManagerSupport;
import io.syndesis.server.inspector.Inspectors;
import io.syndesis.server.openshift.OpenShiftService;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

@SuppressWarnings("PMD.GodClass")
@Path("/integrations")
@Api(value = "integrations")
@Component
public class IntegrationHandler extends BaseHandler
    implements Lister<IntegrationOverview>, Getter<IntegrationOverview>, Creator<Integration>, Deleter<Integration>, Updater<Integration>, Validating<Integration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationHandler.class);

    private final OpenShiftService openShiftService;
    private final Inspectors inspectors;
    private final EncryptionComponent encryptionSupport;

    private final Validator validator;

    public IntegrationHandler(final DataManager dataMgr, OpenShiftService openShiftService, final Validator validator, final Inspectors inspectors, final EncryptionComponent encryptionSupport) {
        super(dataMgr);
        this.openShiftService = openShiftService;
        this.validator = validator;
        this.inspectors = inspectors;
        this.encryptionSupport = encryptionSupport;
    }

    @Override
    public Kind resourceKind() {
        return Kind.Integration;
    }

    @Override
    public IntegrationOverview get(String id) {
        final DataManager dataManager = getDataManager();
        final Integration integration = dataManager.fetch(Integration.class, id);

        if (integration == null) {
            throw new EntityNotFoundException();
        }

        if (integration.isDeleted()) {
            //Not sure if we need to do that for both current and desired status,
            //but If we don't do include the desired state, IntegrationITCase is not going to pass anytime soon. Why?
            //Cause that test, is using NoopHandlerProvider, so that means no controllers.
            throw new EntityNotFoundException(String.format("Integration %s has been deleted", integration.getId()));
        }

        return toCurrentIntegrationOverview(integration);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/{id}/overview")
    public IntegrationOverview getOverview(@PathParam("id") final String id) {
        return get(id);
    }

    @Override
    public ListResult<IntegrationOverview> list(UriInfo uriInfo) {
        ListResult<Integration> integrations = getDataManager().fetchAll(Integration.class,
            new DeletedFilter(),
            new ReflectiveSorter<>(Integration.class, new SortOptionsFromQueryParams(uriInfo)),
            new PaginationFilter<>(new PaginationOptionsFromQueryParams(uriInfo))
        );

        return ListResult.of(
            integrations.getItems().stream().map(this::toCurrentIntegrationOverview).collect(Collectors.toList())
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
        Integration existing = getIntegration(id);

        Integration updatedIntegration = new Integration.Builder()
            .createFrom(encryptionSupport.encrypt(integration))
            .version(existing.getVersion()+1)
            .updatedAt(System.currentTimeMillis())
            .build();

        getDataManager().update(updatedIntegration);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/deployments")
    public IntegrationDeployment putDeployment(@Context SecurityContext sec, @NotNull @PathParam("id") @ApiParam(required = true) String id) {
        Integration integration = getIntegration(id);

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
        Integration existing = getIntegration(id);

        //Set all status to Undeployed and specs as deleted for all deployments
        Set<String> deploymentIds = getDataManager().fetchIdsByPropertyValue(IntegrationDeployment.class, "integrationId", existing.getId().get());
        Set<String> deploymentNames = new HashSet<>();
        if (deploymentIds != null && !deploymentIds.isEmpty()) {
            deploymentIds.stream()
                .map(i -> getDataManager().fetch(IntegrationDeployment.class, i))
                .filter(r -> r != null)
                .map(r -> r.unpublishing().deleted())
                .forEach(r -> {
                    getDataManager().update(r);
                    deploymentNames.add(r.getSpec().getName());
                });
        }

        Integration updatedIntegration = new Integration.Builder()
            .createFrom(existing)
            .updatedAt(System.currentTimeMillis())
            .isDeleted(true)
            .build();

        // delete ALL versions
        for (String name : deploymentNames) {
            try {
                openShiftService.delete(name);
            } catch (KubernetesClientException e) {
                LOGGER.error("Error deleting integration deployment {}: {}", name, e.getMessage());
            }
        }

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

    // **************************
    // Helpers
    // **************************

    public Integration getIntegration(String id) {
        final DataManager dataManager = getDataManager();
        final Integration integration = dataManager.fetch(Integration.class, id);

        if( integration == null ) {
            throw new EntityNotFoundException();
        }

        return integration;
    }

    private IntegrationOverview toCurrentIntegrationOverview(Integration integration) {
        final DataManager dataManager = getDataManager();
        final String id = integration.getId().get();
        final IntegrationOverview.Builder builder = new IntegrationOverview.Builder().createFrom(integration);

        // add board
        DataManagerSupport.fetchBoard(dataManager, IntegrationBulletinBoard.class, id).ifPresent(builder::board);

        // Defaults
        builder.isDraft(true);
        builder.currentState(IntegrationDeploymentState.Unpublished);
        builder.targetState(IntegrationDeploymentState.Unpublished);

        // Get the latest connection.
        builder.connections(integration.getConnections().stream()
            .map(this::toCurrentConnection)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList()));

        // Get the latest steps.
        builder.steps(integration.getSteps().stream().map(this::toCurrentSteps).collect(Collectors.toList()));

        IntegrationDeployment deployed = null;
        List<IntegrationDeployment> activeDeployments = new ArrayList<>();
        for (IntegrationDeployment deployment: dataManager.fetchAll(IntegrationDeployment.class, new IdPrefixFilter<>(id+":"), ReverseFilter.getInstance())) {
            builder.addDeployment(IntegrationDeploymentOverview.of(deployment));

            final IntegrationDeploymentState currentState = deployment.getCurrentState();
            if (currentState == IntegrationDeploymentState.Published) {
                deployed = deployment;
                builder.deploymentVersion(deployment.getVersion());
            }

            if (currentState != IntegrationDeploymentState.Unpublished) {
                // the bet is that any integration that the user wanted to publish
                // will have it's status != Unpublished, the reason why we can't
                // look at the last deployment is because users can choose to deploy
                // previous deployments, so we bet that all the Unpublished
                // integrations are not the ones that the user don't hold the
                // current state
                builder.targetState(deployment.getTargetState());
                builder.currentState(currentState);
                activeDeployments.add(deployment);
            }
        }

        if (deployed != null) {
            builder.isDraft(computeDraft(integration, deployed.getSpec()));
        }

        // Set the URL of the integration deployment if present
        IntegrationDeployment exposedDeployment = deployed;
        if (exposedDeployment == null && activeDeployments.size() == 1) {
            exposedDeployment = activeDeployments.get(0);
        }
        if (exposedDeployment != null && exposedDeployment.getId().isPresent()) {
            IntegrationEndpoint endpoint = dataManager.fetch(IntegrationEndpoint.class, exposedDeployment.getId().get());
            if (endpoint != null) {
                builder.url(endpoint.getUrl());
            }
        }

        return builder.build();
    }

    private static boolean computeDraft(final Integration current, final Integration deployed) {
        final List<Step> currentSteps = current.getSteps();
        final List<Step> deployedSteps = deployed.getSteps();
        if (currentSteps.size() != deployedSteps.size()) {
            return true;
        }

        for (int i = 0; i < currentSteps.size(); i++) {
            final Step currentStep = currentSteps.get(i);
            final Step deployedStep = deployedSteps.get(i);

            if (currentStep.getStepKind() != deployedStep.getStepKind()) {
                return true;
            }

            if (!currentStep.getConfiguredProperties().equals(deployedStep.getConfiguredProperties())) {
                return true;
            }
        }

        return false;
    }

    private Optional<Connection> toCurrentConnection(Connection c) {
        final DataManager dataManager = getDataManager();

        final Connection connection = dataManager.fetch(Connection.class, c.getId().get());
        if (connection == null) {
            // this may happen when a connection has been deleted
            return Optional.empty();
        }

        final Connector connector = dataManager.fetch(Connector.class, c.getConnectorId());
        if (connector == null) {
            // this may happen when the related connector has been deleted
            return Optional.empty();
        }

        return Optional.of(
            new Connection.Builder().createFrom(connection).connector(connector).build()
        );
    }

    private Optional<Extension> toCurrentExtension(Extension e) {
        final DataManager dataManager = getDataManager();

        // Try to lookup the active extensions
        Set<String> ids = dataManager.fetchIdsByPropertyValue(Extension.class,
            "extensionId", e.getExtensionId(),
            "status", Extension.Status.Installed.name()
        );

        // This could happen if an extension has been deleted
        if (ids.isEmpty()) {
            return Optional.empty();
        }

        // This could happen if errors happened while activating an extension
        // leading more than one extension marked as installed
        if (ids.size() > 1) {
            return Optional.empty();
        }

        return Optional.ofNullable(
            dataManager.fetch(Extension.class, ids.iterator().next())
        );
    }

    public Step toCurrentSteps(Step step) {
        // actualize the connection
        final Optional<Connection> connection = step.getConnection().flatMap(this::toCurrentConnection);

        // A connection has been deleted
        if (step.getConnection().isPresent() && !connection.isPresent()) {
            // ... so reset the step to its un-configured state
            return new Step.Builder().stepKind(step.getStepKind()).build();
        }

        // actualize the extension
        final Optional<Extension> extension = step.getExtension().flatMap(this::toCurrentExtension);

        // An extension has been deleted
        if (step.getExtension().isPresent() && !extension.isPresent()) {
            // ... so reset the step to its un-configured state
            return new Step.Builder().stepKind(step.getStepKind()).build();
        }

        // We now need to update the related action
        Optional<? extends Action> action = step.getAction();

        if (action.isPresent() && action.get().hasId()) {
            final Action oldAction = action.get();

            // connector
            if (connection.isPresent() && connection.get().getConnector().isPresent()) {
                action = connection.get().getConnector().get().findActionById(oldAction.getId().get()).map(
                    newAction -> merge(oldAction, newAction)
                );

            }

            // extension
            if (extension.isPresent()) {
                action = extension.get().findActionById(oldAction.getId().get()).map(
                    newAction -> merge(oldAction, newAction)
                );
            }
        }

        return new Step.Builder()
            .createFrom(step)
            .action(action)
            .connection(connection)
            .extension(extension)
            .build();
    }

    /*
     * https://github.com/syndesisio/syndesis/issues/2247
     *
     * When the UI ask for an integration its component (connector, extensions,
     * etc) are updated and moved to their latest version and this work just
     * fine for static actions. For dynamic action (for which metadata is retrieved
     * from thr syndesis-meta service), this is problematic as the action is
     * replaced with its latest version thus all the enriched data are lost.
     *
     * This _ UGLY _ hack tries to merge old and new actions but it does not take
     * into account properties that may have updated, so if a new property has
     * i.e. new default values, the new default is not taken into account and the
     * old ConfigurationProperty is left untouched.
     */
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public Action merge(Action oldAction, Action newAction) {
        if (newAction instanceof ConnectorAction) {
            // to real type
            final ConnectorAction oldConnectorAction = (ConnectorAction)oldAction;
            final ConnectorAction newConnectorAction = (ConnectorAction)newAction;

            // descriptor
            ConnectorDescriptor.Builder descriptorBuilder = new ConnectorDescriptor.Builder().createFrom(newAction.getDescriptor());

            // reset properties
            descriptorBuilder.propertyDefinitionSteps(Collections.emptyList());

            // data shapes
            oldAction.getDescriptor().getInputDataShape().ifPresent(descriptorBuilder::inputDataShape);
            oldAction.getDescriptor().getOutputDataShape().ifPresent(descriptorBuilder::outputDataShape);

            Map<String, ActionDescriptor.ActionDescriptorStep> oldDefinitions = oldConnectorAction.getDescriptor().getPropertyDefinitionStepsAsMap();
            Map<String, ActionDescriptor.ActionDescriptorStep> newDefinitions = newConnectorAction.getDescriptor().getPropertyDefinitionStepsAsMap();

            for (Map.Entry<String, ActionDescriptor.ActionDescriptorStep> entry: newDefinitions.entrySet()) {
                ActionDescriptor.ActionDescriptorStep newDefinition = entry.getValue();
                ActionDescriptor.ActionDescriptorStep oldDefinition = oldDefinitions.get(entry.getKey());

                ActionDescriptor.ActionDescriptorStep.Builder b;

                if (oldDefinition != null) {
                    b = new ActionDescriptor.ActionDescriptorStep.Builder();
                    b.name(oldDefinition.getName());
                    b.configuredProperties(oldDefinition.getConfiguredProperties());

                    Map<String, ConfigurationProperty> oldProperties = oldDefinition.getProperties();
                    Map<String, ConfigurationProperty> newProperties = newDefinition.getProperties();

                    // Add new or properties in common
                    MapDifference<String, ConfigurationProperty> diff = Maps.difference(oldProperties, newProperties);
                    diff.entriesInCommon().forEach(b::putProperty);
                    diff.entriesDiffering().forEach((k, v) -> b.putProperty(k, v.leftValue()));
                    diff.entriesOnlyOnRight().forEach(b::putProperty);
                } else {
                    b = new ActionDescriptor.ActionDescriptorStep.Builder().createFrom(newDefinition);
                }

                descriptorBuilder.addPropertyDefinitionStep(b.build());
            }

            return new ConnectorAction.Builder()
                .createFrom(newAction)
                .descriptor(descriptorBuilder.build())
                .build();
        }

        if (newAction instanceof StepAction) {
            // to real type
            final StepAction oldStepAction = (StepAction)oldAction;
            final StepAction newStepAction = (StepAction)newAction;

            // descriptor
            StepDescriptor.Builder descriptorBuilder = new StepDescriptor.Builder().createFrom(newAction.getDescriptor());

            // reset properties
            descriptorBuilder.propertyDefinitionSteps(Collections.emptyList());

            // data shapes
            oldAction.getDescriptor().getInputDataShape().ifPresent(descriptorBuilder::inputDataShape);
            oldAction.getDescriptor().getOutputDataShape().ifPresent(descriptorBuilder::outputDataShape);

            Map<String, ActionDescriptor.ActionDescriptorStep> oldDefinitions = oldStepAction.getDescriptor().getPropertyDefinitionStepsAsMap();
            Map<String, ActionDescriptor.ActionDescriptorStep> newDefinitions = newStepAction.getDescriptor().getPropertyDefinitionStepsAsMap();

            for (Map.Entry<String, ActionDescriptor.ActionDescriptorStep> entry: newDefinitions.entrySet()) {
                ActionDescriptor.ActionDescriptorStep newDefinition = entry.getValue();
                ActionDescriptor.ActionDescriptorStep oldDefinition = oldDefinitions.get(entry.getKey());

                ActionDescriptor.ActionDescriptorStep.Builder b;

                if (oldDefinition != null) {
                    b = new ActionDescriptor.ActionDescriptorStep.Builder();
                    b.name(oldDefinition.getName());
                    b.configuredProperties(oldDefinition.getConfiguredProperties());

                    Map<String, ConfigurationProperty> oldProperties = oldDefinition.getProperties();
                    Map<String, ConfigurationProperty> newProperties = newDefinition.getProperties();

                    // Add new or properties in common
                    MapDifference<String, ConfigurationProperty> diff = Maps.difference(oldProperties, newProperties);
                    diff.entriesInCommon().forEach(b::putProperty);
                    diff.entriesOnlyOnRight().forEach(b::putProperty);
                } else {
                    b = new ActionDescriptor.ActionDescriptorStep.Builder().createFrom(newDefinition);
                }

                descriptorBuilder.addPropertyDefinitionStep(b.build());
            }

            return new StepAction.Builder()
                .createFrom(newAction)
                .descriptor(descriptorBuilder.build())
                .build();
        }

        return newAction;
    }
}
