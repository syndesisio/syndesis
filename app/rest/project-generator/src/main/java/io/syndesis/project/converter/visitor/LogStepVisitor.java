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
package io.syndesis.project.converter.visitor;

import io.syndesis.integration.model.steps.Log;
import io.syndesis.integration.support.Strings;
import io.syndesis.model.integration.Step;
import io.syndesis.model.log.LogStep;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;


public class LogStepVisitor implements StepVisitor {


    public static class Factory implements StepVisitorFactory<LogStepVisitor> {
        @Override
        public String getStepKind() {
            return LogStep.STEP_KIND;
        }

        @Override
        public LogStepVisitor create() {
            return new LogStepVisitor();
        }
    }

    protected String getStepKind() {
        return LogStep.STEP_KIND;
    }


    @Override
    public Collection<io.syndesis.integration.model.steps.Step> visit(StepVisitorContext stepContext) {
        Step step = stepContext.getStep();
        if (!(step instanceof LogStep)) {
            throw new IllegalStateException("Expected instance of LogStep!");
        }

        LogStep l = (LogStep) step;

        StringBuilder sb = new StringBuilder(128);
        configureProperties(sb, l.getPropertyNames());
        configureHeaders(sb, HeaderKind.in, l.getInHeaderNames());
        configureHeaders(sb, HeaderKind.out, l.getOutHeaderNames());

        if (l.isBodyLoggingEnabled()) {
            sb.append("Body: [${body}] ");
        }

        if (Strings.isNotEmpty(l.getExpression())) {
            sb.append(l.getExpression());
        }

        return Arrays.asList(new Log(sb.toString(), "INFO", "io.syndesis.integration", null));
    }


    private void configureProperties(StringBuilder sb, Collection<String> names) {
        if (names != null && !names.isEmpty()) {
            sb.append("Exchange properties: [");
            sb.append(names.stream().map(p -> "$property." + p).collect(Collectors.joining(" ")));
            sb.append("] ");
        }
    }


    private void configureHeaders(StringBuilder sb, HeaderKind kind, Collection<String> names) {
        if (kind == null) {
            throw new NullPointerException("Kind should not be null. Please specify a valid value.");
        }
        if (names != null && !names.isEmpty()) {
            sb.append(kind.getLabel());
            sb.append(" Headers: [");
            sb.append(names.stream().map(h -> "$"+kind.name()+".header.." + h).collect(Collectors.joining(" ")));
            sb.append("] ");
        }
    }

    private enum HeaderKind {
        in("Input"),
        out("Output");

        private final String label;

        HeaderKind(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
