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
package io.syndesis.server.api.generator.soap.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import io.syndesis.server.api.generator.soap.AbstractSoapExampleTest;
import io.syndesis.server.api.generator.soap.SoapApiModelInfo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test WSDL model parsing using {@link SoapApiModelParser}.
 */
public class SoapApiModelParserTest extends AbstractSoapExampleTest {

    @ParameterizedTest(name = "{1}")
    @MethodSource("parameters")
    public void parseSoapAPI(final String path, final InputStream specification) throws IOException {
        try (InputStream in = specification) {
            final SoapApiModelInfo soapApiModelInfo = SoapApiModelParser.parseSoapAPI(in, path);

            assertThat(soapApiModelInfo).isNotNull();

            assertThat(soapApiModelInfo.getErrors()).isEmpty();
            assertThat(soapApiModelInfo.getWarnings()).isEmpty();

            try (InputStream condensed = soapApiModelInfo.getSpecification()) {
                assertThat(condensed).isNotNull();
            }
            assertThat(soapApiModelInfo.getModel()).isNotNull();
            assertThat(soapApiModelInfo.getServices()).isNotNull();
            assertThat(soapApiModelInfo.getPorts()).isNotNull();
            assertThat(soapApiModelInfo.getDefaultService()).isNotNull();
            assertThat(soapApiModelInfo.getDefaultPort()).isNotNull();
            assertThat(soapApiModelInfo.getDefaultAddress()).isNotNull();
        }
    }

    @Test
    public void shouldGenerateUniqueActionIds() {
        Map<String, Integer> idMap = new HashMap<>();

        assertThat(SoapApiModelParser.getActionId("connectorId", "name", idMap)).isEqualTo("connectorId:name");
        assertThat(SoapApiModelParser.getActionId("connectorId", "name", idMap)).isEqualTo("connectorId:name_1");
        assertThat(SoapApiModelParser.getActionId("connectorId", "name", idMap)).isEqualTo("connectorId:name_2");
        assertThat(SoapApiModelParser.getActionId("connectorId", "another", idMap)).isEqualTo("connectorId:another");
    }
}
