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

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.netflix.hystrix.HystrixExecutable;
import com.netflix.hystrix.HystrixInvokableInfo;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.connection.DynamicActionMetadata;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.endpoint.v1.dto.Meta;
import io.syndesis.server.endpoint.v1.dto.MetaData;
import io.syndesis.server.verifier.MetadataConfigurationProperties;
import org.json.JSONException;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class ConnectionActionHandlerTest {

    private static final String SALESFORCE_CREATE_OR_UPDATE = "io.syndesis:salesforce-create-or-update:latest";

    private static final String SALESFORCE_LIMITS = "io.syndesis:limits:latest";

    private final ConnectorDescriptor createOrUpdateSalesforceObjectDescriptor;

    private final ConnectionActionHandler handler;

    @SuppressWarnings("unchecked")
    private final HystrixExecutable<DynamicActionMetadata> metadataCommand = mock(HystrixExecutable.class,
        withSettings().extraInterfaces(HystrixInvokableInfo.class));

    private Map<String, String> metadataCommandParameters;

    private final DataShape salesforceContactShape;

    private final DataShape salesforceOutputShape;

    public ConnectionActionHandlerTest() {
        salesforceOutputShape = new DataShape.Builder()//
            .kind(DataShapeKinds.JAVA).type("org.apache.camel.component.salesforce.api.dto.CreateSObjectResult")//
            .build();

        salesforceContactShape = new DataShape.Builder()//
            .kind(DataShapeKinds.JSON_SCHEMA)//
            .type("Contact")//
            .name("Contact")//
            .description("Salesforce Contact")//
            .specification(
                "{\"type\":\"object\",\"id\":\"urn:jsonschema:org:apache:camel:component:salesforce:dto:Contact\",\"$schema\":\"http://json-schema.org/draft-04/schema#\",\"title\":\"Contact\"}")//
            .build();

        createOrUpdateSalesforceObjectDescriptor = new ConnectorDescriptor.Builder()
            .withActionDefinitionStep("Select Salesforce object", "Select Salesforce object type to create",
                b -> b.putProperty("sObjectName",
                    new ConfigurationProperty.Builder()//
                        .kind("parameter")//
                        .displayName("Salesforce object type")//
                        .group("common")//
                        .required(true)//
                        .type("string")//
                        .javaType("java.lang.String")//
                        .componentProperty(false)//
                        .description("Salesforce object type to create")//
                        .build()))
            .withActionDefinitionStep("Select Identifier property",
                "Select Salesforce property that will hold the uniquely identifying value of this object",
                b -> b.putProperty("sObjectIdName",
                    new ConfigurationProperty.Builder()//
                        .kind("parameter")//
                        .displayName("Identifier field name")//
                        .group("common")//
                        .required(true)//
                        .type("select")//
                        .javaType("java.lang.String")//
                        .componentProperty(false)//
                        .description("Unique field to hold the identifier value")//
                        .build()))
            .inputDataShape(new DataShape.Builder().kind(DataShapeKinds.JSON_SCHEMA).build())//
            .outputDataShape(salesforceOutputShape).build();

        final Connector connector = new Connector.Builder().id("salesforce")
            .addAction(new ConnectorAction.Builder().id(SALESFORCE_CREATE_OR_UPDATE).addTag("dynamic")
                .descriptor(createOrUpdateSalesforceObjectDescriptor).build())
            .addAction(new ConnectorAction.Builder().id(SALESFORCE_LIMITS).descriptor(new ConnectorDescriptor.Builder().build()).build())
            .build();

        final Connection connection = new Connection.Builder().connector(connector).putConfiguredProperty("clientId", "some-clientId")
            .build();

        handler = new ConnectionActionHandler(connection, new MetadataConfigurationProperties(), new EncryptionComponent(null)) {
            @Override
            protected HystrixExecutable<DynamicActionMetadata> createMetadataCommand(final ConnectorAction action,
                final Map<String, String> parameters) {
                metadataCommandParameters = parameters;
                return metadataCommand;
            }
        };
    }

    @Test
    public void shouldAddMetaAndSetStatusToBadRequestIfMetaCallFails() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        final Class<Entity<Map<String, Object>>> entityType = (Class) Entity.class;
        ArgumentCaptor.forClass(entityType);

        // simulates fallback return
        final DynamicActionMetadata fallback = new DynamicActionMetadata.Builder().build();
        when(metadataCommand.execute()).thenReturn(fallback);
        when(((HystrixInvokableInfo<?>) metadataCommand).isSuccessfulExecution()).thenReturn(false);

        final Response response = handler.enrichWithMetadata(SALESFORCE_CREATE_OR_UPDATE, Collections.emptyMap());

        assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

        @SuppressWarnings("unchecked")
        final Meta<ConnectorDescriptor> meta = (Meta<ConnectorDescriptor>) response.getEntity();

        final ConnectorDescriptor descriptor = new ConnectorDescriptor.Builder().createFrom(createOrUpdateSalesforceObjectDescriptor)
            .inputDataShape(ConnectionActionHandler.ANY_SHAPE)//
            .outputDataShape(salesforceOutputShape)//
            .build();
        assertThat(meta.getValue()).isEqualTo(descriptor);
        final MetaData metadata = meta.getData();
        assertThat(metadata).isNotNull();
        assertThat(metadata.getType()).contains(MetaData.Type.WARNING);
        assertThat(metadata.getMessage()).contains("The query did not succeed");
    }

    @Test
    public void shouldElicitActionPropertySuggestions() throws JSONException {
        final DynamicActionMetadata suggestions = new DynamicActionMetadata.Builder()
            .putProperty("sObjectName",
                Collections.singletonList(DynamicActionMetadata.ActionPropertySuggestion.Builder.of("Contact", "Contact")))
            .putProperty("sObjectIdName",
                Arrays.asList(DynamicActionMetadata.ActionPropertySuggestion.Builder.of("ID", "Contact ID"),
                    DynamicActionMetadata.ActionPropertySuggestion.Builder.of("Email", "Email"),
                    DynamicActionMetadata.ActionPropertySuggestion.Builder.of("TwitterScreenName__c", "Twitter Screen Name")))
            .inputShape(salesforceContactShape)//
            .build();
        when(metadataCommand.execute()).thenReturn(suggestions);
        when(((HystrixInvokableInfo<?>) metadataCommand).isSuccessfulExecution()).thenReturn(true);

        final ConnectorDescriptor enrichedDefinitioin = new ConnectorDescriptor.Builder()
            .createFrom(createOrUpdateSalesforceObjectDescriptor)
            .replaceConfigurationProperty("sObjectName",
                c -> c.addEnum(ConfigurationProperty.PropertyValue.Builder.of("Contact", "Contact")).defaultValue("Contact"))
            .replaceConfigurationProperty("sObjectIdName",
                c -> c.addEnum(ConfigurationProperty.PropertyValue.Builder.of("ID", "Contact ID")))
            .replaceConfigurationProperty("sObjectIdName",
                c -> c.addEnum(ConfigurationProperty.PropertyValue.Builder.of("Email", "Email")))
            .replaceConfigurationProperty("sObjectIdName",
                c -> c.addEnum(ConfigurationProperty.PropertyValue.Builder.of("TwitterScreenName__c", "Twitter Screen Name")))
            .inputDataShape(salesforceContactShape)//
            .build();

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("sObjectName", "Contact");

        final Response response = handler.enrichWithMetadata(SALESFORCE_CREATE_OR_UPDATE, parameters);

        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

        @SuppressWarnings("unchecked")
        final Meta<ConnectorDescriptor> meta = (Meta<ConnectorDescriptor>) response.getEntity();

        assertThat(meta.getValue()).isEqualTo(enrichedDefinitioin);
    }

    @Test
    public void shouldNotContactVerifierForNonDynamicActions() {
        final ConnectorDescriptor defaultDefinition = new ConnectorDescriptor.Builder().build();
        final Response response = handler.enrichWithMetadata(SALESFORCE_LIMITS, null);

        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

        @SuppressWarnings("unchecked")
        final Meta<ConnectorDescriptor> returnValue = (Meta<ConnectorDescriptor>) response.getEntity();

        assertThat(returnValue.getValue()).isEqualTo(defaultDefinition);
    }

    @Test
    public void shouldProvideActionDefinition() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        final Class<Entity<Map<String, Object>>> entityType = (Class) Entity.class;
        ArgumentCaptor.forClass(entityType);

        final DynamicActionMetadata suggestions = new DynamicActionMetadata.Builder()
            .putProperty("sObjectName", Arrays.asList(DynamicActionMetadata.ActionPropertySuggestion.Builder.of("Account", "Account"),
                DynamicActionMetadata.ActionPropertySuggestion.Builder.of("Contact", "Contact")))
            .build();
        when(metadataCommand.execute()).thenReturn(suggestions);
        when(((HystrixInvokableInfo<?>) metadataCommand).isSuccessfulExecution()).thenReturn(true);

        final Response response = handler.enrichWithMetadata(SALESFORCE_CREATE_OR_UPDATE, Collections.emptyMap());

        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

        @SuppressWarnings("unchecked")
        final Meta<ConnectorDescriptor> meta = (Meta<ConnectorDescriptor>) response.getEntity();

        final ConnectorDescriptor enrichedDefinitioin = new ConnectorDescriptor.Builder()
            .createFrom(createOrUpdateSalesforceObjectDescriptor)
            .replaceConfigurationProperty("sObjectName",
                c -> c.addAllEnum(Arrays.asList(
                        ConfigurationProperty.PropertyValue.Builder.of("Account", "Account"),
                        ConfigurationProperty.PropertyValue.Builder.of("Contact", "Contact"))))
            .inputDataShape(ConnectionActionHandler.ANY_SHAPE)//
            .build();

        assertThat(meta.getValue()).isEqualTo(enrichedDefinitioin);
    }

    @Test
    public void shouldSetInoutOutputShapesToAnyIfMetadataCallFails() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        final Class<Entity<Map<String, Object>>> entityType = (Class) Entity.class;
        ArgumentCaptor.forClass(entityType);

        // simulates fallback return
        final DynamicActionMetadata fallback = new DynamicActionMetadata.Builder().build();
        when(metadataCommand.execute()).thenReturn(fallback);
        when(((HystrixInvokableInfo<?>) metadataCommand).isSuccessfulExecution()).thenReturn(false);

        final Response response = handler.enrichWithMetadata(SALESFORCE_CREATE_OR_UPDATE, Collections.emptyMap());

        @SuppressWarnings("unchecked")
        final Meta<ConnectorDescriptor> meta = (Meta<ConnectorDescriptor>) response.getEntity();

        final ConnectorDescriptor descriptor = meta.getValue();
        assertThat(descriptor.getInputDataShape()).contains(ConnectionActionHandler.ANY_SHAPE);
        assertThat(descriptor.getOutputDataShape()).contains(salesforceOutputShape);
    }

    @Test
    public void shouldConvertParameterFromIterableWithStringsToCommaDelimitedString() {
        final DynamicActionMetadata suggestions = new DynamicActionMetadata.Builder()
            .putProperty("sObjectName", Arrays.asList(DynamicActionMetadata.ActionPropertySuggestion.Builder.of("Account", "Account"),
                DynamicActionMetadata.ActionPropertySuggestion.Builder.of("Contact", "Contact")))
            .build();
        when(metadataCommand.execute()).thenReturn(suggestions);
        when(((HystrixInvokableInfo<?>) metadataCommand).isSuccessfulExecution()).thenReturn(true);


        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("sObjectName", Arrays.asList("Contact", "Account"));

        final Response response = handler.enrichWithMetadata(SALESFORCE_CREATE_OR_UPDATE, parameters);

        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        assertThat(metadataCommandParameters).containsEntry("sObjectName", "Contact,Account");
    }

    @Test
    public void shouldConvertParameterFromArrayOfStringsToCommaDelimitedString() {
        final DynamicActionMetadata suggestions = new DynamicActionMetadata.Builder()
            .putProperty("sObjectName", Arrays.asList(DynamicActionMetadata.ActionPropertySuggestion.Builder.of("Account", "Account"),
                DynamicActionMetadata.ActionPropertySuggestion.Builder.of("Contact", "Contact")))
            .build();
        when(metadataCommand.execute()).thenReturn(suggestions);
        when(((HystrixInvokableInfo<?>) metadataCommand).isSuccessfulExecution()).thenReturn(true);


        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("sObjectName", new String[] { "Contact", "Account" });

        final Response response = handler.enrichWithMetadata(SALESFORCE_CREATE_OR_UPDATE, parameters);

        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        assertThat(metadataCommandParameters).containsEntry("sObjectName", "Contact,Account");
    }

    @Test
    public void shouldSupportOtherParameterTypes() {
        final DynamicActionMetadata suggestions = new DynamicActionMetadata.Builder()
            .putProperty("sObjectName", Arrays.asList(DynamicActionMetadata.ActionPropertySuggestion.Builder.of("1", "1"),
                DynamicActionMetadata.ActionPropertySuggestion.Builder.of("2", "2")))
            .build();
        when(metadataCommand.execute()).thenReturn(suggestions);
        when(((HystrixInvokableInfo<?>) metadataCommand).isSuccessfulExecution()).thenReturn(true);


        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("sObjectName", 1);

        handler.enrichWithMetadata(SALESFORCE_CREATE_OR_UPDATE, parameters);
        assertThat(metadataCommandParameters).containsEntry("sObjectName", "1");
    }
}
