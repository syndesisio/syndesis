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
package io.syndesis.integration.runtime.sb;

import io.syndesis.integration.runtime.util.SyndesisHeaderStrategy;
import org.apache.camel.spi.HeaderFilterStrategy;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(SyndesisHttpConfiguration.NeedsHeaderFilterStrategy.class)
@AutoConfigureBefore(name = "org.apache.camel.component.servlet.springboot.ServletComponentAutoConfiguration")
public class SyndesisHttpConfiguration {

    /**
     * Condition to activate this @Configuration only when it's needed. In all
     * the cases below, we need the SyndesisHeaderStrategy bean defined, in
     * other cases we do not.
     */
    static class NeedsHeaderFilterStrategy extends AnyNestedCondition {
        /**
         * Checks if we're using camel-servlet (via camel-servlet-starter), this
         * is when API provider is used.
         */
        @ConditionalOnClass(name = "org.apache.camel.component.servlet.springboot.ServletComponentAutoConfiguration")
        static class CamelServletUsed {
            // auto configuration test
        }

        /**
         * Checks if we're using API connector-http.
         */
        @ConditionalOnResource(resources = "classpath:META-INF/syndesis/connector/http4.json")
        static class HttpUsed {
            // auto configuration test
        }

        /**
         * Checks if we're using API connector-https.
         */
        @ConditionalOnResource(resources = "classpath:META-INF/syndesis/connector/https4.json")
        static class HttpsUsed {
            // auto configuration test
        }

        /**
         * Checks if we're using API connector-rest-swagger.
         */
        @ConditionalOnResource(resources = "classpath:META-INF/syndesis/connector/rest-swagger.json")
        static class SyndesisApiClientUsed {
            // auto configuration test
        }

        /**
         * Checks if we're using API connector-webhook.
         */
        @ConditionalOnResource(resources = "classpath:META-INF/syndesis/connector/webhook.json")
        static class WebhookUsed {
            // auto configuration test
        }

        public NeedsHeaderFilterStrategy() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public HeaderFilterStrategy syndesisHeaderStrategy() {
        return new SyndesisHeaderStrategy();
    }

}
