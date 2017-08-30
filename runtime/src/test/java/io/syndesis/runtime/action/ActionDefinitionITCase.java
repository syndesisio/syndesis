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

import java.util.List;
import java.util.UUID;

import io.syndesis.model.connection.ActionDefinition;
import io.syndesis.model.connection.ActionDefinition.ActionDefinitionStep;
import io.syndesis.model.connection.ConfigurationProperty;
import io.syndesis.model.connection.Connection;
import io.syndesis.runtime.BaseITCase;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class ActionDefinitionITCase extends BaseITCase {

    private static final String UPSERT = "io.syndesis:salesforce-upsert-contact-connector:latest";

    private final String connectionId = UUID.randomUUID().toString();

    private final ActionDefinitionStep singleStep = new ActionDefinition.ActionDefinitionStep.Builder()
        .description("Specify field to hold the identifying value").name("Unique field")
        .putProperty("sObjectIdName",
            new ConfigurationProperty.Builder().kind("parameter").displayName("SObject Id Name").group("common")
                .required(false).deprecated(false).componentProperty(false).secret(false).type("string")
                .javaType("java.lang.String").defaultValue("TwitterScreenName__c")
                .description("SObject external ID field name").build())
        .build();

    @Before
    public void setupConnection() {
        dataManager.create(new Connection.Builder().id(connectionId).connectorId("salesforce").build());
    }

    @Test
    public void shouldExposeActionDefinitions() {
        final ResponseEntity<ActionDefinition> acquisitionResponse = get(
            "/api/v1/connections/" + connectionId + "/actions/" + UPSERT + "/definition", ActionDefinition.class,
            tokenRule.validToken(), HttpStatus.OK);

        final ActionDefinition definition = acquisitionResponse.getBody();
        assertThat(definition).isNotNull();

        final List<ActionDefinitionStep> propertyDefinitionSteps = definition.getPropertyDefinitionSteps();
        assertThat(propertyDefinitionSteps).containsOnly(singleStep);
    }
}
