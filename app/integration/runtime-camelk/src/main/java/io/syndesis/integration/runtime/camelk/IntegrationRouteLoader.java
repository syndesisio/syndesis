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
package io.syndesis.integration.runtime.camelk;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import io.opentracing.Tracer;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;
import io.syndesis.integration.runtime.logging.ActivityTracker;
import io.syndesis.integration.runtime.logging.IntegrationActivityTrackingPolicyFactory;
import io.syndesis.integration.runtime.logging.FlowActivityTrackingPolicyFactory;
import io.syndesis.integration.runtime.tracing.TracingActivityTrackingPolicyFactory;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.k.RoutesLoader;
import org.apache.camel.k.Runtime;
import org.apache.camel.k.Source;
import org.apache.camel.k.support.URIResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrationRouteLoader implements RoutesLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationRouteLoader.class);

    private ActivityTracker activityTracker;
    private Set<IntegrationStepHandler> integrationStepHandlers;
    private Tracer tracer;

    public IntegrationRouteLoader() {
    }

    public IntegrationRouteLoader(ActivityTracker activityTracker, Set<IntegrationStepHandler> integrationStepHandlers, Tracer tracer) {
        this.activityTracker = activityTracker;
        this.integrationStepHandlers = integrationStepHandlers;
        this.tracer = tracer;
    }

    @Override
    public List<String> getSupportedLanguages() {
        return Collections.singletonList("syndesis");
    }

    @Override
    public RouteBuilder load(Runtime.Registry runtimeRegistry, Source source) throws Exception {
        if (activityTracker == null) {
            LOGGER.info("Loading ActivityTracker from Camel RuntimeRegistry.");
            activityTracker = runtimeRegistry.lookup("activityTracker", ActivityTracker.class);
        }
        if (activityTracker == null) {
            LOGGER.info("ActivityTracker not provided or not found in Camel RuntimeRegistry, using new instance of ActivityTracker.SysOut() .");
            activityTracker = new ActivityTracker.SysOut();
        }

        if (integrationStepHandlers == null) {
            LOGGER.info("Loading IntegrationStepHandlers with ServiceLoader.");
            integrationStepHandlers = new HashSet<>();
            ServiceLoader.load(IntegrationStepHandler.class).forEach(integrationStepHandlers::add);
            LOGGER.info("{} IntegrationStepHandlers loaded.", integrationStepHandlers.size());
        }

        if (tracer == null) {
            LOGGER.info("Loading tracer from Camel RuntimeRegistry.");
            tracer = runtimeRegistry.lookup("tracer", Tracer.class);
            if (tracer != null) {
                LOGGER.info("Tracer {} loaded.", tracer);
            } else {
                LOGGER.info("No tracer found.");
            }
        }

        return new IntegrationRouteBuilder(
            ctx -> URIResolver.resolve(ctx, source),
            integrationStepHandlers,
            Arrays.asList(new IntegrationActivityTrackingPolicyFactory(activityTracker),
                new FlowActivityTrackingPolicyFactory(activityTracker),
                new TracingActivityTrackingPolicyFactory(tracer))
        );
    }
}
