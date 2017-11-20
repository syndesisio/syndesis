/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.project.converter.visitor;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.syndesis.integration.model.steps.Endpoint;
import io.syndesis.integration.model.steps.Extension;
import io.syndesis.integration.model.steps.Function;
import io.syndesis.integration.model.steps.SetHeaders;
import io.syndesis.model.action.ExtensionAction;

public final class ExtensionStepVisitor implements StepVisitor {
    public static class Factory implements StepVisitorFactory<ExtensionStepVisitor> {
        @Override
        public String getStepKind() {
            return Extension.KIND;
        }

        @Override
        public ExtensionStepVisitor create() {
            return new ExtensionStepVisitor();
        }
    }

    @Override
    public Collection<io.syndesis.integration.model.steps.Step> visit(StepVisitorContext visitorContext) {
        return visitorContext.getStep().getAction()
            .map(ExtensionAction.class::cast)
            .map(action -> createExtension(visitorContext, action))
            .orElseGet(Collections::emptyList);
    }

    private Collection<io.syndesis.integration.model.steps.Step> createExtension(StepVisitorContext visitorContext, ExtensionAction action) {
        final List<io.syndesis.integration.model.steps.Step> steps = new ArrayList<>();

        if (action.getDescriptor().getKind() == ExtensionAction.Kind.ENDPOINT) {
            SetHeaders headers = new SetHeaders();
            headers.setHeaders(Map.class.cast(visitorContext.getStep().getConfiguredProperties()));

            Endpoint endpoint = new Endpoint();
            endpoint.setUri(action.getDescriptor().getEntrypoint());

            steps.add(headers);
            steps.add(endpoint);
        } else if (action.getDescriptor().getKind() == ExtensionAction.Kind.BEAN) {
            Function function = new Function();
            function.setName(action.getDescriptor().getEntrypoint());
            function.setProperties(Map.class.cast(visitorContext.getStep().getConfiguredProperties()));

            steps.add(function);
        } else if (action.getDescriptor().getKind() == ExtensionAction.Kind.STEP) {
            Extension extension = new Extension();
            extension.setName(action.getDescriptor().getEntrypoint());
            extension.setProperties(Map.class.cast(visitorContext.getStep().getConfiguredProperties()));

            steps.add(extension);
        }

        return steps;
    }
}
