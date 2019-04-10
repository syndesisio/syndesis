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

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Endpoint;
import org.apache.camel.component.rest.swagger.RestSwaggerEndpoint;
import org.apache.camel.util.IntrospectionSupport;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootApplication
@SpringBootTest(properties = {"spring.main.banner-mode = off"})
public class SwaggerConnectorComponentTest {

    @Autowired
    private CamelContext camelContext;

    @Test
    public void shouldDetermineScheme() {
        final Endpoint endpoint = camelContext.getEndpoint("swagger-operation?petId=3");

        assertThat(endpoint).isNotNull();

        final Optional<RestSwaggerEndpoint> maybeRestSwagger = camelContext.getEndpoints().stream()
            .filter(RestSwaggerEndpoint.class::isInstance).map(RestSwaggerEndpoint.class::cast).findFirst();

        assertThat(maybeRestSwagger).hasValueSatisfying(restSwagger -> {
            assertThat(restSwagger.getSpecificationUri().toString()).matches("file:.*swagger-operation.*\\.swagger");
        });
    }

    @Test
    public void shouldPassSpecificationToRestSwaggerComponent() throws Exception {
        final Component component = camelContext.getComponent("swagger-operation");
        assertThat(component).isNotNull();

        final String specification = IOUtils.toString(SwaggerConnectorComponentTest.class.getResource("/petstore.json"),
            StandardCharsets.UTF_8);
        IntrospectionSupport.setProperties(component, new HashMap<>(Collections.singletonMap("specification", specification)));

        final Endpoint endpoint = component.createEndpoint("swagger-operation://?operationId=addPet");
        assertThat(endpoint).isNotNull();

        final Optional<RestSwaggerEndpoint> maybeRestSwagger = camelContext.getEndpoints().stream()
            .filter(RestSwaggerEndpoint.class::isInstance).map(RestSwaggerEndpoint.class::cast).findFirst();

        assertThat(maybeRestSwagger).hasValueSatisfying(restSwagger -> {
            assertThat(restSwagger.getSpecificationUri()).isNotNull();
            assertThat(restSwagger.getOperationId()).isEqualTo("addPet");
        });
    }

    @Test
    public void shouldSetBasicAuthorizationHeader() {
        final SwaggerConnectorComponent component = new SwaggerConnectorComponent();

        component.setAuthenticationType(AuthenticationType.basic);
        component.setUsername("username");
        component.setPassword("dolphins");

        final HashMap<String, Object> headers = new HashMap<>();
        component.addAuthenticationHeadersTo(headers);

        assertThat(headers).containsEntry("Authorization", "Basic dXNlcm5hbWU6ZG9scGhpbnM=");
    }

    @Test
    public void shouldSetOAuth2AuthorizationHeader() {
        final SwaggerConnectorComponent component = new SwaggerConnectorComponent();

        component.setAuthenticationType(AuthenticationType.oauth2);
        component.setAccessToken("the-token");

        final HashMap<String, Object> headers = new HashMap<>();
        component.addAuthenticationHeadersTo(headers);

        assertThat(headers).containsEntry("Authorization", "Bearer the-token");
    }

    @Test
    public void shouldSetStatusCodesForRefreshTokenRetry() {
        final SwaggerConnectorComponent component = new SwaggerConnectorComponent();

        assertThat(component.getRefreshTokenRetryStatuses()).isEmpty();

        component.setRefreshTokenRetryStatuses("");
        assertThat(component.getRefreshTokenRetryStatuses()).isEmpty();

        component.setRefreshTokenRetryStatuses(null);
        assertThat(component.getRefreshTokenRetryStatuses()).isEmpty();

        component.setRefreshTokenRetryStatuses("1");
        assertThat(component.getRefreshTokenRetryStatuses()).isEqualTo("1");

        component.setRefreshTokenRetryStatuses("1,2");
        assertThat(component.getRefreshTokenRetryStatuses()).isEqualTo("1,2");

        component.setRefreshTokenRetryStatuses(" 1 ,2 , 3");
        assertThat(component.getRefreshTokenRetryStatuses()).isEqualTo("1,2,3");
    }
}
