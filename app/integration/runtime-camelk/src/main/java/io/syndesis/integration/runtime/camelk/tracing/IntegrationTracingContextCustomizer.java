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
package io.syndesis.integration.runtime.camelk.tracing;

import io.jaegertracing.Configuration;
import io.opentracing.Tracer;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.integration.runtime.logging.BodyLogger;
import io.syndesis.integration.runtime.tracing.TracingInterceptStrategy;
import io.syndesis.integration.runtime.tracing.TracingLogListener;
import org.apache.camel.CamelContext;
import org.apache.camel.k.ContextCustomizer;
import org.apache.camel.k.Runtime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrationTracingContextCustomizer implements ContextCustomizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTracingContextCustomizer.class);

    private String serviceName;

    @Override
    public void apply(CamelContext camelContext, Runtime.Registry runtimeRegistry) {
        // Lets generates always incrementing lexically sortable unique
        // uuids. These uuids are also more compact than the camel default
        // and contain an embedded timestamp.
        camelContext.setUuidGenerator(KeyGenerator::createKey);

        Tracer tracer = Configuration.fromEnv(serviceName).getTracer();
        runtimeRegistry.bind("tracer", tracer);

        runtimeRegistry.bind("bodyLogger", new BodyLogger.Default());

        TracingInterceptStrategy tis = new TracingInterceptStrategy(tracer);
        runtimeRegistry.bind("integrationLoggingInterceptStrategy", tis);
        camelContext.addInterceptStrategy(tis);

        // Log listener
        camelContext.addLogListener(new TracingLogListener(tracer));

        LOGGER.info("Added opentracing to CamelContext.");
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
