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
package io.syndesis.connector.rest.swagger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;

import org.apache.camel.util.StringHelper;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

public class SpecificationResourceCustomizerTest {

    private static final ComponentProxyComponent NOT_USED = null;

    @Test
    public void shouldNotUpdateSpecificationOnMissingSecurityDefinitionName() {
        final SpecificationResourceCustomizer customizer = new SpecificationResourceCustomizer();
        final String spec = RestSwaggerConnectorIntegrationTest.readSpecification("apikey.json");

        final Map<String, Object> options = new HashMap<>();
        options.put("specification", spec);
        options.put("authenticationType", "apiKey");

        customizer.customize(NOT_USED, options);

        assertThat(options).containsKey("specificationUri");
        assertThat(options).doesNotContainKey("specification");
        assertThat(new File(StringHelper.after(ConnectorOptions.extractOption(options, "specificationUri"), "file:"))).hasContent(spec);
    }

    @Test
    public void shouldNotUpdateSpecificationOnNullSecurityDefinitionName() {
        final SpecificationResourceCustomizer customizer = new SpecificationResourceCustomizer();
        final String spec = RestSwaggerConnectorIntegrationTest.readSpecification("apikey.json");

        final Map<String, Object> options = new HashMap<>();
        options.put("specification", spec);

        customizer.customize(NOT_USED, options);

        assertThat(options).containsKey("specificationUri");
        assertThat(options).doesNotContainKey("specification");
        assertThat(new File(StringHelper.after(ConnectorOptions.extractOption(options, "specificationUri"), "file:"))).hasContent(spec);
    }

    @Test
    public void shouldRemoveUnwantedSecurity() {
        final SpecificationResourceCustomizer customizer = new SpecificationResourceCustomizer();
        final String spec = RestSwaggerConnectorIntegrationTest.readSpecification("apikey.json");
        final String specUpdated = RestSwaggerConnectorIntegrationTest.readSpecification("apikey-security-updated.json");

        final Map<String, Object> options = new HashMap<>();
        options.put("specification", spec);
        options.put("authenticationType", "apiKey: api-key-header");

        customizer.customize(NOT_USED, options);

        assertThat(options).containsKey("specificationUri");
        assertThat(options).doesNotContainKey("specification");
        assertThat(new File(StringHelper.after(ConnectorOptions.extractOption(options, "specificationUri"), "file:"))).hasContent(specUpdated);
    }

    @Test
    public void shouldStoreSpecificationInTemporaryDirectory() {
        final SpecificationResourceCustomizer customizer = new SpecificationResourceCustomizer();

        final Map<String, Object> options = new HashMap<>();
        options.put("specification", "the specification is here");

        customizer.customize(NOT_USED, options);

        assertThat(options).containsKey("specificationUri");
        assertThat(options).doesNotContainKey("specification");
        assertThat(new File(StringHelper.after(ConnectorOptions.extractOption(options, "specificationUri"), "file:"))).hasContent("the specification is here");
    }

    @Test // issue #9411
    void shouldNotFailToUpdateSpecificationWithApiKeyAuthEmptyArray() {
        final SpecificationResourceCustomizer customizer = new SpecificationResourceCustomizer();
        final String specYaml = RestSwaggerConnectorIntegrationTest.readSpecification("twitter-api.yaml");
        final JsonNode jsonNode = JsonUtils.convertValue(new Yaml().load(specYaml), JsonNode.class);
        final String spec = JsonUtils.toString(jsonNode);

        final Map<String, Object> options = new HashMap<>();
        options.put("specification", spec);
        options.put("authenticationType", "apiKey: ApiKeyAuth");

        customizer.customize(NOT_USED, options);
        assertThat(new File(StringHelper.after(ConnectorOptions.extractOption(options, "specificationUri"), "file:"))).hasContent(spec);
    }
}
