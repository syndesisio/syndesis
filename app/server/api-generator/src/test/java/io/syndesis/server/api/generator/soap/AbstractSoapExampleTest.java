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
import java.io.InputStream;
import java.util.stream.Stream;

import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.connection.ConnectorSettings;
import io.syndesis.common.util.IOStreams;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.api.generator.ConnectorGenerator;
import io.syndesis.server.api.generator.soap.parser.SoapApiModelParserTest;

import org.junit.jupiter.params.provider.Arguments;

import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.SPECIFICATION_PROPERTY;

/**
 * Base class for example WSDL driven tests.
 */
public abstract class AbstractSoapExampleTest {

    protected final ConnectorGenerator connectorGenerator = generator();

    protected static ConnectorGenerator generator() {
        try (InputStream stream = SoapApiConnectorGeneratorExampleTest.class
            .getResourceAsStream("/META-INF/syndesis/connector/soap.json")) {

            final Connector soapConnector = JsonUtils.readFromStream(stream, Connector.class);
            return new SoapApiConnectorGenerator(soapConnector);

        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    protected static ConnectorSettings getConnectorSettings(final String specification) {
        return new ConnectorSettings.Builder()
            .putConfiguredProperty(SPECIFICATION_PROPERTY, specification)
            .build();
    }

    static Stream<Arguments> parameters() throws IOException {
        return Stream.of(
            load("/soap/HelloWorld.wsdl"),
            load("/soap/StockQuote.wsdl"),
            load("/soap/SoapFault11.wsdl"),
            load("/soap/SoapFault12.wsdl"),
            // ALL WorkDay WSDLs
            load("/soap/Absence_Management.wsdl"),
            load("/soap/Academic_Foundation.wsdl"),
            load("/soap/External_Integrations.wsdl"),
            load("/soap/Human_Resources.wsdl"),
            load("/soap/Integrations.wsdl"),
            load("/soap/Resource_Management.wsdl"),
            load("/soap/Workday_Connect.wsdl"),
            load("/soap/Workday_Extensibility.wsdl"),
            load("/soap/suitecrm_rpc_literal.wsdl")
        );
    }

    static Arguments load(String path) throws IOException {
        try (InputStream is = SoapApiModelParserTest.class.getResourceAsStream(path)) {
            return Arguments.of(path, IOStreams.readText(is));
        }
    }
}
