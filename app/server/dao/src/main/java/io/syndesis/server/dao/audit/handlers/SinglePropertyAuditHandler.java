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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.syndesis.server.dao.audit.AuditEvent;

abstract class SinglePropertyAuditHandler<T> extends AuditHandler<T> {

    private final Function<T, String> propertyExtractor;

    private final String propertyName;

    SinglePropertyAuditHandler(final String propertyName, final Function<T, String> propertyExtractor) {
        this.propertyName = propertyName;
        this.propertyExtractor = propertyExtractor;
    }

    @Override
    public final List<AuditEvent> definition(final T current) {
        final AuditEvent event = AuditEvent.propertySet(propertyName, propertyExtractor.apply(current));

        return Collections.singletonList(event);
    }

    @Override
    public final List<AuditEvent> difference(final T current, final T previous) {
        final Optional<AuditEvent> event = AuditHandler.propertyDifference(propertyName, propertyExtractor.apply(current), propertyExtractor.apply(previous));

        return event
            .map(Collections::singletonList)
            .orElseGet(Collections::emptyList);
    }
}
