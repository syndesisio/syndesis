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

import com.netflix.hystrix.HystrixExecutable;
import com.netflix.hystrix.HystrixInvokableInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.ConnectionBase;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.connection.DynamicActionMetadata;
import io.syndesis.common.model.connection.WithDynamicProperties;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.common.util.RandomValueGenerator;
import io.syndesis.server.api.generator.ConnectorAndActionGenerator;
import io.syndesis.server.dao.file.SpecificationResourceDao;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.endpoint.v1.dto.Meta;
import io.syndesis.server.verifier.MetadataConfigurationProperties;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

@Tag(name = "actions")
public class ConnectionActionHandler extends BaseConnectorGeneratorHandler {
    public static final DataShape ANY_SHAPE = new DataShape.Builder().kind(DataShapeKinds.ANY).build();

    public static final DataShape NO_SHAPE = new DataShape.Builder().kind(DataShapeKinds.NONE).build();

    private final List<ConnectorAction> actions;

    private final MetadataConfigurationProperties config;

    private final ConnectionBase connection;

    private final String connectorId;

    private final EncryptionComponent encryptionComponent;

    private final Connector connector;

    private final SpecificationResourceDao specificationResourceDao;

    public ConnectionActionHandler(final ConnectionBase connection, final MetadataConfigurationProperties config,
                                   final EncryptionComponent encryptionComponent, final ApplicationContext context,
                                   final DataManager dataManager, final SpecificationResourceDao specificationResourceDao) {
        super(dataManager, context);
        this.connection = connection;
        this.config = config;
        this.encryptionComponent = encryptionComponent;
        this.specificationResourceDao = specificationResourceDao;

        final Optional<Connector> maybeConnector = connection.getConnector();
        connector = maybeConnector.orElseThrow(
            () -> new EntityNotFoundException("Connection with id `" + connection.getId() + "` does not have a Connector defined"));

        connectorId = connector.getId().get();

        actions = connector.getActions();
    }

