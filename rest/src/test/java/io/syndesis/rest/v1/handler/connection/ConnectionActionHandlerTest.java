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

import io.syndesis.model.connection.Action;
import io.syndesis.model.connection.ActionDefinition;
import io.syndesis.model.connection.ConfigurationProperty;
import io.syndesis.model.connection.Connector;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConnectionActionHandlerTest {

    private static final String SALESFORCE_CREATE_OR_UPDATE = "io.syndesis:salesforce-create-or-update:latest";

    private final ActionDefinition createOrUpdateSalesforceObjectDefinition;

    private final ConnectionActionHandler handler;

    public ConnectionActionHandlerTest() {
        createOrUpdateSalesforceObjectDefinition = new ActionDefinition.Builder()
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

        final Connector connector = new Connector.Builder().addAction(new Action.Builder()
            .id(SALESFORCE_CREATE_OR_UPDATE).definition(createOrUpdateSalesforceObjectDefinition).build()).build();

        handler = new ConnectionActionHandler(connector);
    }

    @Test
    public void shouldProvideActionDefinition() {
        final ActionDefinition definition = handler.definition(SALESFORCE_CREATE_OR_UPDATE);

        assertThat(definition).isEqualTo(createOrUpdateSalesforceObjectDefinition);
    }

}
