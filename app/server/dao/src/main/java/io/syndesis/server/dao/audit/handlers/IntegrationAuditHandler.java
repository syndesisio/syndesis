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
package io.syndesis.server.dao.audit.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.server.dao.audit.AuditEvent;

final class IntegrationAuditHandler extends AuditHandler<Integration> {

    private final FlowAuditHandler flowHandler = new FlowAuditHandler();

    private final SinglePropertyAuditHandler<Integration> nameHandler = new SinglePropertyAuditHandler<>("name", Integration::getName);

    private List<AuditEvent> flowDefinition(final Integration current) {
        return current.getFlows().stream()
            .map(flowHandler::definition)
            .collect(MERGE_EVENT_LISTS);
    }

    private List<AuditEvent> flowDifference(final Integration current, final Integration previous) {
        return Stream.concat(
            current.getFlows().stream()
                .map(c -> whenFlowsChange(c, previous)),
            previous.getFlows().stream()
                .map(p -> whenFlowsAreRemoved(p, current)))
            .collect(MERGE_EVENT_LISTS);
    }

    private List<AuditEvent> whenFlowsAreRemoved(final Flow previousFlow, final Integration currentIntegration) {
        return currentIntegration.findFlowById(flowIdFrom(previousFlow))
            // current integration contains a flow from previous integration,
            // this case was handled in whenFlowsChange
            .map(c -> Collections.<AuditEvent>emptyList())
            // current integration doesn't contain a flow that exists in the
            // previous integration, generate events for removal
            .orElseGet(() -> flowHandler.difference(null, previousFlow));
    }

    private List<AuditEvent> whenFlowsChange(final Flow currentFlow, final Integration previousIntegration) {
        return previousIntegration.findFlowById(flowIdFrom(currentFlow))
            // flow from the current integration is present in the previous
            // integration, see if there are differences
            .map(previousFlow -> flowHandler.difference(currentFlow, previousFlow))
            // flow from the current integration is not present in the previous
            // integration -> a new flow was added to the integration
            .orElseGet(() -> flowHandler.definition(currentFlow));
    }

    @Override
    protected List<AuditEvent> definition(final Integration current) {
        final List<AuditEvent> events = new ArrayList<>();

        events.addAll(nameHandler.definition(current));
        events.addAll(flowDefinition(current));

        return events;
    }

    @Override
    protected List<AuditEvent> difference(final Integration current, final Integration previous) {
        final List<AuditEvent> events = new ArrayList<>();

        events.addAll(nameHandler.difference(current, previous));
        events.addAll(flowDifference(current, previous));

        return events;
    }

    private static String flowIdFrom(final Flow flow) {
        return flow.getId().orElse("");
    }
}
