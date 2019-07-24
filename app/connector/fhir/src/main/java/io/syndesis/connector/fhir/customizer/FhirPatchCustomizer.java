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
package io.syndesis.connector.fhir.customizer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.fhir.internal.FhirPatchApiMethod;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FhirPatchCustomizer implements ComponentProxyCustomizer {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> JSON_PATCH_MAP = new TypeReference<Map<String, Object>>() {
        // type token pattern
    };
    private String resourceType;
    private String id;
    private String patch;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        options.put("apiName", FhirCustomizerHelper.getFhirApiName(FhirPatchApiMethod.class));
        options.put("methodName", "patchById");

        resourceType = ConnectorOptions.extractOption(options, "resourceType");
        id = ConnectorOptions.extractOption(options, "id");
        patch = ConnectorOptions.extractOption(options, "patch");

        component.setBeforeProducer(this::beforeProducer);
    }

    private void beforeProducer(Exchange exchange) {
        Message in = exchange.getIn();
        if (StringUtils.isNotBlank(id)) {
            in.setHeader("CamelFhir.stringId", resourceType + "/" + id);
        }

        in.setHeader("CamelFhir.preferReturn", null);

        String body = in.getBody(String.class);
        if (body != null) {
            try {
                Map<String, Object> map = MAPPER.readValue(body, JSON_PATCH_MAP);

                List<Map<String, String>> list = new ArrayList<>();
                for(Map.Entry<String, Object> entry: map.entrySet()) {
                    if ("id".equals(entry.getKey()) && ObjectHelper.isNotEmpty(entry.getValue())) {
                        in.setHeader("CamelFhir.stringId", resourceType + "/" + entry.getValue());
                    } else if (entry.getValue() instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, String> operation = (Map<String, String>) entry.getValue();
                        if (ObjectHelper.isEmpty(ConnectorOptions.extractOption(operation, "op"))) {
                            operation.put("op", "replace"); //'replace' by default
                        }
                        list.add(operation);
                    }
                }

                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    MAPPER.writeValue(out, list);
                    patch = out.toString("UTF-8");
                } catch (IOException e) {
                    throw new CamelExecutionException("Cannot serialize body to json", exchange, e);
                }
            } catch (IOException e) {
                //Body might be in the correct format already, so use it as is
                patch = body;
            }
        }
        in.setHeader("CamelFhir.patchBody", patch);
    }
}
