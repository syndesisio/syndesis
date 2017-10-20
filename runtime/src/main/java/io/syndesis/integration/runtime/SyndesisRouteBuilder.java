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
package io.syndesis.integration.runtime;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;

import static java.util.Optional.ofNullable;

import io.syndesis.integration.model.Flow;
import io.syndesis.integration.model.SyndesisModel;
import io.syndesis.integration.model.steps.Endpoint;
import io.syndesis.integration.model.steps.Function;
import io.syndesis.integration.model.steps.Step;
import io.syndesis.integration.runtime.designer.SingleMessageRoutePolicyFactory;
import io.syndesis.integration.runtime.util.JsonSimplePredicate;
import io.syndesis.integration.support.Strings;
import org.apache.camel.Expression;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpEndpoint;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Camel {@link RouteBuilder} which maps the SyndesisModel rules to Camel routes
 */
public class SyndesisRouteBuilder extends RouteBuilder {
    private static final transient Logger LOG = LoggerFactory.getLogger(SyndesisRouteBuilder.class);

    // use servlet to map from http trigger to use Spring Boot servlet engine
    private static final String DEFAULT_TRIGGER_URL = "http://0.0.0.0:8080/";
    private static final String DEFAULT_HTTP_ENDPOINT_PREFIX = "servlet:syndesis";

    private Set<String> localHosts = new HashSet<>(Arrays.asList("localhost", "0.0.0.0", "127.0.0.1"));

    private final Set<StepHandler<? extends Step>> handlers;
    private final SyndesisModel model;

    public SyndesisRouteBuilder(SyndesisModel model, Collection<StepHandler<? extends Step>> handlers) {
        this.model = model;

        this.handlers = new HashSet<>(handlers);
        for (StepHandler handler : ServiceLoader.load(StepHandler.class, getClass().getClassLoader())) {
            this.handlers.add(handler);
        }
    }

    private static String replacePrefix(String text, String prefix, String replacement) {
        if (text.startsWith(prefix)) {
            return replacement + text.substring(prefix.length());
        }
        return text;
    }

    @Override
    public void configure() throws Exception {
        int idx = 0;
        List<Flow> rules = model.getFlows();
        for (Flow rule : rules) {
            configureRule(rule, idx++);
        }
    }

    protected void configureRule(Flow flow, int syndesisIndex) throws MalformedURLException {
        getContext().setStreamCaching(true);

        if (flow.isTraceEnabled()) {
            getContext().setTracing(true);
        }

        StringBuilder message = new StringBuilder("FLOW ");
        String name = flow.getName();
        if (Strings.isEmpty(name)) {
            name = "flow" + (syndesisIndex + 1);
            flow.setName(name);
        }

        RouteDefinition route = null;
        List<Step> steps = flow.getSteps();

        if (steps == null || steps.isEmpty()) {
            throw new IllegalStateException("No valid steps! Invalid flow " + flow);
        }

        for (Step item : steps) {
            if (item instanceof Function) {
                Function function = (Function) item;
                String functionName = function.getName();
                if (!Strings.isEmpty(functionName)) {
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
                    message.append(functionName).append(".").append(ofNullable(method).orElse("main")).append("()");
                }
            } else if (item instanceof Endpoint) {
                Endpoint invokeEndpoint = (Endpoint) item;
                String uri = invokeEndpoint.getUri();
                if (!Strings.isEmpty(uri)) {
                    route = fromOrTo(route, name, uri, message);
                    message.append(uri);
                }
            } else {
                addStep(route, item);
            }
        }

        if (flow.isLogResultEnabled()) {
            String chain = "log:" + name + "?showStreams=true";
            route.to(chain);
            message.append(" => ");
            message.append(chain);
        }
        LOG.info(message.toString());

        if (flow.isSingleMessageModeEnabled()) {
            LOG.info("Enabling single message mode so that only one message is consumed for Design Mode");
            getContext().addRoutePolicyFactory(new SingleMessageRoutePolicyFactory());
        }
    }

    public ProcessorDefinition addSteps(ProcessorDefinition route, Iterable<Step> steps) {
        if (route != null && steps != null) {
            for (Step item : steps) {
                route = addStep(route, item);
            }
        }
        return route;
    }

    private ProcessorDefinition addStep(ProcessorDefinition route, Step item) {
        assertRouteNotNull(route, item);
        for (StepHandler handler : handlers) {
            if (handler.canHandle(item)) {
                return handler.handle(item, route, this);
            }
        }

        throw new IllegalStateException("Unknown step kind: " + item + " of class: " + item.getClass().getName());
    }

    public Predicate getMandatorySimplePredicate(Step step, String expression) {
        Objects.requireNonNull(expression, "No expression specified for step " + step);
        Predicate answer = new JsonSimplePredicate(expression, getContext());
        Objects.requireNonNull(answer, "No predicate created from: " + expression);
        return answer;
    }

    public Expression getMandatoryExpression(Step step, String expression) {
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
            String trigger = !Strings.isEmpty(uri) ? uri : DEFAULT_TRIGGER_URL;
            message.append(name);
            message.append("() ");

            if (trigger.equals("http")) {
                trigger = DEFAULT_HTTP_ENDPOINT_PREFIX;
            } else if (trigger.startsWith("http:") || trigger.startsWith("https:")) {

                String host = getURIHost(trigger);
                if (localHosts.contains(host)) {
                    trigger = DEFAULT_HTTP_ENDPOINT_PREFIX;
                } else {

                    // lets add the HTTP endpoint prefix

                    // is there any context-path
                    String path = getWithoutPrefixIfItStartsWithPrefix(trigger, "https://", "http://", "http:", "https:");
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

    private String getWithoutPrefixIfItStartsWithPrefix(String trigger, String ... prefixes) {
        for (String prefix : prefixes) {
            if (trigger.startsWith(prefix)) {
                return trigger.substring(prefix.length());
            }
        }
        return null;
    }

    public String convertEndpointURI(String uri) {
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
