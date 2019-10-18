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

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("cors")
public class SyndesisCorsConfiguration {

    private List<String> allowedOrigins = Arrays.asList(CorsConfiguration.ALL);

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(request -> {
            String pathInfo = request.getPathInfo();
            if (pathInfo != null &&
                (pathInfo.endsWith("/swagger.json") ||
                 pathInfo.endsWith("/swagger.yaml"))) {
                return new CorsConfiguration().applyPermitDefaultValues();
            }

            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(allowedOrigins);
            config.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH"));
            config.applyPermitDefaultValues();
            return config;
        });
    }

}
