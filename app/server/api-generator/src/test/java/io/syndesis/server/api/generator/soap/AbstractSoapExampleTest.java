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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.connection.ConnectorSettings;
import io.syndesis.common.util.IOStreams;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.api.generator.ConnectorGenerator;
import io.syndesis.server.api.generator.openapi.TestHelper;
import io.syndesis.server.api.generator.soap.parser.SoapApiModelParserTest;

import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.SPECIFICATION_PROPERTY;
import static java.util.Objects.requireNonNull;

/**
 * Base class for example WSDL driven tests.
 */
@RunWith(Parameterized.class)
public abstract class AbstractSoapExampleTest {

    protected final String specification;
    protected final ConnectorGenerator connectorGenerator;

    protected AbstractSoapExampleTest(String resource) throws IOException {
        if (resource.startsWith("http")) {
            this.specification = resource;
        } else {
            this.specification = IOStreams.readText(SoapApiModelParserTest.class.getResourceAsStream(resource));
        }
        connectorGenerator = generator();
    }

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

    protected ConnectorSettings getConnectorSettings() {
        return new ConnectorSettings.Builder()
            .putConfiguredProperty(SPECIFICATION_PROPERTY, specification)
            .build();
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<String> parameters() {
        return Arrays.asList(
            "/soap/HelloWorld.wsdl",
            "/soap/StockQuote.wsdl",
            // ALL WorkDay WSDLs
            "https://community.workday.com/sites/default/files/file-hosting/productionapi/Absence_Management/v33.1/Absence_Management.wsdl",
            "https://community.workday.com/sites/default/files/file-hosting/productionapi/Academic_Foundation/v33.1/Academic_Foundation.wsdl",
            "https://community.workday.com/sites/default/files/file-hosting/productionapi/External_Integrations/v33.1/External_Integrations.wsdl",
            "https://community.workday.com/sites/default/files/file-hosting/productionapi/Human_Resources/v33.1/Human_Resources.wsdl",
            "https://community.workday.com/sites/default/files/file-hosting/productionapi/Integrations/v33.1/Integrations.wsdl",
            "https://community.workday.com/sites/default/files/file-hosting/productionapi/Resource_Management/v33.1/Resource_Management.wsdl",
            "https://community.workday.com/sites/default/files/file-hosting/productionapi/Workday_Connect/v33.1/Workday_Connect.wsdl",
            "https://community.workday.com/sites/default/files/file-hosting/productionapi/Workday_Extensibility/v33.1/Workday_Extensibility.wsdl"
        );
    }
}
