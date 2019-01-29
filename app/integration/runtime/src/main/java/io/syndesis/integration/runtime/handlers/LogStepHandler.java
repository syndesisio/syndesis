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

import java.util.Map;
import java.util.Optional;

import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;
import org.apache.camel.LoggingLevel;
import org.apache.camel.model.ProcessorDefinition;

public class LogStepHandler implements IntegrationStepHandler {

    @Override
    public boolean canHandle(Step step) {
        return step.getStepKind() == StepKind.log;
    }

    @Override
    public Optional<ProcessorDefinition<?>> handle(Step step, ProcessorDefinition<?> route, IntegrationRouteBuilder builder, String flowIndex, String stepIndex) {
        final String message = createMessage(step);
        if (message.isEmpty()) {
            return Optional.empty();
        }

        if (step.getId().isPresent()) {
            String stepId = step.getId().get();
            return Optional.of(route.log(LoggingLevel.INFO, (String) null, stepId, message));
        }

        return Optional.of(route.log(LoggingLevel.INFO, message));
    }


    static String createMessage(Step l) {
        StringBuilder sb = new StringBuilder(128);
        String customText = getCustomText(l.getConfiguredProperties());
        boolean isContextLoggingEnabled = isContextLoggingEnabled(l.getConfiguredProperties());
        boolean isBodyLoggingEnabled = isBodyLoggingEnabled(l.getConfiguredProperties());

        if (isContextLoggingEnabled) {
            sb.append("Message Context: [${in.headers}] ");
        }
        if (isBodyLoggingEnabled) {
            sb.append("Body: [${bean:bodyLogger}] ");
        }
        if (customText != null &&
                !customText.isEmpty() &&
                !customText.equals("null")) { //TODO: the null thing should be handled by the ui.
            sb.append(customText);
        }

        return sb.toString();
    }

    private static boolean isContextLoggingEnabled(Map<String, String> props) {
        if (props == null || props.isEmpty()) {
            return false;
        }
        return Boolean.parseBoolean(props.getOrDefault("contextLoggingEnabled", "false"));
    }

    private static boolean isBodyLoggingEnabled(Map<String, String> props) {
        if (props == null || props.isEmpty()) {
            return false;
        }
        return Boolean.parseBoolean(props.getOrDefault("bodyLoggingEnabled", "false"));
    }

    private static String getCustomText(Map<String, String> props) {
        if (props == null || props.isEmpty()) {
            return null;
        }
        return props.get("customText");
    }
}
