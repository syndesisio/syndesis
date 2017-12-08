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
package io.syndesis.connector.generator.swagger;

import java.net.URI;

import io.swagger.models.Scheme;
import io.swagger.models.Swagger;

import org.junit.Test;

import static io.syndesis.connector.generator.swagger.PropertyGenerators.createHostUri;
import static io.syndesis.connector.generator.swagger.PropertyGenerators.determineHost;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class PropertyGeneratorsTest {

    @Test
    public void shouldCreateHostUri() {
        assertThat(createHostUri("scheme", "host")).isEqualTo("scheme://host");
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
        assertThat(determineHost(new Swagger().vendorExtension(SwaggerConnectorGenerator.URL_EXTENSION, specificationUrl)))
            .isEqualTo("https://api.example.com");
        assertThat(
            determineHost(new Swagger().vendorExtension(SwaggerConnectorGenerator.URL_EXTENSION, specificationUrl).scheme(Scheme.HTTP)))
                .isEqualTo("http://api.example.com");
        assertThat(determineHost(new Swagger().vendorExtension(SwaggerConnectorGenerator.URL_EXTENSION, specificationUrl)
            .host("api2.example.com").scheme(Scheme.HTTP))).isEqualTo("http://api2.example.com");
    }

    @Test
    public void shouldFailIfNoHttpSchemesFound() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> determineHost(new Swagger().scheme(Scheme.WS).scheme(Scheme.WSS)))
            .withMessageStartingWith("Unable to find a supported scheme");
    }

    @Test
    public void shouldFailToDetermineIfNoHostGivenAnywhere() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> determineHost(new Swagger()))
            .withMessageStartingWith("Swagger specification does not provide");
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> determineHost(new Swagger().scheme(Scheme.HTTP)))
            .withMessageStartingWith("Swagger specification does not provide");
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> determineHost(new Swagger().host("host")))
            .withMessageStartingWith("Swagger specification does not provide");
    }
}
