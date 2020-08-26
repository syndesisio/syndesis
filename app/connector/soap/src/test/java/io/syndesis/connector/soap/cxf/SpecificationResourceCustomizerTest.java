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
package io.syndesis.connector.soap.cxf;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

public class SpecificationResourceCustomizerTest {

    private static final ComponentProxyComponent NOT_USED = null;

    private static final String WSDL_URL = "wsdlURL";
    private static final String SPECIFICATION = "specification";
    private static final String CONTENTS = "the specification is here";
    private static final String HTTP_DUMMY_URL = "http://dummy.url";

    @Test
    public void shouldStoreSpecificationInTemporaryDirectory() {
        final SpecificationResourceCustomizer customizer = new SpecificationResourceCustomizer();

        final Map<String, Object> options = new HashMap<>();
        options.put(SPECIFICATION, CONTENTS);

        customizer.customize(NOT_USED, options);

        assertThat(options).containsKey(WSDL_URL);
        assertThat(options).doesNotContainKey(SPECIFICATION);
        assertThat(new File(ConnectorOptions.extractOption(options, WSDL_URL))).hasContent(CONTENTS);
    }

    @Test
    public void shouldIgnoreSpecificationWhenWsdlUrlIsPresent() {
        final SpecificationResourceCustomizer customizer = new SpecificationResourceCustomizer();

        final Map<String, Object> options = new HashMap<>();
        options.put(SPECIFICATION, CONTENTS);
        options.put(WSDL_URL, HTTP_DUMMY_URL);

        customizer.customize(NOT_USED, options);

        assertThat(options).containsKey(WSDL_URL);
        assertThat(options.get(WSDL_URL)).isEqualTo(HTTP_DUMMY_URL);
        assertThat(options).doesNotContainKey(SPECIFICATION);
    }

    @Test
    public void shouldThrowExceptionOnMissingProperties() {
        final SpecificationResourceCustomizer customizer = new SpecificationResourceCustomizer();

        final Map<String, Object> options = new HashMap<>();

        assertThrows(IllegalStateException.class, () -> customizer.customize(NOT_USED, options));
    }
}
