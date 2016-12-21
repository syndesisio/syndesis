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

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.fabric8.funktion.model.steps.Step;
import io.fabric8.funktion.model.Funktion;
import io.fabric8.funktion.model.Funktions;
import io.fabric8.funktion.model.Flow;
import io.fabric8.funktion.model.steps.Endpoint;
import io.fabric8.funktion.model.steps.Function;
import io.fabric8.funktion.model.steps.SetBody;
import io.fabric8.funktion.model.steps.SetHeaders;
import io.fabric8.funktion.runtime.designer.SingleMessageRoutePolicyFactory;
import io.fabric8.funktion.support.Strings;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpEndpoint;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spring.boot.FatJarRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
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
        Funktion config = Funktions.load();

        int idx = 0;
        List<Flow> rules = config.getFlows();
        for (Flow rule : rules) {
            configureRule(rule, idx++);
        }
    }

    protected void configureRule(Flow flow, int funktionIndex) throws MalformedURLException {
        if (flow.isTraceEnabled()) {
            getContext().setTracing(true);
        }


        StringBuilder message =  new StringBuilder("FLOW ");
        String name = flow.getName();
        if (Strings.isEmpty(name)) {
            name = "flow" + (funktionIndex + 1);
            flow.setName(name);
        }
        RouteDefinition route = null;
        List<Step> steps = flow.getSteps();
        int validSteps = 0;
        if (steps != null) {
            for (Step item : steps) {
                if (item instanceof Function) {
                    Function function = (Function) item;
                    String functionName = function.getName();
                    if (!Strings.isEmpty(functionName)) {
                        if (route != null) {
                            route.to("json:marshal");
                        }
                        String method = null;
                        int idx = functionName.indexOf("::");
                        if (idx > 0) {
                            method = functionName.substring(idx + 2);
                            functionName = functionName.substring(0, idx);
                        }
                        String uri = "class:" + functionName;
                        if (method != null) {
                            uri += "?method=" + method;
                        }
                        route = fromOrTo(route, name, uri, message);

                        message.append(functionName);
                        if (method != null) {
                            message.append("." + method + "()");
                        }
                        else {
                            message.append(".main()");
                        }
                        validSteps++;
                    }
                } else if (item instanceof Endpoint) {
                    Endpoint invokeEndpoint = (Endpoint) item;
                    String uri = invokeEndpoint.getUri();
                    if (!Strings.isEmpty(uri)) {
                        if (route != null) {
                            route.to("json:marshal");
                        }
                        // lets configure the http component
                        if (uri.startsWith("http:") || uri.startsWith("https:")) {
                            HttpEndpoint endpoint = endpoint(uri, HttpEndpoint.class);
                            if (endpoint != null) {
                                // lets bridge them as a proxy
                                endpoint.setBridgeEndpoint(true);
                                endpoint.setThrowExceptionOnFailure(false);
                            }
                        }
                        route = fromOrTo(route, name, uri, message);
                        message.append(uri);
                        validSteps++;
                    }
                } else if (item instanceof SetBody) {
                    SetBody step = (SetBody) item;
                    assertRouteNotNull(route, item);
                    route.setBody(constant(step.getBody()));
                } else if (item instanceof SetHeaders) {
                    SetHeaders step = (SetHeaders) item;
                    assertRouteNotNull(route, item);
                    Map<String, Object> headers = step.getHeaders();
                    if (headers != null) {
                        Set<Map.Entry<String, Object>> entries = headers.entrySet();
                        for (Map.Entry<String, Object> entry : entries) {
                            String key = entry.getKey();
                            Object value = entry.getValue();
                            route.setHeader(key, constant(value));
                        }
                    }
                } else {
                    throw new IllegalStateException("Uknown step kind: " + item + " of class: " + item.getClass().getName());
                }
            }
        }
        if (route == null || validSteps == 0) {
            throw new IllegalStateException("No valid steps! Invalid flow " + flow);

        }
        if (flow.isLogResultEnabled()) {
            String chain = "log:" +name + "?showStreams=true";
            route.to(chain);
            message.append(" => ");
            message.append(chain);
            validSteps++;
        }
        LOG.info(message.toString());

        if (flow.isSingleMessageModeEnabled()) {
            LOG.info("Enabling single message mode so that only one message is consumed for Design Mode");
            getContext().addRoutePolicyFactory(new SingleMessageRoutePolicyFactory());
        }
    }

    protected void assertRouteNotNull(RouteDefinition route, Step item) {
        if (route == null) {
            throw new IllegalArgumentException("You cannot use a " + item.getKind() + " step before you have started a flow with an endpoint or function!");
        }
    }

    protected RouteDefinition fromOrTo(RouteDefinition route, String name, String endpoint, StringBuilder message) {
        if (route == null) {
            String trigger = endpoint;
            if (Strings.isEmpty(trigger)) {
                trigger = DEFAULT_TRIGGER_URL;
            }
            message.append(name);
            message.append("() ");

            if (trigger.equals("http")) {
                trigger = DEFAULT_HTTP_ENDPOINT_PREFIX;
            } else if (trigger.startsWith("http:") || trigger.startsWith("https:") ||
                       trigger.startsWith("http://") || trigger.startsWith("https://")) {
                // lets add the HTTP endpoint prefix

                // is there any context-path
                String path = trigger.startsWith("https:") ? trigger.substring(6) : null;
                if (path == null) {
                    path = trigger.startsWith("http:") ? trigger.substring(5) : null;
                }
                if (path == null) {
                    path = trigger.startsWith("https://") ? trigger.substring(8) : null;
                }
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

                trigger = DEFAULT_HTTP_ENDPOINT_PREFIX + "/" + trigger;
            }
            route = from(trigger);
            route.id(name);
        } else {
            message.append(" => ");
            route.to(endpoint);
        }
        return route;
    }


}
