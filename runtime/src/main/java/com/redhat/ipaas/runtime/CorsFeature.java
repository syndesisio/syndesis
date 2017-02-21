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
import com.redhat.ipaas.rest.AllowedOrigins;
import org.jboss.resteasy.plugins.interceptors.CorsFilter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.ws.rs.container.*;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import java.util.Arrays;
import java.util.List;

@Service
@EnableConfigurationProperties
@ConfigurationProperties("cors")
@Provider
public class CorsFeature implements DynamicFeature {

    /**
     * The @Prematching on CorsFilter does not work well with dynamic features. So extend here
     * to ommit the @Prematching behaviour.
     */
    public static class NonPrematchingCorsFilter extends CorsFilter {
    }

    private List<String> allowedOrigins = Arrays.asList("*");

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        AllowedOrigins ao = resourceInfo.getResourceClass().getAnnotation(AllowedOrigins.class);
        AllowedOrigins mao = resourceInfo.getResourceMethod().getAnnotation(AllowedOrigins.class);
        if( mao!=null ) {
            ao = mao;
        }

        NonPrematchingCorsFilter corsFilter = new NonPrematchingCorsFilter();
        if (ao!=null) {
            corsFilter.getAllowedOrigins().addAll(Arrays.asList(ao.value()));
            context.register(corsFilter);
        } else {
            corsFilter.getAllowedOrigins().addAll(allowedOrigins);
            context.register(corsFilter);
        }

    }
}
