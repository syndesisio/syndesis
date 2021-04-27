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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public final class SpecificationResourceCustomizer implements ComponentProxyCustomizer {

    private static final Logger LOG = LoggerFactory.getLogger(SpecificationResourceCustomizer.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void customize(final ComponentProxyComponent component, final Map<String, Object> options) {
        consumeOption(options, "specification", specificationObject -> {
            try {
                final String authenticationType = ConnectorOptions.extractOption(options, "authenticationType");
                final String specification = updateSecuritySpecification((String) specificationObject, authenticationType);

                final File tempSpecification = File.createTempFile("rest-swagger", ".json");
                tempSpecification.deleteOnExit();
                final String swaggerSpecificationPath = tempSpecification.getAbsolutePath();

                try (OutputStream out = new FileOutputStream(swaggerSpecificationPath)) {
                    IOUtils.write(specification, out, StandardCharsets.UTF_8);
                }

                options.put("specificationUri", "file:" + swaggerSpecificationPath);
            } catch (final IOException e) {
                throw new IllegalStateException("Unable to persist the specification to filesystem", e);
            }
        });
    }

    /**
     * Get the name from the security definition or an empty value
     * @param securityDefinitionSelectedByUser with format type:name (ie,
     *            apiKey: api-key-security)
     * @return the name of the definition or an empty value
     */
    private static Optional<String> getNameFromDefinition(final String securityDefinitionSelectedByUser) {
        if (securityDefinitionSelectedByUser != null && securityDefinitionSelectedByUser.indexOf(':') > 0) {
            return Optional.of(securityDefinitionSelectedByUser.substring(securityDefinitionSelectedByUser.indexOf(':') + 1).trim());
        }

        return Optional.empty();
    }

    /**
     * Scan the specification and leave only the security method picked by user,
     * if any present. The change is required to avoid exposure of security
     * configuration (such as apikey) through other channels not requested by
     * user, for example, query parameters when the user only wants to provide
     * api key through http headers.
     * @param specification the swagger/openapi original specification
     * @param securityDefinitionSelectedByUser the securityDefinition picked by
     *            the user
     * @return the updated swagger/openapi specification or the original
     *         specification
     */
    private static String updateSecuritySpecification(final String specification, final String securityDefinitionSelectedByUser) throws IOException {
        final Optional<String> securityDefinitionName = getNameFromDefinition(securityDefinitionSelectedByUser);
        if (!securityDefinitionName.isPresent()) {
            // no changes
            LOG.debug("Specification was provided with no security method");
            return specification;
        }

        final JsonNode rootNode = OBJECT_MAPPER.readTree(specification);
        final List<JsonNode> securities = rootNode.findValues("security");
        if (securities.isEmpty()) {
            // no changes
            LOG.debug("Specification was provided with no security method");
            return specification;
        }

        LOG.info("Updating specification to accept only {} user selected security method", securityDefinitionName.get());
        for (final JsonNode endpointSecurity : securities) {
            if (endpointSecurity.isArray()) {
                final ArrayNode securityArray = (ArrayNode) endpointSecurity;
                for (int i = 0; i < securityArray.size(); i++) {
                    final JsonNode securityDefinition = securityArray.get(i);
                    final Iterator<String> fields = securityDefinition.fieldNames();
                    if (!fields.hasNext()) {
                        continue;
                    }

                    final String securityName = fields.next();
                    if (!securityDefinitionName.get().equals(securityName)) {
                        securityArray.remove(i);
                    }
                }
            } else {
                // Security section must be an array
                throw new IllegalArgumentException("Swagger/OpenAPI specification requires endpoint security to be an array of elements!");
            }
        }

        return OBJECT_MAPPER.writeValueAsString(rootNode);
    }
}
