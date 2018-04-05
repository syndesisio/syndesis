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
package io.syndesis.connector.meta;


import javax.servlet.Filter;

import io.syndesis.connector.support.verifier.api.MetadataRetrieval;
import io.syndesis.connector.support.verifier.api.Verifier;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.ExplicitCamelContextNameStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * @author roland
 * @since 20/03/2017
 */
@SuppressWarnings("PMD.UseUtilityClass")
@SpringBootApplication
public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(final String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        for (String name: context.getBeanNamesForType(Verifier.class)) {
            BeanDefinition definition = context.getBeanFactory().getBeanDefinition(name);

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Found Verifier: name={}, factory={}",
                    name,
                    definition.getBeanClassName() != null
                        ? definition.getBeanClassName()
                        : definition.getFactoryBeanName() + "::" + definition.getFactoryMethodName()
                );
            }
        }
        for (String name: context.getBeanNamesForType(MetadataRetrieval.class)) {
            BeanDefinition definition = context.getBeanFactory().getBeanDefinition(name);

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Found MetaDataAdapter: name={}, factory={}",
                    name,
                    definition.getBeanClassName() != null
                        ? definition.getBeanClassName()
                        : definition.getFactoryBeanName() + "::" + definition.getFactoryMethodName()
                );
            }
        }
    }

    /**
     * A filter used capture the request body
     */
    @Bean
    public static Filter requestLoggingFilter() {
        final CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludePayload(true);
        loggingFilter.setMaxPayloadLength(512);

        return loggingFilter;
    }

    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Bean(name = "verifier-context", initMethod = "start", destroyMethod = "stop")
    public static CamelContext verifierContext() {
        CamelContext context = new DefaultCamelContext();
        context.setNameStrategy(new ExplicitCamelContextNameStrategy("verifier-context"));
        context.disableJMX();

        return context;
    }
}
