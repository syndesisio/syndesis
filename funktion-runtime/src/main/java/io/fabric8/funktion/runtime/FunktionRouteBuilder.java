/*
 * Copyright 2016 Red Hat, Inc.
 * <p>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package io.fabric8.funktion.runtime;

import java.util.List;

import io.fabric8.funktion.model.FunktionConfig;
import io.fabric8.funktion.model.FunktionConfigs;
import io.fabric8.funktion.model.FunktionRule;
import io.fabric8.funktion.support.Strings;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpEndpoint;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spring.boot.FatJarRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * A Camel {@link RouteBuilder} which maps the Funktion rules to Camel routes
 */
@Component
public class FunktionRouteBuilder extends RouteBuilder {
    private static final transient Logger LOG = LoggerFactory.getLogger(FunktionRouteBuilder.class);

    // use servlet to map from http trigger to use Spring Boot servlet engine
    private static final String DEFAULT_TRIGGER_URL = "http://0.0.0.0:8080/";
    private static final String DEFAULT_HTTP_ENDPOINT_PREFIX = "servlet:funktion";

    // must have a main method spring-boot can run
    public static void main(String[] args) {
        FatJarRouter.main(args);
    }

    @Bean
    ServletRegistrationBean camelServlet() {
        // use a @Bean to register the Camel servlet which we need to do
        // because we want to use the camel-servlet component for the Camel REST service
        ServletRegistrationBean mapping = new ServletRegistrationBean();
        mapping.setName("CamelServlet");
        mapping.setLoadOnStartup(1);
        mapping.setServlet(new CamelHttpTransportServlet());
        mapping.addUrlMappings("/camel/*");
        return mapping;
    }

    @Override
    public void configure() throws Exception {
        FunktionConfig config = FunktionConfigs.load();

        int idx = 0;
        List<FunktionRule> rules = config.getRules();
        for (FunktionRule rule : rules) {
            configureRule(rule, idx++);
        }
    }

    protected void configureRule(FunktionRule rule, int funktionIndex) {
        String trigger = rule.getTrigger();
        if (Strings.isEmpty(trigger)) {
            trigger = DEFAULT_TRIGGER_URL;
        }

        StringBuilder message =  new StringBuilder("FUNKTION ");
        String name = rule.getName();
        if (Strings.isEmpty(name)) {
            name = "funktion" + (funktionIndex + 1);
            rule.setName(name);
        }
        message.append(name);
        message.append("() ");
        message.append(trigger);

        if (trigger.equals("http")) {
            trigger = DEFAULT_HTTP_ENDPOINT_PREFIX;
        } else if (trigger.startsWith("http://") || trigger.startsWith("https://")) {
            // lets add the HTTP endpoint prefix

            // is there any context-path
            String path = trigger.startsWith("https://") ? trigger.substring(8) : null;
            if (path == null) {
                path = trigger.startsWith("http://") ? trigger.substring(7) : null;
            }

            if (path != null) {
                // keep only context path
                if (path.contains("/")) {
                    path = path.substring(path.indexOf('/'));
                }
            }
            if (path != null) {
                trigger = path;
            }

            trigger = DEFAULT_HTTP_ENDPOINT_PREFIX + trigger;
        }

        RouteDefinition route = from(trigger);
        route.id(name);
        String action = rule.getAction();
        if (!Strings.isEmpty(action)) {
            String method = null;

            int idx = action.indexOf("::");
            if (idx > 0) {
                method = action.substring(idx + 2);
                action = action.substring(0, idx);
            }

            message.append(" => ");
            message.append(action);

            if (method != null) {
                message.append("." + method + "()");
                action += "?method=" + method;
            }
            else {
                message.append(".main()");
            }
            action = "class:" + action;

            route.to(action);
        }
        String chain = rule.getChain();
        if (!Strings.isEmpty(chain)) {
            // lets configure the http component
            if (chain.startsWith("http:") || chain.startsWith("https:")) {
                HttpEndpoint endpoint = endpoint(chain, HttpEndpoint.class);
                if (endpoint != null) {
                    // lets bridge them as a proxy
                    endpoint.setBridgeEndpoint(true);
                    endpoint.setThrowExceptionOnFailure(false);
                }
            }
            route.to(chain);
            message.append(" => ");
            message.append(chain);
        }
        LOG.info(message.toString());

        if (Strings.isEmpty(chain) && Strings.isEmpty(action)) {
            throw new IllegalStateException("Both action and chain are empty! Invalid rule " + trigger);
        }
    }
}
