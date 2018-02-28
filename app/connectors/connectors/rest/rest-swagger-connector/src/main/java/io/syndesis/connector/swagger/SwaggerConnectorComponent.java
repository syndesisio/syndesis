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
package io.syndesis.connector.swagger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.camel.Endpoint;
import org.apache.camel.Processor;
import org.apache.camel.component.connector.DefaultConnectorComponent;
import org.apache.camel.component.connector.DefaultConnectorEndpoint;
import org.apache.camel.component.rest.swagger.RestSwaggerEndpoint;
import org.apache.camel.processor.Pipeline;
import org.apache.camel.util.IntrospectionSupport;
import org.apache.camel.util.IntrospectionSupport.ClassInfo;
import org.apache.commons.io.IOUtils;

public final class SwaggerConnectorComponent extends DefaultConnectorComponent {

    private String accessToken;

    private AuthenticationType authenticationType;

    private String password;

    private String specification;

    private String username;

    public SwaggerConnectorComponent() {
        this(null);
    }

    public SwaggerConnectorComponent(final String componentSchema) {
        super("swagger-connector", componentSchema, SwaggerConnectorComponent.class);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public String getPassword() {
        return password;
    }

    public String getSpecification() {
        return specification;
    }

    public String getUsername() {
        return username;
    }

    public void setAccessToken(final String accessToken) {
        this.accessToken = accessToken;
    }

    public void setAuthenticationType(final AuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setSpecification(final String specification) {
        this.specification = specification;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    void addAuthenticationHeadersTo(final Map<String, Object> headers) {
        if (authenticationType == AuthenticationType.oauth2) {
            headers.put("Authorization", "Bearer " + accessToken);
        } else if (authenticationType == AuthenticationType.basic) {
            final String usernameAndPassword = username + ":" + password;
            headers.put("Authorization", "Basic " + Base64.getEncoder().encode(usernameAndPassword.getBytes(StandardCharsets.UTF_8)));
        }
    }

    Map<String, Object> determineHeaders(final Map<String, Object> parameters) {
        final Map<String, Object> headers = new HashMap<>();
        final ClassInfo classInfo = IntrospectionSupport.cacheClass(RestSwaggerEndpoint.class);

        final Set<String> knownParameters = Arrays.stream(classInfo.methods).map(i -> i.getterOrSetterShorthandName)
            .filter(Objects::nonNull).collect(Collectors.toSet());

        for (final Iterator<Entry<String, Object>> i = parameters.entrySet().iterator(); i.hasNext();) {
            final Entry<String, Object> entry = i.next();
            final String name = entry.getKey();

            if (!knownParameters.contains(name)) {
                headers.put(name, entry.getValue());

                i.remove();
            }
        }

        addAuthenticationHeadersTo(headers);

        return headers;
    }

    @Override
    protected Endpoint createEndpoint(final String uri, final String remaining, final Map<String, Object> parameters) throws Exception {
        final URI baseEndpointUri = URI.create(uri);

        final String scheme = Optional.ofNullable(baseEndpointUri.getScheme()).orElse(baseEndpointUri.getPath());

        final String swaggerSpecificationPath = File.createTempFile(scheme, ".swagger").getAbsolutePath();

        try (OutputStream out = new FileOutputStream(swaggerSpecificationPath)) {
            IOUtils.write(specification, out, StandardCharsets.UTF_8);
        }

        final String operationId = Optional.ofNullable((String) parameters.get("operationId")).orElse(remaining);

        final Map<String, Object> headers = determineHeaders(parameters);

        final DefaultConnectorEndpoint endpoint = (DefaultConnectorEndpoint) super.createEndpoint(uri,
            "file:" + swaggerSpecificationPath + "#" + operationId, parameters);

        final Processor headerSetter = exchange -> exchange.getIn().getHeaders().putAll(headers);

        final Processor combinedBeforeProducers = Pipeline.newInstance(getCamelContext(), new PayloadConverter(), headerSetter);
        endpoint.setBeforeProducer(combinedBeforeProducers);

        return endpoint;
    }
}
