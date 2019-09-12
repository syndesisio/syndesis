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
import org.junit.Ignore;

import java.util.List;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
@Ignore("Make sure the AWSDDBConfiguration has the proper credentials before running this test")
public class AWSDDBQueryItemTest extends AWSDDBGenericOperation {

    @Override
    String getConnectorId() {
        return "io.syndesis:aws-ddb-query-connector";
    }

    @Override
    String getCustomizer() {
        return "io.syndesis.connector.aws.ddb.customizer" +
                ".DDBConnectorCustomizerQuery";
    }


    /**
     * Extend the steps to add an intermediate putitem
     *
     * @return
     */
    @Override
    protected List<Step> createSteps() {

        final ConnectorAction action = new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                        .componentScheme("aws-ddb")
                        .connectorId("io.syndesis:aws-ddb-putitem-to-connector")
                        .build())
                .build();

        final Connection connection = new Connection.Builder()
                .putConfiguredProperty(AWSDDBConfiguration.ACCESSKEY,
                        AWSDDBConfiguration.ACCESSKEY_VALUE)
                .putConfiguredProperty(AWSDDBConfiguration.SECRETKEY,
                        AWSDDBConfiguration.SECRETKEY_VALUE)
                .putConfiguredProperty(AWSDDBConfiguration.REGION,
                        AWSDDBConfiguration.REGION_VALUE)
                .putConfiguredProperty(AWSDDBConfiguration.TABLENAME,
                        AWSDDBConfiguration.TABLENAME_VALUE)
                .putConfiguredProperty(AWSDDBConfiguration.ELEMENT,
                        AWSDDBConfiguration.ELEMENT_VALUE)
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
                        .putProperty(
                                AWSDDBConfiguration.ELEMENT,
                                new ConfigurationProperty.Builder()
                                        .kind(AWSDDBConfiguration.ELEMENT)
                                        .secret(false)
                                        .componentProperty(true)
                                        .build())
                        .addConnectorCustomizer("io.syndesis.connector.aws.ddb.customizer" +
                                ".DDBConnectorCustomizerPutItem")
                        .build())
                .build();

        Step step = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(connection)
                .action(action)
                .build();

        List<Step> result = super.createSteps();
        result.add(2, step);

        return result;
    }
}
