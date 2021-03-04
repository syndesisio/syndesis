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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.connection.ConnectorSettings;
import io.syndesis.common.util.IOStreams;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.api.generator.ConnectorGenerator;
import io.syndesis.server.api.generator.openapi.TestHelper;
import io.syndesis.server.api.generator.soap.parser.SoapApiModelParserTest;

import org.junit.jupiter.params.provider.Arguments;

import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.SPECIFICATION_PROPERTY;
import static java.util.Objects.requireNonNull;

/**
 * Base class for example WSDL driven tests.
 */
public abstract class AbstractSoapExampleTest {

    protected final ConnectorGenerator connectorGenerator = generator();

    public static String resource(final String path) throws IOException {
        final String resource;
        try (final InputStream in = requireNonNull(TestHelper.class.getResourceAsStream(path), path);
             final BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

            resource = reader.lines().collect(Collectors.joining("\n"));
        }
        return resource;
    }

    protected ConnectorGenerator generator() {
        try (InputStream stream = SoapApiConnectorGeneratorExampleTest.class
            .getResourceAsStream("/META-INF/syndesis/connector/soap.json")) {

            final Connector soapConnector = JsonUtils.readFromStream(stream, Connector.class);
            return new SoapApiConnectorGenerator(soapConnector);

        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    protected ConnectorSettings getConnectorSettings(final String specification) {
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
            load("/soap/Workday_Extensibility.wsdl")
        );
    }

    static Arguments load(String path) throws IOException {
        try (InputStream is = SoapApiModelParserTest.class.getResourceAsStream(path)) {
            return Arguments.of(path, IOStreams.readText(is));
        }
    }
}
