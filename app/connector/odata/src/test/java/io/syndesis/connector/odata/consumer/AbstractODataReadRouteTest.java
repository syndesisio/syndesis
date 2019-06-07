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
package io.syndesis.connector.odata.consumer;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.connector.odata.AbstractODataRouteTest;
import io.syndesis.connector.odata.component.ODataComponentFactory;
import io.syndesis.connector.odata.customizer.ODataReadCustomizer;
import io.syndesis.connector.support.util.PropertyBuilder;

public abstract class AbstractODataReadRouteTest extends AbstractODataRouteTest {

    protected static final String TEST_SERVER_DATA_1 = "test-server-data-1.json";
    protected static final String TEST_SERVER_DATA_2 = "test-server-data-2.json";
    protected static final String TEST_SERVER_DATA_3 = "test-server-data-3.json";
    protected static final String TEST_SERVER_DATA_1_WITH_COUNT = "test-server-data-1-with-count.json";
    protected static final String TEST_SERVER_DATA_2_WITH_COUNT = "test-server-data-2-with-count.json";
    protected static final String TEST_SERVER_DATA_3_WITH_COUNT = "test-server-data-3-with-count.json";
    protected static final String REF_SERVER_PEOPLE_DATA_1 = "ref-server-people-data-1.json";
    protected static final String REF_SERVER_PEOPLE_DATA_1_EXPANDED_TRIPS = "ref-server-people-data-1-expanded-trips.json";
    protected static final String REF_SERVER_PEOPLE_DATA_2 = "ref-server-people-data-2.json";
    protected static final String REF_SERVER_PEOPLE_DATA_3 = "ref-server-people-data-3.json";
    protected static final String REF_SERVER_PEOPLE_DATA_KLAX = "ref-server-data-klax.json";
    protected static final String REF_SERVER_PEOPLE_DATA_KLAX_LOC = "ref-server-data-klax-location.json";
    protected static final String REF_SERVER_AIRPORT_DATA_1 = "ref-server-airport-data-1.json";
    protected static final String TEST_SERVER_DATA_EMPTY = "test-server-data-empty.json";

    private final boolean splitResult;

    public AbstractODataReadRouteTest(boolean splitResult) throws Exception {
        super();
        this.splitResult = splitResult;
    }

    protected boolean isSplitResult() {
        return splitResult;
    }

    @Override
    protected Connector createODataConnector(PropertyBuilder<String> configurePropBuilder) {
        configurePropBuilder.property(SPLIT_RESULT, Boolean.toString(isSplitResult()));
        return super.createODataConnector(configurePropBuilder);
    }

    @Override
    protected Connector createODataConnector(PropertyBuilder<String> configurePropBuilder, PropertyBuilder<ConfigurationProperty> propBuilder) {
        configurePropBuilder.property(SPLIT_RESULT, Boolean.toString(isSplitResult()));
        return super.createODataConnector(configurePropBuilder, propBuilder);
    }

    @Override
    protected ConnectorAction createConnectorAction() throws Exception {
        ConnectorAction odataAction = new ConnectorAction.Builder()
            .description("Read a resource from the server")
             .id("io.syndesis:odata-read-connector")
             .name("Read")
             .descriptor(new ConnectorDescriptor.Builder()
                        .componentScheme("olingo4")
                        .putConfiguredProperty(METHOD_NAME, Methods.READ.id())
                        .addConnectorCustomizer(ODataReadCustomizer.class.getName())
                        .connectorFactory(ODataComponentFactory.class.getName())
                        .outputDataShape(new DataShape.Builder()
                                         .kind(DataShapeKinds.JSON_INSTANCE)
                                         .build())
                        .build())
            .build();
        return odataAction;
    }
}
