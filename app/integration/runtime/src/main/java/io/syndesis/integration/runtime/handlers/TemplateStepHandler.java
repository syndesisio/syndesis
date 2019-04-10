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
package io.syndesis.integration.runtime.handlers;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.URISupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.model.integration.step.template.TemplateStepConstants;
import io.syndesis.common.model.integration.step.template.TemplateStepLanguage;
import io.syndesis.common.util.StringConstants;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;

/**
 * Handler for processing JSON messages and using their properties to populate a
 * template written in recognised template languages.
 */
public class TemplateStepHandler implements IntegrationStepHandler, StringConstants {

    private static final String TEMPLATE_PROPERTY = "template";

    private final JsonToMapProcessor jsonToMapProcessor = new JsonToMapProcessor();

    private final TextToJsonProcessor textToJsonProcessor = new TextToJsonProcessor();

    @Override
    public boolean canHandle(Step step) {
        if (StepKind.template != step.getStepKind()) {
            return false;
        }

        Action action = step.getAction().orElse(null);
        if (action == null) {
            return false;
        }

        DataShape inputDataShape = action.getInputDataShape().orElse(null);
        if (inputDataShape == null) {
            return false;
        }

        return DataShapeKinds.JSON_SCHEMA == inputDataShape.getKind();
    }

    @SuppressWarnings("PMD.AvoidReassigningParameters")
    private Optional<ProcessorDefinition<?>> handle(TemplateStepLanguage language, Step step, ProcessorDefinition<?> route, String flowIndex, String stepIndex) {
        Map<String, String> properties = step.getConfiguredProperties();
        String template = properties.get(TEMPLATE_PROPERTY);

        try {
            /*
             * Pre-process the template to ensure it conforms to the standard.
             */
            template = language.preProcess(template);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }

        //
        // Convert the exchange's in message from JSON
        // to a HashMap since this is required by camel modules
        //
        route = route.process(jsonToMapProcessor);

        //
        // Apply the template to the header property
        // Then add to the route path
        //
        route.setHeader(language.camelHeader()).constant(template);

        //
        // Encode the delimiters since they are applied as URI query parameters
        //
        try {
            String id = flowIndex + HYPHEN + stepIndex;
            String uri = language.generateUri(id);
            Map<String, Object> params = language.getUriParams();
            if (params != null) {
                uri = URISupport.appendParametersToURI(uri, params);
            }
            route = route.to(uri);

            //
            // Post-process the output exchange into JSON
            // so it will be available as part of a JSON object
            //
            route = route.process(textToJsonProcessor);

            return Optional.ofNullable(route);

        } catch (UnsupportedEncodingException | URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("PMD.AvoidReassigningParameters")
    @Override
    public Optional<ProcessorDefinition<?>> handle(Step step, ProcessorDefinition<?> route, IntegrationRouteBuilder builder, String flowIndex, String stepIndex) {
        ObjectHelper.notNull(route, "route");

        Map<String, String> properties = step.getConfiguredProperties();
        String languageId = properties.get(TemplateStepLanguage.LANGUAGE_PROPERTY);
        //
        // If languageId is null then defaults to mustache
        //
        TemplateStepLanguage language = TemplateStepLanguage.stepLanguage(languageId);
        return this.handle(language, step, route, flowIndex, stepIndex);
    }

    /**
     * Processor that is designed to jump in front of the template routing.
     * The handler accepts messages in JSON but the mustache route requires
     * all properties to be available in a {@link Map}. Thus, this parses the JSON
     * and converts it to the appropriate {@link Map}.
     */
    public static class JsonToMapProcessor implements Processor, TemplateStepConstants {

        private static final Logger LOGGER = LoggerFactory.getLogger(JsonToMapProcessor.class);

        private static final ObjectMapper MAPPER = new ObjectMapper();

        /*
         * If the key is prefixed with the "body" then remove it since the map
         * will be stored AS the body of the exchange which is then inserted
         * into a mustache map keyed with "body".
         */
        private void refactorKeys(Map<String, Object> map) {
            ArrayList<String> keys = new ArrayList<String>(map.keySet());
            keys.stream().forEach(key -> {
                Object value = map.remove(key);
                if (key.startsWith(BODY_PREFIX)) {
                    key = key.substring(BODY_PREFIX.length());
                    LOGGER.debug("Refactored Key: {}", key);
                }

                map.put(key, value);
            });
        }

        @SuppressWarnings( "unchecked" )
        @Override
        public void process(Exchange exchange) throws Exception {
            Object body = exchange.getIn().getBody();
            LOGGER.debug("Exchange In Body: {}", body);

            try {
                // Map the json body to a Map
                Map<String, Object> map = (Map<String, Object>) MAPPER.readValue(body.toString(), HashMap.class);

                // Refactor the keys of the map to remove any "body." notation since this
                // is no longer required during actual exchange processing
                refactorKeys(map);

                exchange.getIn().setBody(map);
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to pre-process the TemplateStep's input message (expecting JSON)", ex);
            }
        }
    }

    public static class TextToJsonProcessor implements Processor {

        private static final String MESSAGE_ATTRIBUTE = "message";

        private static final Logger LOGGER = LoggerFactory.getLogger(TextToJsonProcessor.class);

        private static final ObjectMapper MAPPER = new ObjectMapper();

        @Override
        public void process(Exchange exchange) throws Exception {
            Object body = exchange.getIn().getBody();
            LOGGER.debug("Exchange In Body: {}", body);

            try {
                ObjectNode node = MAPPER.createObjectNode();
                node.put(MESSAGE_ATTRIBUTE, body.toString());
                String newBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node);
                exchange.getIn().setBody(newBody);

                LOGGER.debug("New Exchange In Body: {}", newBody);
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to post-process the TemplateStep's message into JSON", ex);
            }
        }
    }
}
