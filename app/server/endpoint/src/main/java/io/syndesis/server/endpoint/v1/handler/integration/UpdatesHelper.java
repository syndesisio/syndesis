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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ActionDescriptor;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.action.StepDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Step;
import io.syndesis.server.dao.manager.DataManager;

final class UpdatesHelper {

    private UpdatesHelper() {
        // helper class
    }

    static Optional<Connection> toCurrentConnection(final Connection c, final DataManager dataManager) {
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

        return Optional.of(new Connection.Builder().createFrom(connection).connector(connector).build());
    }

    static Optional<Extension> toCurrentExtension(final Extension e, final DataManager dataManager) {
        // Try to lookup the active extensions
        final Set<String> ids = dataManager.fetchIdsByPropertyValue(Extension.class, "extensionId", e.getExtensionId(),
            "status", Extension.Status.Installed.name());

        // This could happen if an extension has been deleted
        if (ids.isEmpty()) {
            return Optional.empty();
        }

        // This could happen if errors happened while activating an extension
        // leading more than one extension marked as installed
        if (ids.size() > 1) {
            return Optional.empty();
        }

        return Optional.ofNullable(dataManager.fetch(Extension.class, ids.iterator().next()));
    }

    static Flow toCurrentFlow(final Flow f, final DataManager dataManager) {
        return new Flow.Builder().createFrom(f)
            .connections(f.getConnections().stream().map(c -> toCurrentConnection(c, dataManager))
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()))
            .steps(f.getSteps().stream().map(s -> toCurrentSteps(s, dataManager)).collect(Collectors.toList())).build();
    }

    static Step toCurrentSteps(final Step step, final DataManager dataManager) {
        // actualize the connection
        final Optional<Connection> connection = step.getConnection().flatMap(c -> toCurrentConnection(c, dataManager));

        // A connection has been deleted
        if (step.getConnection().isPresent() && !connection.isPresent()) {
            // ... so reset the step to its un-configured state
            return new Step.Builder().stepKind(step.getStepKind()).build();
        }

        // actualize the extension
        final Optional<Extension> extension = step.getExtension().flatMap(e -> toCurrentExtension(e, dataManager));

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
                action = connection.get().getConnector().get().findActionById(oldAction.getId().get())
                    .map(newAction -> merge(oldAction, newAction));

            }

            // extension
            if (extension.isPresent()) {
                action = extension.get().findActionById(oldAction.getId().get())
                    .map(newAction -> merge(oldAction, newAction));
            }
        }

        return new Step.Builder().createFrom(step).action(action).connection(connection).extension(extension).build();
    }

    /*
     * https://github.com/syndesisio/syndesis/issues/2247
     *
     * When the UI ask for an integration its component (connector, extensions,
     * etc) are updated and moved to their latest version and this work just
     * fine for static actions. For dynamic action (for which metadata is
     * retrieved from the syndesis-meta service), this is problematic as the
     * action is replaced with its latest version thus all the enriched data are
     * lost.
     *
     * This _ UGLY _ hack tries to merge old and new actions but it does not
     * take into account properties that may have updated, so if a new property
     * has i.e. new default values, the new default is not taken into account
     * and the old ConfigurationProperty is left untouched.
     */
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    private static Action merge(final Action oldAction, final Action newAction) {
        if (newAction instanceof ConnectorAction) {
            // to real type
            final ConnectorAction oldConnectorAction = (ConnectorAction) oldAction;
            final ConnectorAction newConnectorAction = (ConnectorAction) newAction;

            // descriptor
            final ConnectorDescriptor.Builder descriptorBuilder = new ConnectorDescriptor.Builder()
                .createFrom(newAction.getDescriptor());

            // reset properties
            descriptorBuilder.propertyDefinitionSteps(Collections.emptyList());

            // data shapes
            oldAction.getDescriptor().getInputDataShape().ifPresent(descriptorBuilder::inputDataShape);
            oldAction.getDescriptor().getOutputDataShape().ifPresent(descriptorBuilder::outputDataShape);

            final Map<String, ActionDescriptor.ActionDescriptorStep> oldDefinitions = oldConnectorAction.getDescriptor()
                .getPropertyDefinitionStepsAsMap();
            final Map<String, ActionDescriptor.ActionDescriptorStep> newDefinitions = newConnectorAction.getDescriptor()
                .getPropertyDefinitionStepsAsMap();

            for (final Map.Entry<String, ActionDescriptor.ActionDescriptorStep> entry : newDefinitions.entrySet()) {
                final ActionDescriptor.ActionDescriptorStep newDefinition = entry.getValue();
                final ActionDescriptor.ActionDescriptorStep oldDefinition = oldDefinitions.get(entry.getKey());

                ActionDescriptor.ActionDescriptorStep.Builder b;

                if (oldDefinition != null) {
                    b = new ActionDescriptor.ActionDescriptorStep.Builder();
                    b.name(oldDefinition.getName());
                    b.configuredProperties(oldDefinition.getConfiguredProperties());

                    final Map<String, ConfigurationProperty> oldProperties = oldDefinition.getProperties();
                    final Map<String, ConfigurationProperty> newProperties = newDefinition.getProperties();

                    // Add new or properties in common
                    final MapDifference<String, ConfigurationProperty> diff = Maps.difference(oldProperties,
                        newProperties);
                    diff.entriesInCommon().forEach(b::putProperty);
                    diff.entriesDiffering().forEach((k, v) -> b.putProperty(k, v.leftValue()));
                    diff.entriesOnlyOnRight().forEach(b::putProperty);
                } else {
                    b = new ActionDescriptor.ActionDescriptorStep.Builder().createFrom(newDefinition);
                }

                descriptorBuilder.addPropertyDefinitionStep(b.build());
            }

            return new ConnectorAction.Builder().createFrom(newAction).descriptor(descriptorBuilder.build()).metadata(oldAction.getMetadata()).build();
        }

        if (newAction instanceof StepAction) {
            // to real type
            final StepAction oldStepAction = (StepAction) oldAction;
            final StepAction newStepAction = (StepAction) newAction;

            // descriptor
            final StepDescriptor.Builder descriptorBuilder = new StepDescriptor.Builder()
                .createFrom(newAction.getDescriptor());

            // reset properties
            descriptorBuilder.propertyDefinitionSteps(Collections.emptyList());

            // data shapes
            oldAction.getDescriptor().getInputDataShape().ifPresent(descriptorBuilder::inputDataShape);
            oldAction.getDescriptor().getOutputDataShape().ifPresent(descriptorBuilder::outputDataShape);

            final Map<String, ActionDescriptor.ActionDescriptorStep> oldDefinitions = oldStepAction.getDescriptor()
                .getPropertyDefinitionStepsAsMap();
            final Map<String, ActionDescriptor.ActionDescriptorStep> newDefinitions = newStepAction.getDescriptor()
                .getPropertyDefinitionStepsAsMap();

            for (final Map.Entry<String, ActionDescriptor.ActionDescriptorStep> entry : newDefinitions.entrySet()) {
                final ActionDescriptor.ActionDescriptorStep newDefinition = entry.getValue();
                final ActionDescriptor.ActionDescriptorStep oldDefinition = oldDefinitions.get(entry.getKey());

                ActionDescriptor.ActionDescriptorStep.Builder b;

                if (oldDefinition != null) {
                    b = new ActionDescriptor.ActionDescriptorStep.Builder();
                    b.name(oldDefinition.getName());
                    b.configuredProperties(oldDefinition.getConfiguredProperties());

                    final Map<String, ConfigurationProperty> oldProperties = oldDefinition.getProperties();
                    final Map<String, ConfigurationProperty> newProperties = newDefinition.getProperties();

                    // Add new or properties in common
                    final MapDifference<String, ConfigurationProperty> diff = Maps.difference(oldProperties,
                        newProperties);
                    diff.entriesInCommon().forEach(b::putProperty);
                    diff.entriesOnlyOnRight().forEach(b::putProperty);
                } else {
                    b = new ActionDescriptor.ActionDescriptorStep.Builder().createFrom(newDefinition);
                }

                descriptorBuilder.addPropertyDefinitionStep(b.build());
            }

            return new StepAction.Builder().createFrom(newAction).descriptor(descriptorBuilder.build()).metadata(oldAction.getMetadata()).build();
        }

        return newAction;
    }
}
