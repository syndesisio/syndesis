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
package io.syndesis.integration.runtime;


import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.camel.CamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.util.ObjectHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(name = "org.apache.camel.spring.boot.CamelAutoConfiguration")
@ConditionalOnBean(CamelContext.class)
@ConditionalOnClass(name = "org.apache.camel.spring.boot.CamelAutoConfiguration")
@ConditionalOnProperty(prefix = "syndesis.route.definition.collector", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(SyndesisConfiguration.class)
public class SyndesisExtensionCollectorAutoConfiguration {
    @Autowired
    private CamelContext context;
    @Autowired(required = false)
    private List<RouteDefinition> definitions;

    @PostConstruct
    public void addRouteDefinitions() throws Exception {
        if (ObjectHelper.isNotEmpty(definitions)) {
            context.addRouteDefinitions(definitions);
        }
    }
}
