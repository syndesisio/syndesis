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
package io.syndesis.runtime.action;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import io.syndesis.model.connection.Action;
import io.syndesis.model.connection.ActionDefinition;
import io.syndesis.model.connection.ActionPropertySuggestions;
import io.syndesis.model.connection.ActionPropertySuggestions.ActionPropertySuggestion;
import io.syndesis.model.connection.ConfigurationProperty;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.runtime.BaseITCase;
import io.syndesis.runtime.action.ActionSuggestionsITCase.TestConfigurationInitializer;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(initializers = TestConfigurationInitializer.class)
@SuppressWarnings({"PMD.TooManyStaticImports", "PMD.ExcessiveImports"})
public class ActionSuggestionsITCase extends BaseITCase {

    private static final String CREATE_OR_UPDATE_ACTION_ID = "io.syndesis:salesforce-create-or-update-connector:latest";

    @ClassRule
    public static final WireMockRule WIREMOCK = new WireMockRule(wireMockConfig().dynamicPort());

    private static final Action CREATE_OR_UPDATE_ACTION = new Action.Builder().id(CREATE_OR_UPDATE_ACTION_ID)
        .definition(new ActionDefinition.Builder()
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
            .build())
        .build();

    private final ActionPropertySuggestion account = new ActionPropertySuggestion.Builder().value("Account")
        .displayValue("Accounts").build();

    private final String connectionId = UUID.randomUUID().toString();

    private final ActionPropertySuggestion contact = new ActionPropertySuggestion.Builder().value("Contact")
        .displayValue("Contacts").build();

    private final ActionPropertySuggestion email = new ActionPropertySuggestion.Builder().value("Email")
        .displayValue("E-mail address").build();

    private final ActionPropertySuggestion id = new ActionPropertySuggestion.Builder().value("Id")
        .displayValue("Identifier").build();

    private final ActionPropertySuggestion twitterScreenName = new ActionPropertySuggestion.Builder()
        .value("TwitterScreenName__c").displayValue("Twitter Screen Name").build();

    public static class TestConfigurationInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(final ConfigurableApplicationContext applicationContext) {
            final ConfigurableEnvironment environment = applicationContext.getEnvironment();
            environment.getPropertySources().addFirst(new MapPropertySource("test-source",
                Collections.singletonMap("verifier.service", "localhost:" + WIREMOCK.port())));
        }

    }

    @Before
    public void setupConnection() {
        dataManager.create(new Connection.Builder().id(connectionId).connectorId("salesforce")
            .putConfiguredProperty("clientId", "a-client-id").build());

        final Connector existingSalesforceConnector = dataManager.fetch(Connector.class, "salesforce");

        final Connector withCreateOrUpdateAction = new Connector.Builder().createFrom(existingSalesforceConnector)
            .addAction(CREATE_OR_UPDATE_ACTION).build();

        dataManager.update(withCreateOrUpdateAction);
    }

    @Before
    public void setupMocks() {
        stubFor(WireMock.post(urlEqualTo("/api/v1/action/properties/salesforce"))//
            .withHeader("Accept", equalTo("application/json"))//
            .withRequestBody(equalToJson("{\"clientId\":\"a-client-id\",\"sObjectName\":null,\"sObjectIdName\":null}"))
            .willReturn(aResponse()//
                .withStatus(200)//
                .withHeader("Content-Type", "application/json")//
                .withBody(read("/verifier-response-salesforce-no-properties.json"))));

        stubFor(WireMock.post(urlEqualTo("/api/v1/action/properties/salesforce"))//
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
        final ResponseEntity<ActionPropertySuggestions> firstResponse = get(
            "/api/v1/connections/" + connectionId + "/actions/" + CREATE_OR_UPDATE_ACTION_ID + "/properties",
            ActionPropertySuggestions.class);

        final ActionPropertySuggestions firstSuggestion = new ActionPropertySuggestions.Builder()
            .putValue("sObjectName", Arrays.asList(account, contact)).build();
        assertThat(firstResponse.getBody()).isEqualTo(firstSuggestion);

        final ResponseEntity<ActionPropertySuggestions> secondResponse = get("/api/v1/connections/" + connectionId
            + "/actions/" + CREATE_OR_UPDATE_ACTION_ID + "/properties?sObjectName=Contact",
            ActionPropertySuggestions.class);

        final ActionPropertySuggestions secondSuggestion = new ActionPropertySuggestions.Builder()
            .putValue("sObjectIdName", Arrays.asList(id, email, twitterScreenName)).build();
        assertThat(secondResponse.getBody()).isEqualTo(secondSuggestion);
    }

    private static String read(final String path) {
        try {
            return String.join("",
                Files.readAllLines(Paths.get(ActionSuggestionsITCase.class.getResource(path).toURI())));
        } catch (IOException | URISyntaxException e) {
            throw new IllegalArgumentException("Unable to read from path: " + path, e);
        }
    }
}
