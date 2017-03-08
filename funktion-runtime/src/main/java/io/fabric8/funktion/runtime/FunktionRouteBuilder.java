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

import io.fabric8.funktion.model.Flow;
import io.fabric8.funktion.model.Funktion;
import io.fabric8.funktion.model.Funktions;
import io.fabric8.funktion.model.steps.*;
import io.fabric8.funktion.runtime.designer.SingleMessageRoutePolicyFactory;
import io.fabric8.funktion.support.Strings;
import org.apache.camel.Expression;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpEndpoint;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.FilterDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.SplitDefinition;
import org.apache.camel.model.ThrottleDefinition;
import org.apache.camel.spi.Language;
import org.apache.camel.spring.boot.CamelSpringBootApplicationController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static io.fabric8.funktion.support.Lists.notNullList;

/**
 * A Camel {@link RouteBuilder} which maps the Funktion rules to Camel routes
 */
@Component
public class FunktionRouteBuilder extends RouteBuilder {
    private static final transient Logger LOG = LoggerFactory.getLogger(FunktionRouteBuilder.class);

    // use servlet to map from http trigger to use Spring Boot servlet engine
    private static final String DEFAULT_TRIGGER_URL = "http://0.0.0.0:8080/";
    private static final String DEFAULT_HTTP_ENDPOINT_PREFIX = "servlet:funktion";

    private Set<String> localHosts = new HashSet<>(Arrays.asList("localhost", "0.0.0.0", "127.0.0.1"));

    // must have a main method spring-boot can run
    public static void main(String[] args) {
        ApplicationContext applicationContext = new SpringApplication(FunktionRouteBuilder.class).run(args);
        CamelSpringBootApplicationController ctx = applicationContext.getBean(CamelSpringBootApplicationController.class);
        ctx.run();
    }

    private static String replacePrefix(String text, String prefix, String replacement) {
        if (text.startsWith(prefix)) {
            return replacement + text.substring(prefix.length());
        }
        return text;
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
        Funktion config = loadFunktion();

        int idx = 0;
        List<Flow> rules = config.getFlows();
        for (Flow rule : rules) {
            configureRule(rule, idx++);
        }
    }

    protected Funktion loadFunktion() throws IOException {
        return Funktions.load();
    }

