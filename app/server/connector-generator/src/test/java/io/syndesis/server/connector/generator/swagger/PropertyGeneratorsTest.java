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
package io.syndesis.server.connector.generator.swagger;

import java.net.URI;

import io.swagger.models.Scheme;
import io.swagger.models.Swagger;

import org.junit.Test;

import static io.syndesis.server.connector.generator.swagger.PropertyGenerators.createHostUri;
import static io.syndesis.server.connector.generator.swagger.PropertyGenerators.determineHost;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertyGeneratorsTest {

    @Test
    public void shouldCreateHostUri() {
        assertThat(createHostUri("scheme", "host", -1)).isEqualTo("scheme://host");
        assertThat(createHostUri("scheme", "host", 8080)).isEqualTo("scheme://host:8080");
    }

    @Test
    public void shouldDetermineFromHostsContainingPorts() {
        assertThat(determineHost(new Swagger().host("54.152.43.92:8080").scheme(Scheme.HTTPS))).isEqualTo("https://54.152.43.92:8080");
    }

    @Test
    public void shouldDetermineHostFromSpecification() {
        assertThat(determineHost(new Swagger().host("api.example.com").scheme(Scheme.HTTPS))).isEqualTo("https://api.example.com");
        assertThat(determineHost(new Swagger().host("api.example.com").scheme(Scheme.HTTP).scheme(Scheme.HTTPS)))
            .isEqualTo("https://api.example.com");
        assertThat(determineHost(new Swagger().host("api.example.com").scheme(Scheme.HTTP))).isEqualTo("http://api.example.com");
    }

    @Test
    public void shouldDetermineHostFromSpecificationUrl() {
        final URI specificationUrl = URI.create("https://api.example.com/swagger.json");
        assertThat(determineHost(new Swagger().vendorExtension(BaseSwaggerConnectorGenerator.URL_EXTENSION, specificationUrl)))
            .isEqualTo("https://api.example.com");
        assertThat(
            determineHost(new Swagger().vendorExtension(BaseSwaggerConnectorGenerator.URL_EXTENSION, specificationUrl).scheme(Scheme.HTTP)))
                .isEqualTo("http://api.example.com");
        assertThat(determineHost(new Swagger().vendorExtension(BaseSwaggerConnectorGenerator.URL_EXTENSION, specificationUrl)
            .host("api2.example.com").scheme(Scheme.HTTP))).isEqualTo("http://api2.example.com");
    }

    @Test
    public void shouldReturnNullIfNoHostGivenAnywhere() {
        assertThat(determineHost(new Swagger())).isNull();
        assertThat(determineHost(new Swagger().scheme(Scheme.HTTP))).isNull();
        assertThat(determineHost(new Swagger().host("host"))).isNull();
    }

    @Test
    public void shouldReturnNullIfNoHttpSchemesFound() {
        assertThat(determineHost(new Swagger().scheme(Scheme.WS).scheme(Scheme.WSS))).isNull();
    }
}
