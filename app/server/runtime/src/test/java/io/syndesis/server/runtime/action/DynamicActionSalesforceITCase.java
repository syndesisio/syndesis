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
package io.syndesis.server.runtime.action;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.UUID;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.runtime.BaseITCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(initializers = BaseITCase.TestConfigurationInitializer.class)
@SuppressWarnings({"PMD.TooManyStaticImports", "PMD.ExcessiveImports"})
public class DynamicActionSalesforceITCase extends BaseITCase {
    private static final String CREATE_OR_UPDATE_ACTION_ID = "io.syndesis:salesforce-create-or-update-connector:latest";

    private static final ConfigurationProperty _DEFAULT_SALESFORCE_IDENTIFIER = new ConfigurationProperty.Builder()//
        .kind("parameter")//
        .displayName("Identifier field name")//
        .group("common")//
        .required(Boolean.TRUE)//
        .type("select")//
        .javaType("java.lang.String")//
        .componentProperty(Boolean.FALSE)//
        .description("Unique field to hold the identifier value")//
        .build();

    private static final ConfigurationProperty _DEFAULT_SALESFORCE_OBJECT_NAME = new ConfigurationProperty.Builder()//
        .kind("parameter")//
        .displayName("Salesforce object type")//
        .group("common")//
        .required(Boolean.TRUE)//
        .type("select")//
        .javaType("java.lang.String")//
        .componentProperty(Boolean.FALSE)//
        .description("Salesforce object type to create")//
        .build();

    private static final ConnectorAction DEFAULT_CREATE_OR_UPDATE_ACTION = new ConnectorAction.Builder()//
        .id(DynamicActionSalesforceITCase.CREATE_OR_UPDATE_ACTION_ID)//
        .addTag("dynamic")//
        .descriptor(new ConnectorDescriptor.Builder()//
            .inputDataShape(new DataShape.Builder().kind(DataShapeKinds.JSON_SCHEMA).build())//
            .outputDataShape(new DataShape.Builder().kind(DataShapeKinds.JAVA)//
                .type("org.apache.camel.component.salesforce.api.dto.CreateSObjectResult").build())//
            .withActionDefinitionStep("Select Salesforce object", "Select Salesforce object type to create",//
                b -> b.putProperty("sObjectName", _DEFAULT_SALESFORCE_OBJECT_NAME))//
            .withActionDefinitionStep("Select Identifier property",//
                "Select Salesforce property that will hold the uniquely identifying value of this object",//
                b -> b.putProperty("sObjectIdName", _DEFAULT_SALESFORCE_IDENTIFIER))//
            .build())//
        .build();

    private final String connectionId = UUID.randomUUID().toString();

    private final ConfigurationProperty contactSalesforceObjectName = new ConfigurationProperty.Builder()
        .createFrom(_DEFAULT_SALESFORCE_OBJECT_NAME)
        .addEnum(ConfigurationProperty.PropertyValue.Builder.of("Contact", "Contact"))
        .defaultValue("Contact")
        .build();

    private final ConfigurationProperty suggestedSalesforceIdNames = new ConfigurationProperty.Builder()
        .createFrom(_DEFAULT_SALESFORCE_IDENTIFIER)
        .addEnum(ConfigurationProperty.PropertyValue.Builder.of("Id", "Contact ID"),
            ConfigurationProperty.PropertyValue.Builder.of("Email", "Email"),
            ConfigurationProperty.PropertyValue.Builder.of("TwitterScreenName__c", "Twitter Screen Name"))
        .build();

    private final ConfigurationProperty suggestedSalesforceObjectNames = new ConfigurationProperty.Builder()
        .createFrom(_DEFAULT_SALESFORCE_OBJECT_NAME)
        .addEnum(ConfigurationProperty.PropertyValue.Builder.of("Account", "Accounts"),
            ConfigurationProperty.PropertyValue.Builder.of("Contact", "Contacts"))
        .build();

    @BeforeClass
    public static void startMockIfNeeded() {
        if (wireMock==null || !wireMock.isRunning()) {
            wireMock = new WireMockRule(wireMockConfig().dynamicPort());
            wireMock.start();
        }
    }

    @Before
    public void setupConnection() {
        dataManager.create(new Connection.Builder().id(connectionId).connectorId("salesforce")
            .putConfiguredProperty("clientId", "a-client-id").build());

        final Connector existingSalesforceConnector = dataManager.fetch(Connector.class, "salesforce");

        final Connector withCreateOrUpdateAction = new Connector.Builder()
            .createFrom(existingSalesforceConnector)
            .addAction(DEFAULT_CREATE_OR_UPDATE_ACTION).build();

        dataManager.update(withCreateOrUpdateAction);
    }

