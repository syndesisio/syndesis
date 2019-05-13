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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;
import io.syndesis.integration.runtime.capture.OutMessageCaptureProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataMapperStepHandler implements IntegrationStepHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DataMapperStepHandler.class);

    private static final String ATLASMAP_MODEL_VERSION = "v2";
    static final String ATLASMAP_JSON_DATA_SOURCE = "io.atlasmap.json." + ATLASMAP_MODEL_VERSION + ".JsonDataSource";

    /** Exchange property key marks that source body has been converted before mapping  */
    static final String DATA_MAPPER_AUTO_CONVERSION = "Syndesis.DATA_MAPPER_AUTO_CONVERSION";

    @Override
    public boolean canHandle(Step step) {
        return StepKind.mapper == step.getStepKind();
    }

    @Override
    public Optional<ProcessorDefinition<?>> handle(Step step, ProcessorDefinition<?> route, IntegrationRouteBuilder builder, String flowIndex, String stepIndex) {
        ObjectHelper.notNull(route, "route");

        List<Map<String, Object>> dataSources = getAtlasmapDataSources(step.getConfiguredProperties());

        addJsonTypeSourceProcessor(route, dataSources);
        route.toF("atlas:mapping-flow-%s-step-%s.json?encoding=UTF-8&sourceMapName=%s", flowIndex, stepIndex, OutMessageCaptureProcessor.CAPTURED_OUT_MESSAGES_MAP);
        addJsonTypeTargetProcessor(route, dataSources);

        return Optional.of(route);
    }

    /**
     * In case atlas mapping definition contains Json typed source documents we need to make sure to convert those from list to Json array Strings before passing those
     * source documents to the mapper.
     * @param route
     * @param dataSources
     * @return
     */
    private void addJsonTypeSourceProcessor(ProcessorDefinition<?> route, List<Map<String, Object>> dataSources) {
        List<Map<String, Object>> sourceDocuments = dataSources.stream()
                                            .filter(s -> "SOURCE".equals(s.get("dataSourceType")))
                                            .collect(Collectors.toList());

        List<String> jsonTypeSourceIds = sourceDocuments.stream()
                                            .filter(s -> ATLASMAP_JSON_DATA_SOURCE.equals(s.get("jsonType")))
                                            .filter(s -> ObjectHelper.isNotEmpty(s.get("id")))
                                            .map(s -> s.get("id").toString())
                                            .collect(Collectors.toList());

        if (ObjectHelper.isNotEmpty(jsonTypeSourceIds)) {
            route.process(new JsonTypeSourceProcessor(jsonTypeSourceIds, sourceDocuments.size()));
        }
    }

    /**
     * In case mapping definition has Json typed target document we need to make sure to convert the output. This is because mapper provides Json target collection as Json array String representation.
     * We prefer to use list objects where each element is a Json Object String.
     * @param route
     * @param dataSources
     * @return
     */
    private void addJsonTypeTargetProcessor(ProcessorDefinition<?> route, List<Map<String, Object>> dataSources) {
        boolean isJsonTypeTarget = dataSources.stream()
                .anyMatch(s -> ATLASMAP_JSON_DATA_SOURCE.equals(s.get("jsonType")) && "TARGET".equals(s.get("dataSourceType")));

        if (isJsonTypeTarget) {
            route.process(new JsonTypeTargetProcessor());
        }
    }

    /**
     * Reads atlas mapping definition from configured step properties and extracts all data source elements.
     * @param configuredProperties
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getAtlasmapDataSources(Map<String, String> configuredProperties) {
        List<Map<String, Object>> sources = new ArrayList<>();

        try {
            Map<String, Object> atlasMapping = Json.reader().forType(Map.class).readValue(configuredProperties.getOrDefault("atlasmapping", "{}"));
            atlasMapping = (Map<String, Object>) atlasMapping.getOrDefault("AtlasMapping", new HashMap<>());
            sources = (List<Map<String, Object>>) atlasMapping.getOrDefault("dataSource", Collections.emptyList());
        } catch (IOException | ClassCastException e) {
            LOG.warn("Failed to read atlas mapping definition from configured properties", e);
        }

        return sources;
    }

    /**
     * Processor converts all Json collection typed entries in captured out messages to a
     * Json array String representation. See {@link OutMessageCaptureProcessor}
     */
    static class JsonTypeSourceProcessor implements Processor {
        final int overallSourceDocCount;
        final List<String> jsonTypeSourceIds;

        JsonTypeSourceProcessor(List<String> jsonTypeSourceIds, int overallSourceDocCount) {
            this.jsonTypeSourceIds = jsonTypeSourceIds;
            this.overallSourceDocCount = overallSourceDocCount;
        }

        @Override
        public void process(Exchange exchange) throws Exception {
            // When only on single source document is provided Atlasmap will always use the current exchange message as source document.
            if (overallSourceDocCount == 1) {
                Message message = exchange.hasOut() ? exchange.getOut() : exchange.getIn();
                convertMessageJsonTypeBody(exchange, message);
            }

            for (String sourceId : jsonTypeSourceIds) {
                Message message = OutMessageCaptureProcessor.getCapturedMessageMap(exchange).get(sourceId);

                if (message == null && jsonTypeSourceIds.size() == 1) {
                    message = exchange.hasOut() ? exchange.getOut() : exchange.getIn();
                }

                convertMessageJsonTypeBody(exchange, message);
            }
        }

        /**
         * Convert list typed message body to Json array String representation.
         * @param message
         */
        private void convertMessageJsonTypeBody(Exchange exchange, Message message) {
            if (message != null && message.getBody() instanceof List) {
                List<?> jsonBeans = message.getBody(List.class);
                message.setBody(JsonUtils.jsonBeansToArray(jsonBeans));

                // mark auto conversion so we can reconvert after data mapper is done
                exchange.setProperty(DATA_MAPPER_AUTO_CONVERSION, true);
            }
        }
    }

    /**
     * Processor converts Atlasmap target Json array String representation to list of Json bean strings.
     */
    static class JsonTypeTargetProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            if (exchange.removeProperty(DATA_MAPPER_AUTO_CONVERSION) != null) {
                final Message message = exchange.hasOut() ? exchange.getOut() : exchange.getIn();

                if (message != null && message.getBody(String.class) != null) {
                    try {
                        JsonNode json = Json.reader().readTree(message.getBody(String.class));
                        if (json.isArray()) {
                            message.setBody(JsonUtils.arrayToJsonBeans(json));
                        }
                    } catch (JsonParseException e) {
                        LOG.warn("Unable to convert json array type String to required format", e);
                    }
                }
            }
        }
    }
}
