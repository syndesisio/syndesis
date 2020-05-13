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
package io.syndesis.server.api.generator.soap;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ActionsSummary;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.api.APISummary;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.api.generator.soap.parser.XmlSchemaTestHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectReader;

import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.ADDRESS_PROPERTY;
import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.DATA_FORMAT_PROPERTY;
import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.DEFAULT_OPERATION_NAMESPACE_PROPERTY;
import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.DEFAULT_OPERATION_NAME_PROPERTY;
import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.PASSWORD_PROPERTY;
import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.PAYLOAD_FORMAT;
import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.PORTS_PROPERTY;
import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.PORT_NAME_PROPERTY;
import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.SERVICES_PROPERTY;
import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.SERVICE_NAME_PROPERTY;
import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.SOAP_VERSION_PROPERTY;
import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.SPECIFICATION_PROPERTY;
import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.USERNAME_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;

public class SoapApiConnectorGeneratorExampleTest extends AbstractSoapExampleTest {

    private static final Logger LOG = LoggerFactory.getLogger(SoapApiConnectorGeneratorExampleTest.class);

    public SoapApiConnectorGeneratorExampleTest(final String resource) throws IOException {
        super(resource);
    }

    @Test
    public void shouldProvideInfo() throws IOException {

        final APISummary apiSummary = connectorGenerator.info(SoapConnectorTemplate.SOAP_TEMPLATE,
            getConnectorSettings());

        assertThat(apiSummary).isNotNull();
        assertThat(apiSummary.getWarnings()).isEmpty();
        assertThat(apiSummary.getErrors()).isEmpty();

        assertThat(apiSummary.getName()).isNotEmpty();
        assertThat(apiSummary.getDescription()).isNotEmpty();

        final Map<String, String> configuredProperties = apiSummary.getConfiguredProperties();
        assertThat(configuredProperties).isNotEmpty();
        assertThat(configuredProperties).containsKey(SPECIFICATION_PROPERTY);
        assertThat(configuredProperties).containsKey(SERVICE_NAME_PROPERTY);
        assertThat(configuredProperties).containsKey(PORT_NAME_PROPERTY);

        // services and ports should contain valid json list and map respectively
        final ObjectReader reader = JsonUtils.reader();
        final String[] services = reader
            .forType(String[].class)
            .readValue(configuredProperties.get(SERVICES_PROPERTY));
        final Map<String, List<String>> ports = reader
            .forType(new TypeReference<Map<String, List<String>>>(){})
            .readValue(configuredProperties.get(PORTS_PROPERTY));
        assertThat(services).isNotEmpty();
        assertThat(ports).isNotEmpty();

        final ActionsSummary actionsSummary = apiSummary.getActionsSummary();
        assertThat(actionsSummary).isNotNull();
        assertThat(actionsSummary.getTotalActions()).isGreaterThan(0);
        assertThat(actionsSummary.getActionCountByTags()).isNotEmpty();
    }

    @Test
    public void shouldGenerateConnector() throws IOException, SAXException {

        final Connector connector = connectorGenerator.generate(SoapConnectorTemplate.SOAP_TEMPLATE, getConnectorSettings());

        assertThat(connector).isNotNull();
        assertThat(connector.getName()).isNotEmpty();
        assertThat(connector.getDescription()).isNotEmpty();

        // assert summary
        final Optional<ActionsSummary> actionsSummary = connector.getActionsSummary();
        assertThat(actionsSummary).isPresent();
        assertThat(actionsSummary.get().getTotalActions()).isGreaterThan(0);
        assertThat(actionsSummary.get().getActionCountByTags()).isNotEmpty();

        // assert template properties
        final Map<String, ConfigurationProperty> properties = connector.getProperties();
        assertThat(properties).isNotEmpty();
        assertThat(properties).containsKey(ADDRESS_PROPERTY);
        assertThat(properties.get(ADDRESS_PROPERTY).getDefaultValue()).isNotNull();
        assertThat(properties).containsKey(USERNAME_PROPERTY);
        assertThat(properties).containsKey(PASSWORD_PROPERTY);

        // assert configured properties
        final Map<String, String> configuredProperties = connector.getConfiguredProperties();
        assertThat(configuredProperties).isNotEmpty();
        assertThat(configuredProperties).containsKey(SERVICE_NAME_PROPERTY);
        assertThat(configuredProperties).containsKey(PORT_NAME_PROPERTY);
        assertThat(configuredProperties).containsKey(SPECIFICATION_PROPERTY);
        assertThat(configuredProperties).containsKey(ADDRESS_PROPERTY);
        assertThat(configuredProperties).containsKey(SOAP_VERSION_PROPERTY);
        assertThat(configuredProperties).containsKey(SOAP_VERSION_PROPERTY);
        assertThat(configuredProperties).containsKey("componentName");

        // assert actions
        assertThat(connector.getActions()).isNotEmpty();
        for (ConnectorAction a : connector.getActions()) {
            assertThat(a.getActionType()).isEqualTo(ConnectorAction.TYPE_CONNECTOR);
            assertThat(a.getName()).isNotEmpty();
            assertThat(a.getDescription()).isNotEmpty();
            assertThat(a.getPattern()).isEqualTo(Action.Pattern.To);

            final ConnectorDescriptor descriptor = a.getDescriptor();
            assertThat(descriptor).isNotNull();
            assertThat(connector.getId()).isPresent();
            assertThat(descriptor.getConnectorId()).isEqualTo(connector.getId().get());

            final Map<String, String> actionProperties = descriptor.getConfiguredProperties();
            assertThat(actionProperties).isNotEmpty();
            assertThat(actionProperties).containsEntry(DEFAULT_OPERATION_NAME_PROPERTY, a.getName());
            assertThat(actionProperties).containsKey(DEFAULT_OPERATION_NAMESPACE_PROPERTY);
            assertThat(actionProperties).containsEntry(DATA_FORMAT_PROPERTY,
                    PAYLOAD_FORMAT);

            // assert input and output data shapes
            final Optional<DataShape> inputDataShape = descriptor.getInputDataShape();
            assertThat(inputDataShape).isPresent();
            validateDataShape(inputDataShape.get());
            final Optional<DataShape> outputDataShape = descriptor.getOutputDataShape();
            assertThat(outputDataShape).isPresent();
            validateDataShape(outputDataShape.get());
        }
    }

    private static void validateDataShape(DataShape inputDataShape) throws SAXException, IOException {
        // check whether the shape is not none
        if (inputDataShape.getKind() != DataShapeKinds.NONE) {
            assertThat(inputDataShape.getName()).isNotEmpty();
            assertThat(inputDataShape.getDescription()).isNotEmpty();
            assertThat(inputDataShape.getKind()).isEqualTo(DataShapeKinds.XML_SCHEMA);
            final String specification = inputDataShape.getSpecification();
            assertThat(specification).isNotEmpty();
            LOG.info(specification);

            // validate schemaset
            XmlSchemaTestHelper.validateSchemaSet(specification);
        }
    }

}