    @Before
    public void setupMocks() {
        WireMock.configureFor(wireMock.port());
        stubFor(WireMock

            .post(urlEqualTo(
                "/api/v1/connectors/salesforce/actions/io.syndesis:salesforce-create-or-update-connector:latest"))//
            .withHeader("Accept", equalTo("application/json"))//
            .withRequestBody(equalToJson("{\"clientId\":\"a-client-id\",\"sObjectName\":null,\"sObjectIdName\":null}"))
            .willReturn(aResponse()//
                .withStatus(200)//
                .withHeader("Content-Type", "application/json")//
                .withBody(read("/verifier-response-salesforce-no-properties.json"))));

        stubFor(WireMock
            .post(urlEqualTo(
                "/api/v1/connectors/salesforce/actions/io.syndesis:salesforce-create-or-update-connector:latest"))//
            .withHeader("Accept", equalTo("application/json"))//
            .withRequestBody(
                equalToJson("{\"clientId\":\"a-client-id\",\"sObjectName\":\"Contact\",\"sObjectIdName\":null}"))
            .willReturn(aResponse()//
                .withStatus(200)//
                .withHeader("Content-Type", "application/json")//
                .withBody(read("/verifier-response-salesforce-type-contact.json"))));
    }

    @Test
    public void shouldOfferDynamicActionPropertySuggestions() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        final ResponseEntity<ConnectorDescriptor> firstResponse = http(HttpMethod.POST,
            "/api/v1/connections/" + connectionId + "/actions/" + CREATE_OR_UPDATE_ACTION_ID, null,
            ConnectorDescriptor.class, tokenRule.validToken(), headers, HttpStatus.OK);

        final ConnectorDescriptor firstEnrichment = new ConnectorDescriptor.Builder()//
            .inputDataShape(new DataShape.Builder().kind(DataShapeKinds.ANY).build())
            .outputDataShape(new DataShape.Builder().kind(DataShapeKinds.JAVA)
                .type("org.apache.camel.component.salesforce.api.dto.CreateSObjectResult").build())
            .withActionDefinitionStep("Select Salesforce object", "Select Salesforce object type to create",
                b -> b.putProperty("sObjectName", suggestedSalesforceObjectNames))
            .withActionDefinitionStep("Select Identifier property",
                "Select Salesforce property that will hold the uniquely identifying value of this object",
                b -> b.putProperty("sObjectIdName", _DEFAULT_SALESFORCE_IDENTIFIER))
            .build();
        assertThat(firstResponse.getBody()).isEqualTo(firstEnrichment);

        final ResponseEntity<ConnectorDescriptor> secondResponse = http(HttpMethod.POST,
            "/api/v1/connections/" + connectionId + "/actions/" + CREATE_OR_UPDATE_ACTION_ID,
            Collections.singletonMap("sObjectName", "Contact"), ConnectorDescriptor.class, tokenRule.validToken(), headers,
            HttpStatus.OK);

        final ConnectorDescriptor secondEnrichment = new ConnectorDescriptor.Builder()//
            .outputDataShape(new DataShape.Builder().kind(DataShapeKinds.JAVA)
                .type("org.apache.camel.component.salesforce.api.dto.CreateSObjectResult").build())
            .withActionDefinitionStep("Select Salesforce object", "Select Salesforce object type to create",
                b -> b.putProperty("sObjectName", contactSalesforceObjectName))
            .withActionDefinitionStep("Select Identifier property",
                "Select Salesforce property that will hold the uniquely identifying value of this object",
                b -> b.putProperty("sObjectIdName", suggestedSalesforceIdNames))
            .build();
        final ConnectorDescriptor secondResponseBody = secondResponse.getBody();
        assertThat(secondResponseBody).isEqualToIgnoringGivenFields(secondEnrichment, "inputDataShape");
        assertThat(secondResponseBody.getInputDataShape()).hasValueSatisfying(input -> {
            assertThat(input.getKind()).isEqualTo(DataShapeKinds.JSON_SCHEMA);
            assertThat(input.getType()).isEqualTo("Contact");
            assertThat(input.getName()).isEqualTo("Contact");
            assertThat(input.getDescription()).isEqualTo("Salesforce Contact");
            assertThat(input.getSpecification()).isNotEmpty();
        });
    }

    private static String read(final String path) {
        try {
            return String.join("",
                Files.readAllLines(Paths.get(DynamicActionSalesforceITCase.class.getResource(path).toURI())));
        } catch (IOException | URISyntaxException e) {
            throw new IllegalArgumentException("Unable to read from path: " + path, e);
        }
    }
}
