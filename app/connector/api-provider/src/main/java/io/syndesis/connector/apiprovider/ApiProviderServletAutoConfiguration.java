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
package io.syndesis.connector.apiprovider;

import java.util.Arrays;

import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "io.syndesis.connector.apiprovider.servlet.enabled", matchIfMissing = true)
@ConditionalOnWebApplication
public class ApiProviderServletAutoConfiguration {

    String basePath;

    public ApiProviderServletAutoConfiguration(final @Value("${api-basePath}") String basePath) {
        this.basePath = basePath;
    }

    @Bean
    public ServletRegistrationBean apiProviderServletRegistrationBean() {
        ServletRegistrationBean mapping = new ServletRegistrationBean();
        mapping.setServlet(new CamelHttpTransportServlet());
        mapping.addUrlMappings(basePath + "/*");
        mapping.setName("CamelServlet");
        mapping.setLoadOnStartup(1);

        return mapping;
    }

    @Bean
    public FilterRegistrationBean apiSpecificationContentType() {
        final FilterRegistrationBean filter = new FilterRegistrationBean();
        filter.setUrlPatterns(Arrays.asList(basePath + "/.api-doc/swagger.json", basePath + "/.api-doc/swagger.yaml"));
        filter.setFilter(new SpecificationContentTypeFilter());

        return filter;
    }
}
