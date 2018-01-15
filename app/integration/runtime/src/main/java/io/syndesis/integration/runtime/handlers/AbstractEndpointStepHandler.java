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

import java.util.Optional;

import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;
import io.syndesis.model.Split;
import io.syndesis.model.action.ConnectorDescriptor;
import io.syndesis.model.integration.SimpleStep;
import org.apache.camel.model.ProcessorDefinition;

abstract class AbstractEndpointStepHandler implements IntegrationStepHandler {

    protected Optional<ProcessorDefinition> handleSplit(ConnectorDescriptor descriptor, ProcessorDefinition route, IntegrationRouteBuilder builder) {
        // Handle split
        if (descriptor.getSplit().isPresent()) {
            final Split split = descriptor.getSplit().get();
            final SimpleStep.Builder splitBuilder = new SimpleStep.Builder().stepKind("split");

            split.getLanguage().ifPresent(s -> splitBuilder.putConfiguredProperty("language", s));
            split.getExpression().ifPresent(s -> splitBuilder.putConfiguredProperty("expression", s));

            route = new SplitStepHandler()
                .handle(
                    splitBuilder.build(),
                    route,
                    builder)
                .orElse(route);
        }

        return Optional.of(route);
    }
}
