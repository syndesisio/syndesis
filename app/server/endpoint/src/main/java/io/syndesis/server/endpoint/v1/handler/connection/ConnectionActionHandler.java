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
package io.syndesis.server.endpoint.v1.handler.connection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.netflix.hystrix.HystrixExecutable;
import com.netflix.hystrix.HystrixInvokableInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.ConnectionBase;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.connection.DynamicActionMetadata;
import io.syndesis.common.util.RandomValueGenerator;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.endpoint.v1.dto.Meta;
import io.syndesis.server.verifier.MetadataConfigurationProperties;

@Api(value = "actions")
public class ConnectionActionHandler {
    public static final DataShape ANY_SHAPE = new DataShape.Builder().kind(DataShapeKinds.ANY).build();

    public static final DataShape NO_SHAPE = new DataShape.Builder().kind(DataShapeKinds.NONE).build();

    private final List<ConnectorAction> actions;

    private final MetadataConfigurationProperties config;

    private final ConnectionBase connection;

    private final String connectorId;

    private final EncryptionComponent encryptionComponent;

    public ConnectionActionHandler(final ConnectionBase connection, final MetadataConfigurationProperties config,
                                   final EncryptionComponent encryptionComponent) {
        this.connection = connection;
        this.config = config;
        this.encryptionComponent = encryptionComponent;

        final Optional<Connector> maybeConnector = connection.getConnector();
        final Connector connector = maybeConnector.orElseThrow(
            () -> new EntityNotFoundException("Connection with id `" + connection.getId() + "` does not have a Connector defined"));

        connectorId = connector.getId().get();

        actions = connector.getActions().stream()//
            .filter(ConnectorAction.class::isInstance)//
            .map(ConnectorAction.class::cast)//
            .collect(Collectors.toList());
    }

    @POST
    @Path(value = "/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Retrieves enriched action definition, that is an action definition that has input/output data shapes and property enums defined with respect to the given action properties")
    @ApiResponses(@ApiResponse(code = 200, reference = "#/definitions/ConnectorDescriptor",
        message = "A map of zero or more action property suggestions keyed by the property name"))
    public Response enrichWithMetadata(
        @PathParam("id") @ApiParam(required = true, example = "io.syndesis:salesforce-create-or-update:latest") final String id,
        final Map<String, String> properties) {

        final ConnectorAction action = actions.stream()//
            .filter(a -> a.idEquals(id))//
            .findAny()//
            .orElseThrow(() -> new EntityNotFoundException("Action with id: " + id));

        final ConnectorDescriptor originalDescriptor = action.getDescriptor();

        ConnectorDescriptor.Builder generatedDescriptorBuilder = new ConnectorDescriptor.Builder().createFrom(originalDescriptor);
        action.getProperties().forEach((k, v) -> {
            if (v.getGenerator() != null && v.getDefaultValue() == null) {
                generatedDescriptorBuilder.replaceConfigurationProperty(k, c -> c.defaultValue(RandomValueGenerator.generate(v.getGenerator())));
            }
        });
        ConnectorDescriptor generatedDescriptor = generatedDescriptorBuilder.build();

        if (!action.getTags().contains("dynamic")) {
            return Response.ok().entity(Meta.verbatim(generatedDescriptor)).build();
        }

        final Map<String, String> parameters = encryptionComponent
            .decrypt(new HashMap<>(Optional.ofNullable(properties).orElseGet(HashMap::new)));
        // put all action parameters with `null` values
        generatedDescriptor.getPropertyDefinitionSteps()
            .forEach(step -> step.getProperties().forEach((k, v) -> parameters.putIfAbsent(k, null)));

        // add the pattern as a property
        if (action.getPattern() != null) {
            parameters.put(action.getPattern().getDeclaringClass().getSimpleName(), action.getPattern().name());
        }
        // lastly put all connection properties
        parameters.putAll(encryptionComponent.decrypt(connection.getConfiguredProperties()));

        final HystrixExecutable<DynamicActionMetadata> meta = createMetadataCommand(action, parameters);
        final DynamicActionMetadata dynamicActionMetadata = meta.execute();

        final ConnectorDescriptor enrichedDescriptor = applyMetadataTo(generatedDescriptor, dynamicActionMetadata);

        @SuppressWarnings("unchecked")
        final HystrixInvokableInfo<ConnectorDescriptor> metaInfo = (HystrixInvokableInfo<ConnectorDescriptor>) meta;

        final Meta<ConnectorDescriptor> metaResult = Meta.from(enrichedDescriptor, metaInfo);

        final Status status = metaResult.getData().getType().map(t -> t.status).orElse(Status.OK);

        return Response.status(status).entity(metaResult).build();
    }

