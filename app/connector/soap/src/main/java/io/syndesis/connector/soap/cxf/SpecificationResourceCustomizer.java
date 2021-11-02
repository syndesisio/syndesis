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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;

public final class SpecificationResourceCustomizer implements ComponentProxyCustomizer {

    @Override
    public void customize(final ComponentProxyComponent component, final Map<String, Object> options) {

        if (options.containsKey(ComponentProperties.WSDL_URL)) {

            // remove specification, since CXF will load it from wsdlURL
            options.remove(ComponentProperties.SPECIFICATION);

        } else if (options.containsKey(ComponentProperties.SPECIFICATION)) {

            consumeOption(options, ComponentProperties.SPECIFICATION, specificationObject -> {
                final String specification = (String) specificationObject;

                if (specification.startsWith("db:")) {
                    return;
                }

                try {
                    final File tempSpecification = File.createTempFile("soap", ".wsdl");
                    tempSpecification.deleteOnExit();
                    final String wsdlURL = tempSpecification.getAbsolutePath();

                    try (OutputStream out = new FileOutputStream(wsdlURL)) {
                        IOUtils.write(specification, out, StandardCharsets.UTF_8);
                    }

                    options.put(ComponentProperties.WSDL_URL, wsdlURL);
                } catch (final IOException e) {
                    throw new IllegalStateException("Unable to persist the WSDL specification to filesystem", e);
                }
            });

        } else {
            throw new IllegalStateException("Missing property, either 'wsdlURL' or 'specification' MUST be provided");
        }
    }

}
