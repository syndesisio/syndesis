/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.ipaas.runtime;

import org.jboss.resteasy.plugins.interceptors.CorsFilter;
import org.jboss.resteasy.spi.CorsHeaders;
import org.springframework.stereotype.Service;

import javax.ws.rs.container.*;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Arrays;

@Service
@Provider
public class CorsOptionsFeature implements Feature {

    @PreMatching
    public static class CORSOptionsFilter extends CorsFilter {

        CORSOptionsFilter() {
            getAllowedOrigins().addAll(Arrays.asList("*"));
        }

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            String origin = requestContext.getHeaderString(CorsHeaders.ORIGIN);
            if (origin == null)
            {
                return;
            }
            if (requestContext.getMethod().equalsIgnoreCase("OPTIONS"))
            {
                preflight(origin, requestContext);
            }
        }

        // We don't want to do response filtering...
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        }
    }

    @Override
    public boolean configure(FeatureContext context) {
        context.register(new CORSOptionsFilter());
        return true;
    }
}
