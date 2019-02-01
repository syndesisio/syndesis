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
package io.syndesis.server.runtime;

import java.io.IOException;
import java.time.Duration;

import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;
import io.micrometer.prometheus.PrometheusMeterRegistry;

@Component
@Provider
public class EndpointMetricsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String TIMING_SAMPLE = "TIMING_SAMPLE";
    @Context
    private ResourceInfo resourceInfo;
    @Autowired
    private PrometheusMeterRegistry registry;

    /**
     * Called before the resource method.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        Sample sample = Timer.start(registry);
        requestContext.setProperty(TIMING_SAMPLE, sample);
    }

    /**
     * Called after the resource method.
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        Sample sample = (Sample) requestContext.getProperty(TIMING_SAMPLE);
        if (sample != null) {
            Timer timer = Timer
                .builder("http.server.requests")
                .tag("method", getMethod(requestContext))
                .tag("status", getStatus(responseContext))
                .tag("uri", getUri())
                .publishPercentileHistogram()
                .maximumExpectedValue(Duration.ofSeconds(10))
                .register(registry);
            sample.stop(timer);
        }
    }

    private String getMethod(ContainerRequestContext requestContext) {
        return requestContext.getRequest().getMethod();
    }

    private String getStatus(ContainerResponseContext responseContext) {
        return Integer.toString(responseContext.getStatus());
    }

    private String getUri() {
        return getResourceClassPath() + getResourceMethodPath();
    }

    private String getResourceClassPath() {
        Path classPath = resourceInfo.getResourceClass().getAnnotation(Path.class);
        return classPath != null ? classPath.value() : "";
    }

    private String getResourceMethodPath() {
        Path methodPath = resourceInfo.getResourceMethod().getAnnotation(Path.class);
        return methodPath != null ? methodPath.value() : "";
    }

}
