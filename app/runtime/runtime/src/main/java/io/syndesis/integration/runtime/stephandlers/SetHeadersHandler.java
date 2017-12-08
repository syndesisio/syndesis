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
package io.syndesis.integration.runtime.stephandlers;

import java.util.Map;

import com.google.auto.service.AutoService;
import io.syndesis.integration.model.steps.SetHeaders;
import io.syndesis.integration.model.steps.Step;
import io.syndesis.integration.runtime.StepHandler;
import io.syndesis.integration.runtime.SyndesisRouteBuilder;
import org.apache.camel.model.ProcessorDefinition;

@AutoService(StepHandler.class)
public class SetHeadersHandler implements StepHandler<SetHeaders> {
    @Override
    public boolean canHandle(Step step) {
        return step.getClass().equals(SetHeaders.class);
    }

    @Override
    public ProcessorDefinition handle(SetHeaders step, ProcessorDefinition route, SyndesisRouteBuilder routeBuilder) {
        Map<String, Object> headers = step.getHeaders();
        if (headers != null) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                route.setHeader(entry.getKey(), routeBuilder.constant(entry.getValue()));
            }
        }
        return route;
    }
}
