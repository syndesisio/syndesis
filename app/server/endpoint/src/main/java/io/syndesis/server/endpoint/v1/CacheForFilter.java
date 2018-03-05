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
package io.syndesis.server.endpoint.v1;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

import org.springframework.stereotype.Service;

/**
 * This filter will set the Cache-Control header depending on if and what the
 * @CacheFor annotation is configured with.  When not set, it disables response caching.
 */
@Provider
@Service
public class CacheForFilter implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        CacheFor cc = resourceInfo.getResourceClass().getAnnotation(CacheFor.class);
        CacheFor mcc = resourceInfo.getResourceMethod().getAnnotation(CacheFor.class);
        if( mcc!=null ) {
            cc = mcc;
        }
        if (cc!=null) {
            if( cc.value() == 0 ) {
                context.register(NoCacheFilter.class);
            } else if( cc.value() > 0 ) {
                context.register(new CacheFilter("max-age= " + cc.unit().toSeconds(cc.value())));
            }
        } else {
            context.register(NoCacheFilter.class);
        }
    }

    public static class CacheFilter  implements ContainerResponseFilter {
        private final String cacheControl;

        public CacheFilter(String cacheControl) {
            this.cacheControl = cacheControl;
        }

        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
            if( responseContext.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL)==null ) {
                responseContext.getHeaders().add(HttpHeaders.CACHE_CONTROL, cacheControl);
            }
        }
    }

    public static class NoCacheFilter  implements ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
            if( responseContext.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL)==null ) {
                responseContext.getHeaders().add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, max-age=0, must-revalidate, proxy-revalidate, s-maxage=0");
            }
        }
    }

}
