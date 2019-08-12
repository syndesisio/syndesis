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
package io.syndesis.connector.aws.ddb;


import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.connector.support.test.ConnectorTestSupport;
import org.apache.camel.Endpoint;
import org.apache.camel.component.aws.ddb.DdbComponentVerifierExtension;
import org.apache.camel.component.aws.ddb.DdbEndpoint;
import org.apache.camel.component.extension.ComponentVerifierExtension;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
@Ignore("Make sure the AWSDDBConfiguration has the proper credentials before running this test")
public class AWSDDBRawOptionsTest extends ConnectorTestSupport {
    @Override
    protected List<Step> createSteps() {

        //Create a connection for DDB
        final ConnectorAction putItemAction = new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                        .componentScheme("aws-ddb")
                        .connectorId("io.syndesis:aws-ddb-putitem-to-connector")
                        .build())
                .build();

        final Connection ddbConnection = new Connection.Builder()
                .putConfiguredProperty(AWSDDBConfiguration.ACCESSKEY,
                        AWSDDBConfiguration.ACCESSKEY_VALUE)
                .putConfiguredProperty(AWSDDBConfiguration.SECRETKEY,
                        AWSDDBConfiguration.SECRETKEY_VALUE)
                .putConfiguredProperty(AWSDDBConfiguration.REGION, AWSDDBConfiguration.REGION_VALUE)
                .putConfiguredProperty(AWSDDBConfiguration.TABLENAME,
                        AWSDDBConfiguration.TABLENAME_VALUE)
                .connector(new Connector.Builder()
                        .putProperty(
                                AWSDDBConfiguration.ACCESSKEY,
                                new ConfigurationProperty.Builder()
                                        .kind(AWSDDBConfiguration.ACCESSKEY)
                                        .secret(true)
                                        .raw(true)
                                        .componentProperty(false)
                                        .build())
                        .putProperty(
                                AWSDDBConfiguration.SECRETKEY,
                                new ConfigurationProperty.Builder()
                                        .kind(AWSDDBConfiguration.SECRETKEY)
                                        .secret(true)
                                        .raw(true)
                                        .componentProperty(false)
                                        .build())
                        .putProperty(
                                AWSDDBConfiguration.REGION,
                                new ConfigurationProperty.Builder()
                                        .kind(AWSDDBConfiguration.REGION)
                                        .secret(false)
                                        .componentProperty(false)
                                        .build())
                        .putProperty(
                                AWSDDBConfiguration.TABLENAME,
                                new ConfigurationProperty.Builder()
                                        .kind(AWSDDBConfiguration.TABLENAME)
                                        .secret(false)
                                        .componentProperty(true)
                                        .build())
                        .build())
                .build();

        Step ddbPutItemStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(ddbConnection)
                .action(putItemAction)
                .build();

        return Arrays.asList(
                newSimpleEndpointStep(
                        "direct",
                        builder -> builder.putConfiguredProperty("name", "start")),
                ddbPutItemStep
        );
    }

    @Test
    public void testRawOptions() {
        assertNotNull(context());

        Optional<Endpoint> endpoint =
                context.getEndpoints().stream().filter(e -> e instanceof DdbEndpoint).findFirst();

        assertTrue(endpoint.isPresent());
        assertEquals("aws-ddb://" + AWSDDBConfiguration.TABLENAME_VALUE +
                        "?accessKey=RAW(" + AWSDDBConfiguration.ACCESSKEY_VALUE + ")&region=" +
                        AWSDDBConfiguration.REGION_VALUE + "&secretKey=RAW(" + AWSDDBConfiguration.SECRETKEY_VALUE + ")",
                endpoint.get().getEndpointUri());
    }

    @Test
    public void testRealConfiguration() {
        assertNotNull(context());
        DdbComponentVerifierExtension verifier = new DdbComponentVerifierExtension();
        try {
            verifier.setCamelContext(context());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> parameters = new HashMap<String, Object>();

        parameters.put(AWSDDBConfiguration.ACCESSKEY, AWSDDBConfiguration.ACCESSKEY_VALUE);
        parameters.put(AWSDDBConfiguration.SECRETKEY, AWSDDBConfiguration.SECRETKEY_VALUE);
        parameters.put(AWSDDBConfiguration.REGION, AWSDDBConfiguration.REGION_VALUE);
        parameters.put(AWSDDBConfiguration.TABLENAME, AWSDDBConfiguration.TABLENAME_VALUE);
        ComponentVerifierExtension.Result result =
                verifier.verify(ComponentVerifierExtension.Scope.PARAMETERS
                        , parameters);

        assertTrue(result.getErrors().isEmpty());
        result =
                verifier.verify(ComponentVerifierExtension.Scope.CONNECTIVITY
                        , parameters);

        assertTrue(result.getErrors().isEmpty());
    }
}
