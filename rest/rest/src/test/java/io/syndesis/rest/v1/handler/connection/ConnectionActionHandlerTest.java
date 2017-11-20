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
package io.syndesis.rest.v1.handler.connection;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import io.syndesis.dao.manager.EncryptionComponent;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.action.ConnectorDescriptor;
import io.syndesis.model.connection.ConfigurationProperty;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.connection.DynamicActionMetadata;
import io.syndesis.verifier.VerificationConfigurationProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConnectionActionHandlerTest {

    private static final String SALESFORCE_CREATE_OR_UPDATE = "io.syndesis:salesforce-create-or-update:latest";

    private static final String SALESFORCE_LIMITS = "io.syndesis:limits:latest";

    private final Client client = mock(Client.class);

    private final ConnectorDescriptor createOrUpdateSalesforceObjectDescriptor;

    private final ConnectionActionHandler handler;

    private Builder invocationBuilder;

    public ConnectionActionHandlerTest() {
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
                        .type("string")//
                        .javaType("java.lang.String")//
                        .componentProperty(false)//
                        .description("Unique field to hold the identifier value")//
                        .build()))
            .build();

        final Connector connector = new Connector.Builder().id("salesforce")
            .addAction(new ConnectorAction.Builder()
                .id(SALESFORCE_CREATE_OR_UPDATE)
                .addTag("dynamic")
                .descriptor(createOrUpdateSalesforceObjectDescriptor)
                .build())
            .addAction(new ConnectorAction.Builder()
                .id(SALESFORCE_LIMITS)
                .descriptor(new ConnectorDescriptor.Builder().build())
                .build())
            .build();

        final Connection connection = new Connection.Builder().connector(connector)
            .putConfiguredProperty("clientId", "some-clientId").build();

        handler = new ConnectionActionHandler(connection, new VerificationConfigurationProperties(), new EncryptionComponent(null)) {
            @Override
            /* default */ Client createClient() {
                return client;
            }
        };
    }

    @Before
    public void setupMocks() {
        final WebTarget target = mock(WebTarget.class);
        when(client.target(anyString())).thenReturn(target);

        invocationBuilder = mock(Builder.class);
        when(target.request(MediaType.APPLICATION_JSON)).thenReturn(invocationBuilder);
    }

    @Test
    public void shouldElicitActionPropertySuggestions() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        final Class<Entity<Map<String, Object>>> entityType = (Class) Entity.class;
        final ArgumentCaptor<Entity<Map<String, Object>>> entity = ArgumentCaptor.forClass(entityType);

        final DynamicActionMetadata suggestions = new DynamicActionMetadata.Builder()
            .putProperty("sObjectName",
                Collections
                    .singletonList(DynamicActionMetadata.ActionPropertySuggestion.Builder.of("Contact", "Contact")))
            .putProperty("sObjectIdName",
                Arrays.asList(DynamicActionMetadata.ActionPropertySuggestion.Builder.of("ID", "Contact ID"),
                    DynamicActionMetadata.ActionPropertySuggestion.Builder.of("Email", "Email"),
                    DynamicActionMetadata.ActionPropertySuggestion.Builder.of("TwitterScreenName__c",
                        "Twitter Screen Name")))
            .build();
        when(invocationBuilder.post(entity.capture(), eq(DynamicActionMetadata.class))).thenReturn(suggestions);

        final ConnectorDescriptor enrichedDefinitioin = new ConnectorDescriptor.Builder()
            .createFrom(createOrUpdateSalesforceObjectDescriptor)
            .replaceConfigurationProperty("sObjectName",
                c -> c.addEnum(ConfigurationProperty.PropertyValue.Builder.of("Contact", "Contact")))
            .replaceConfigurationProperty("sObjectIdName",
                c -> c.addEnum(ConfigurationProperty.PropertyValue.Builder.of("ID", "Contact ID")))
            .replaceConfigurationProperty("sObjectIdName",
                c -> c.addEnum(ConfigurationProperty.PropertyValue.Builder.of("Email", "Email")))
            .replaceConfigurationProperty("sObjectIdName",
                c -> c.addEnum(
                    ConfigurationProperty.PropertyValue.Builder.of("TwitterScreenName__c", "Twitter Screen Name")))
            .build();

        final Map<String, String> parameters = new HashMap<>();
        parameters.put("sObjectName", "Contact");

        assertThat(handler.enrichWithMetadata(SALESFORCE_CREATE_OR_UPDATE, parameters)).isEqualTo(enrichedDefinitioin);

        assertThat(entity.getValue().getEntity()).contains(entry("clientId", "some-clientId"),
            entry("sObjectIdName", null), entry("sObjectName", "Contact"));
    }

    @Test
    public void shouldNotContactVerifierForNonDynamicActions() {
        final ConnectorDescriptor defaultDefinition = new ConnectorDescriptor.Builder().build();
        assertThat(handler.enrichWithMetadata(SALESFORCE_LIMITS, null)).isEqualTo(defaultDefinition);
    }

    @Test
    public void shouldProvideActionDefinition() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        final Class<Entity<Map<String, Object>>> entityType = (Class) Entity.class;
        final ArgumentCaptor<Entity<Map<String, Object>>> entity = ArgumentCaptor.forClass(entityType);

        final DynamicActionMetadata suggestions = new DynamicActionMetadata.Builder().putProperty("sObjectName",
            Arrays.asList(DynamicActionMetadata.ActionPropertySuggestion.Builder.of("Account", "Account"),
                DynamicActionMetadata.ActionPropertySuggestion.Builder.of("Contact", "Contact")))
            .build();
        when(invocationBuilder.post(entity.capture(), eq(DynamicActionMetadata.class))).thenReturn(suggestions);

        final ConnectorDescriptor definition = handler.enrichWithMetadata(SALESFORCE_CREATE_OR_UPDATE,
            Collections.emptyMap());

        final ConnectorDescriptor enrichedDefinitioin = new ConnectorDescriptor.Builder()
            .createFrom(createOrUpdateSalesforceObjectDescriptor)
            .replaceConfigurationProperty("sObjectName",
                c -> c.addEnum(ConfigurationProperty.PropertyValue.Builder.of("Account", "Account"),
                    ConfigurationProperty.PropertyValue.Builder.of("Contact", "Contact")))
            .build();

        assertThat(definition).isEqualTo(enrichedDefinitioin);
    }
}