    protected void configureRule(Flow flow, int funktionIndex) throws MalformedURLException {
        getContext().setStreamCaching(true);

        if (flow.isTraceEnabled()) {
            getContext().setTracing(true);
        }

        StringBuilder message = new StringBuilder("FLOW ");
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
                        } else {
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
                        route = fromOrTo(route, name, uri, message);
                        message.append(uri);
                        validSteps++;
                    }
                } else {
                    addStep(route, item);
                    validSteps++;
                }
            }
        }
        if (route == null || validSteps == 0) {
            throw new IllegalStateException("No valid steps! Invalid flow " + flow);

        }
        if (flow.isLogResultEnabled()) {
            String chain = "log:" + name + "?showStreams=true";
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


    protected void addSteps(ProcessorDefinition route, Iterable<Step> steps) {
        if (route != null && steps != null) {
            for (Step item : steps) {
                route = addStep(route, item);
            }
        }
    }

    private ProcessorDefinition addStep(ProcessorDefinition route, Step item) {
        assertRouteNotNull(route, item);
        if (item instanceof Function) {
            Function function = (Function) item;
            String functionName = function.getName();
            if (!Strings.isEmpty(functionName)) {
                route.to("json:marshal");
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
                uri = convertEndpointURI(uri);
                route = route.to(uri);
            }
        } else if (item instanceof Endpoint) {
            Endpoint invokeEndpoint = (Endpoint) item;
            String uri = invokeEndpoint.getUri();
            if (!Strings.isEmpty(uri)) {
                uri = convertEndpointURI(uri);
                route = route.to("json:marshal");
                route = route.to(uri);
            }
        } else if (item instanceof SetBody) {
            SetBody step = (SetBody) item;
            route.setBody(constant(step.getBody()));
        } else if (item instanceof Throttle) {
            Throttle step = (Throttle) item;
            ThrottleDefinition throttle = route.throttle(step.getMaximumRequests());
            Long period = step.getPeriodMillis();
            if (period != null) {
                throttle.timePeriodMillis(period);
            }
            addSteps(throttle, step.getSteps());
        } else if (item instanceof SetHeaders) {
            SetHeaders step = (SetHeaders) item;
            Map<String, Object> headers = step.getHeaders();
            if (headers != null) {
                Set<Map.Entry<String, Object>> entries = headers.entrySet();
                for (Map.Entry<String, Object> entry : entries) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    route.setHeader(key, constant(value));
                }
            }
        } else if (item instanceof Filter) {
            Filter step = (Filter) item;
            Predicate predicate = getMandatoryPredicate(step, step.getExpression());
            FilterDefinition filter = route.filter(predicate);
            addSteps(filter, step.getSteps());
        } else if (item instanceof Split) {
            Split step = (Split) item;
            Expression expression = getMandatoryExpression(step, step.getExpression());
            SplitDefinition split = route.split(expression);
            addSteps(split, step.getSteps());
        } else if (item instanceof Choice) {
            Choice step = (Choice) item;
            ChoiceDefinition choice = route.choice();
            List<Filter> filters = notNullList(step.getFilters());
            for (Filter filter : filters) {
                Predicate predicate = getMandatoryPredicate(filter, filter.getExpression());
                ChoiceDefinition when = choice.when(predicate);
                addSteps(when, filter.getSteps());
            }
            Otherwise otherwiseStep = step.getOtherwise();
            if (otherwiseStep != null) {
                List<Step> otherwiseSteps = notNullList(otherwiseStep.getSteps());
                if (!otherwiseSteps.isEmpty()) {
                    ChoiceDefinition otherwise = choice.otherwise();
                    addSteps(otherwise, otherwiseSteps);
                }
            }
        } else if (item instanceof Log) {
            Log step = (Log) item;
            LoggingLevel loggingLevel = LoggingLevel.INFO;
            if (step.getLoggingLevel() != null) {
                loggingLevel = LoggingLevel.valueOf(step.getLoggingLevel());
            }
            route.log(loggingLevel, step.getLogger(), step.getMarker(), step.getMessage());
        } else {
            throw new IllegalStateException("Unknown step kind: " + item + " of class: " + item.getClass().getName());
        }
        return route;
    }

    protected Predicate getMandatoryPredicate(Step step, String expression) {
        Objects.requireNonNull(expression, "No expression specified for step " + step);
        Language jsonpath = getLanguage();
        Predicate answer = jsonpath.createPredicate(expression);
        Objects.requireNonNull(answer, "No predicate created from: " + expression);
        return answer;
    }

    protected Expression getMandatoryExpression(Step step, String expression) {
        Objects.requireNonNull(expression, "No expression specified for step " + step);
        Language jsonpath = getLanguage();
        Expression answer = jsonpath.createExpression(expression);
        Objects.requireNonNull(answer, "No expression created from: " + expression);
        return answer;
    }

    protected Language getLanguage() {
        String languageName = "jsonpath";
        Language answer = getContext().resolveLanguage(languageName);
        Objects.requireNonNull(answer, "The language `" + languageName + "` cound not be resolved!");
        return answer;
    }

    protected void assertRouteNotNull(ProcessorDefinition route, Step item) {
        if (route == null) {
            throw new IllegalArgumentException("You cannot use a " + item.getKind() + " step before you have started a flow with an endpoint or function!");
        }
    }

    protected RouteDefinition fromOrTo(RouteDefinition route, String name, String uri, StringBuilder message) {
        if (route == null) {
            String trigger = uri;
            if (Strings.isEmpty(trigger)) {
                trigger = DEFAULT_TRIGGER_URL;
            }
            message.append(name);
            message.append("() ");

            if (trigger.equals("http")) {
                trigger = DEFAULT_HTTP_ENDPOINT_PREFIX;
            } else if (trigger.startsWith("http:") || trigger.startsWith("https:") ||
                    trigger.startsWith("http://") || trigger.startsWith("https://")) {

                String host = getURIHost(trigger);
                if (localHosts.contains(host)) {
                    trigger = DEFAULT_HTTP_ENDPOINT_PREFIX;
                } else {

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
            }
            route = from(trigger);
            route.id(name);
        } else {
            uri = convertEndpointURI(uri);
            message.append(" => ");
            route.to(uri);
        }
        return route;
    }

    private String convertEndpointURI(String uri) {
        if (uri.startsWith("http:") || uri.startsWith("https:")) {
            // lets use http4 for all http transports
            uri = replacePrefix(uri, "http:", "http4:");
            uri = replacePrefix(uri, "https:", "https4:");

            HttpEndpoint endpoint = endpoint(uri, HttpEndpoint.class);
            if (endpoint != null) {
                // lets bridge them as a proxy
                endpoint.setBridgeEndpoint(true);
                endpoint.setThrowExceptionOnFailure(false);
            }
        }
        return uri;
    }

    private String getURIHost(String uri) {
        try {
            return new URI(uri).getHost();
        } catch (URISyntaxException e) {
            return null;
        }
    }


}
