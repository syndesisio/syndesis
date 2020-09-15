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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import io.syndesis.server.api.generator.soap.AbstractSoapExampleTest;
import io.syndesis.server.api.generator.soap.SoapApiModelInfo;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test WSDL model parsing using {@link SoapApiModelParser}.
 */
@RunWith(Parameterized.class)
public class SoapApiModelParserTest extends AbstractSoapExampleTest {

    public SoapApiModelParserTest(String resource) throws IOException {
        super(resource);
    }

    @Test
    public void parseSoapAPI() {
        final SoapApiModelInfo soapApiModelInfo = SoapApiModelParser.parseSoapAPI(this.specification, this.specification);

        assertThat(soapApiModelInfo).isNotNull();

        assertThat(soapApiModelInfo.getErrors()).isEmpty();
        assertThat(soapApiModelInfo.getWarnings()).isEmpty();

        assertThat(soapApiModelInfo.getResolvedSpecification()).isNotNull();
        assertThat(soapApiModelInfo.getModel()).isNotNull();
        assertThat(soapApiModelInfo.getServices()).isNotNull();
        assertThat(soapApiModelInfo.getPorts()).isNotNull();
        assertThat(soapApiModelInfo.getDefaultService()).isNotNull();
        assertThat(soapApiModelInfo.getDefaultPort()).isNotNull();
        assertThat(soapApiModelInfo.getDefaultAddress()).isNotNull();
    }

}