    @POST
    @Path(value = "/{actionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieves enriched action definition, that is an action definition that has input/output data shapes and property enums defined with respect to the given action properties")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ConnectorDescriptor.class)),
        description = "A map of zero or more action property suggestions keyed by the property name")
    public Response enrichWithMetadata(
        @PathParam("actionId") @Parameter(required = true, example = "io.syndesis:salesforce-create-or-update:latest") final String actionId,
        final Map<String, Object> props) {

        final Map<String, String> properties = new HashMap<>();

        if (props != null) {
            for (Map.Entry<String, Object> entry : props.entrySet()) {
                if (entry.getValue() == null) {
                    properties.put(entry.getKey(), null);
                } else if (entry.getValue() instanceof String[]) {
                    String value = StringUtils.join((String[]) entry.getValue(), ConfigurationProperty.MULTIPLE_SEPARATOR);
                    properties.put(entry.getKey(), value);
                } else if (entry.getValue() instanceof Iterable) {
                    String value = StringUtils.join((Iterable<?>) entry.getValue(), ConfigurationProperty.MULTIPLE_SEPARATOR);
                    properties.put(entry.getKey(), value);
                } else {
                    properties.put(entry.getKey(), entry.getValue().toString());
                }
            }
        }

        final ConnectorAction action = actions.stream()//
            .filter(a -> a.idEquals(actionId))//
            .findAny()//
            .orElseThrow(() -> new EntityNotFoundException("Action with id: " + actionId));

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

        final Map<String, String> parameters = encryptionComponent.decrypt(new HashMap<>(properties));
        // put all action parameters with `null` values
        generatedDescriptor.getPropertyDefinitionSteps()
            .forEach(step -> step.getProperties().forEach((k, v) -> parameters.putIfAbsent(k, null)));

        // add the pattern as a property
        action.getPattern().ifPresent(pattern -> parameters.put("Pattern", pattern.name()));

        // lastly put all connection properties
        parameters.putAll(encryptionComponent.decrypt(connection.getConfiguredProperties()));

        final HystrixExecutable<DynamicActionMetadata> meta = createMetadataCommandAction(action, parameters);
        final DynamicActionMetadata dynamicActionMetadata = meta.execute();

        final ConnectorDescriptor enrichedDescriptor = applyMetadataTo(generatedDescriptor, dynamicActionMetadata);

        @SuppressWarnings("unchecked")
        final HystrixInvokableInfo<ConnectorDescriptor> metaInfo = (HystrixInvokableInfo<ConnectorDescriptor>) meta;

        final Meta<ConnectorDescriptor> metaResult = Meta.from(enrichedDescriptor, metaInfo);

        final Status status = metaResult.getData().getType().map(t -> t.status).orElse(Status.OK);

        return Response.status(status).entity(metaResult).build();
    }

    protected HystrixExecutable<DynamicActionMetadata> createMetadataCommandAction(final ConnectorAction action,
                                                                                   final Map<String, String> parameters) {
        // if the connector's ID resembles a generated key by the key generator that means it's generated
        // in that case there will be no metadata endpoint for it, so we must use the local metadata logic
        if (KeyGenerator.resemblesAKey(connectorId)) {
            // we can't lookup a connection action generator by ID, instead we use the group ID instead,
            // which needs to be defined
            final String connectorGroupId = connector.getConnectorGroupId().get();

            return this.<ConnectorAndActionGenerator, LocalMetadataCommandAction>withGenerator(connectorGroupId, generator -> {
                // in case the connector's specification has been stored separately in the database, we load
                // it now so we can provide it to the generator
                final Map<String, String> configuredProperties = connector.getConfiguredProperties();
                final String specification = configuredProperties.get("specification");
                final InputStream specificationStream;
                if (specification != null && specification.startsWith("db:")) {
                    // strip the "db:" prefix and read the stream
                    specificationStream = specificationResourceDao.read(specification.substring(3));
                } else {
                    specificationStream = null;
                }

                return new LocalMetadataCommandAction(generator, connector, action, parameters, specificationStream);
            });
        }

        return new MetadataCommandAction(config, connectorId, action, parameters);
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
        final Map<String, List<WithDynamicProperties.ActionPropertySuggestion>> actionPropertySuggestions = dynamicMetadata.properties();

        final ConnectorDescriptor.Builder enriched = new ConnectorDescriptor.Builder().createFrom(descriptor);

        // Setting enum or dataList as needed by UI widget
        for (final Map.Entry<String, List<WithDynamicProperties.ActionPropertySuggestion>> suggestions : actionPropertySuggestions.entrySet()) {
            if (!suggestions.getValue().isEmpty()) {
                ConfigurationProperty property= enriched.findProperty(suggestions.getKey());
                if (property != null) {
                    if ("dataList".equalsIgnoreCase(property.getType())) {
                        enriched.replaceConfigurationProperty(suggestions.getKey(),
                                builder -> {
                                    if (suggestions.getValue().size() == 1) {
                                        builder.defaultValue(suggestions.getValue().get(0).value());
                                    }

                                    builder.addAllDataList(suggestions.getValue()
                                            .stream()
                                            .map(ConfigurationProperty.PropertyValue.Builder::value).collect(Collectors.toList()));
                                });
                    } else {
                        enriched.replaceConfigurationProperty(suggestions.getKey(),
                                builder -> {
                                    if (suggestions.getValue().size() == 1) {
                                        builder.defaultValue(suggestions.getValue().get(0).value());
                                    }

                                    builder.addAllEnum(suggestions.getValue()
                                            .stream()
                                            .map(ConfigurationProperty.PropertyValue.Builder::from).collect(Collectors.toList()));
                                });
                    }
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

    private static boolean isMalleable(final DataShapeKinds kind) {
        return kind != DataShapeKinds.JAVA;
    }

    private static boolean shouldEnrichDataShape(Optional<DataShape> existing, final DataShape received) {
        final boolean existingIsPresent = existing.isPresent();
        final boolean canSwapOutExisting = !existingIsPresent || (existingIsPresent && isMalleable(existing.get().getKind()));
        final boolean receivedIsValid = received != null;

        return canSwapOutExisting && receivedIsValid;
    }
}
