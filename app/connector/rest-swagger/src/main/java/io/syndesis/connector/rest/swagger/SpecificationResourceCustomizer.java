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
import java.util.Map;

import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;

import org.apache.commons.io.IOUtils;

public final class SpecificationResourceCustomizer implements ComponentProxyCustomizer {

    @Override
    public void customize(final ComponentProxyComponent component, final Map<String, Object> options) {
        consumeOption(options, "specification", specificationObject -> {
            final String specification = (String) specificationObject;

            try {
                final File tempSpecification = File.createTempFile("rest-swagger", ".json");
                tempSpecification.deleteOnExit();
                final String swaggerSpecificationPath = tempSpecification.getAbsolutePath();

                try (OutputStream out = new FileOutputStream(swaggerSpecificationPath)) {
                    IOUtils.write(specification, out, StandardCharsets.UTF_8);
                }

                options.put("specificationUri", swaggerSpecificationPath);
            } catch (final IOException e) {
                throw new IllegalStateException("Unable to persist the specification to filesystem", e);
            }
        });
    }

}