    protected HystrixExecutable<DynamicActionMetadata> createMetadataCommand(final ConnectorAction action,
        final Map<String, String> parameters) {
        return new MetadataCommand(config, connectorId, action, parameters);
    }

    private static DataShape adaptDataShape(final Optional<DataShape> maybeDataShape) {
        if (maybeDataShape.isPresent()) {
            final DataShape dataShape = maybeDataShape.get();
            if (dataShape.getKind() != DataShapeKinds.ANY && dataShape.getKind() != DataShapeKinds.NONE
                && dataShape.getSpecification() == null && dataShape.getType() == null) {
                return ANY_SHAPE;
            }
        }

        return maybeDataShape.orElse(NO_SHAPE);
    }

    private static ConnectorDescriptor adaptDataShapes(final ConnectorDescriptor.Builder builder) {
        final ConnectorDescriptor descriptor = builder.build();

        final Optional<DataShape> maybeInputDataShape = descriptor.getInputDataShape();
        final DataShape inputDataShape = adaptDataShape(maybeInputDataShape);
        builder.inputDataShape(inputDataShape);

        final Optional<DataShape> maybeOutputDataShape = descriptor.getOutputDataShape();
        final DataShape outputDataShape = adaptDataShape(maybeOutputDataShape);
        builder.outputDataShape(outputDataShape);

        return builder.build();
    }

    private static ConnectorDescriptor applyMetadataTo(final ConnectorDescriptor descriptor, final DynamicActionMetadata dynamicMetadata) {
        final Map<String, List<DynamicActionMetadata.ActionPropertySuggestion>> actionPropertySuggestions = dynamicMetadata.properties();

        final ConnectorDescriptor.Builder enriched = new ConnectorDescriptor.Builder().createFrom(descriptor);
        actionPropertySuggestions.forEach((k, vals) -> enriched.replaceConfigurationProperty(k,
            b -> b.addAllEnum(vals.stream().map(s -> ConfigurationProperty.PropertyValue.Builder.from(s))::iterator)));

        // Setting the defaultValue as suggested by the metadata
        for (final Entry<String, List<DynamicActionMetadata.ActionPropertySuggestion>> suggestions : actionPropertySuggestions.entrySet()) {
            if (suggestions.getValue().size() == 1) {
                for (final DynamicActionMetadata.ActionPropertySuggestion suggestion : suggestions.getValue()) {
                    enriched.replaceConfigurationProperty(suggestion.displayValue(), v -> v.defaultValue(suggestion.value()));
                }
            }
        }

        final DataShape input = dynamicMetadata.inputShape();
        if (shouldEnrichDataShape(descriptor.getInputDataShape(), input)) {
            enriched.inputDataShape(input);
        }

        final DataShape output = dynamicMetadata.outputShape();
        if (shouldEnrichDataShape(descriptor.getOutputDataShape(), output)) {
            enriched.outputDataShape(output);
        }

        return adaptDataShapes(enriched);
    }

    private static boolean isMaleable(final DataShapeKinds kind) {
        return kind != DataShapeKinds.JAVA;
    }

    private static boolean shouldEnrichDataShape(final Optional<DataShape> maybeExistingDataShape, final DataShape received) {
        return maybeExistingDataShape.isPresent() && isMaleable(maybeExistingDataShape.get().getKind()) && received != null
            && received.getKind() != null;
    }
}
