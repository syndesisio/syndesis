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
package io.syndesis.connector.kafka;

import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.connector.support.test.ConnectorTestSupport;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.component.kafka.KafkaEndpoint;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"PMD.SignatureDeclareThrowsException", "PMD.JUnitTestsShouldIncludeAssert"})
public class KafkaConnectorTest extends ConnectorTestSupport {

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = super.createCamelContext();
        context.setAutoStartup(false);
        context.disableJMX();

        return context;
    }

    @Override
    protected List<Step> createSteps() {
        List<Step> stepList = new ArrayList<>();
        Map<String, String> options = new HashMap<>();
        options.put("topic", "ciao");
        options = Collections.unmodifiableMap(options);
        stepList.add( new Step.Builder()
            .stepKind(StepKind.endpoint)
            .connection(new Connection.Builder()
                .putConfiguredProperty("brokers", "domain:1234")
                .connector(new Connector.Builder()
                    .putProperty(
                        "brokers",
                        new ConfigurationProperty.Builder()
                            .kind("property")
                            .displayName("Kafka bootstraps URI")
                            .secret(false)
                            .group("common")
                            .label("common")
                            .required(true)
                            .componentProperty(true)
                            .type("string")
                            .javaType("java.lang.String")
                            .deprecated(false)
                            .secret(false)
                            .labelHint("Comma separated list of Kafka brokers URI i the form of host:port")
                            .order(1)
                            .build())
                    .build())
                .build())
            .action(new ConnectorAction.Builder()
                .pattern(Action.Pattern.From)
                .descriptor(new ConnectorDescriptor.Builder()
                    .componentScheme("kafka")
                    .configuredProperties(options)
                    .build())
                .build())
            .action(new ConnectorAction.Builder()
                .pattern(Action.Pattern.To)
                .descriptor(new ConnectorDescriptor.Builder()
                    .componentScheme("kafka")
                    .configuredProperties(options)
                    .build())
                .build())
            .build()
        );

        return stepList;
    }

    @Test
    public void testKafkaConnector() {
        assertNotNull(context());

        Optional<Endpoint> endpoint = context.getEndpoints().stream().filter(e -> e instanceof KafkaEndpoint).findFirst();

        Assertions.assertThat(endpoint.isPresent()).isTrue();
        Assertions.assertThat(endpoint.get().getEndpointUri()).isEqualTo("kafka://ciao?brokers=domain%3A1234");
    }
}
