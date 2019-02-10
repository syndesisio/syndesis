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
package io.syndesis.integration.runtime.camelk.jmx;

import io.syndesis.integration.runtime.jmx.CamelContextMetadataMBean;
import org.apache.camel.CamelContext;
import org.apache.camel.k.ContextCustomizer;
import org.apache.camel.k.Runtime;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrationMetadataContextCustomizer implements ContextCustomizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationMetadataContextCustomizer.class);

    @Override
    public void apply(CamelContext camelContext, Runtime.Registry runtimeRegistry) {
        try {
            // register custom mbean
            camelContext.addService(new CamelContextMetadataMBean());
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
            ObjectHelper.wrapRuntimeCamelException(e);
        }
        LOGGER.info("Added Syndesis MBean Service");
    }
}
