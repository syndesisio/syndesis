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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collector;

import io.syndesis.common.model.WithConfiguredProperties;
import io.syndesis.common.model.WithName;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.server.dao.audit.AuditEvent;

public abstract class AuditHandler<T> {
    static final Collector<List<AuditEvent>, List<AuditEvent>, List<AuditEvent>> MERGE_EVENT_LISTS = Collector.of(
        ArrayList::new,
        AuditHandler::concat,
        AuditHandler::concat,
        Collector.Characteristics.IDENTITY_FINISH);

    private static class PrefixedHandler<T> extends AuditHandler<T> {

        private final AuditHandler<T> delegate;

        private final BiFunction<T, String, String> prefixer;

        public PrefixedHandler(final AuditHandler<T> delegate, final BiFunction<T, String, String> prefixer) {
            this.delegate = delegate;
            this.prefixer = prefixer;
        }

        private List<AuditEvent> prefix(final T object, final List<AuditEvent> events) {
            final List<AuditEvent> prefixed = new ArrayList<>(events.size());

            for (final AuditEvent event : events) {
                prefixed.add(event.withProperty(prefixer.apply(object, event.property())));
            }

            return prefixed;
        }

        @Override
        protected List<AuditEvent> definition(final T current) {
            return prefix(current, delegate.definition(current));
        }

        @Override
        protected List<AuditEvent> difference(final T current, final T previous) {
            return prefix(current == null ? previous : current, delegate.difference(current, previous));
        }
    }

    public final List<AuditEvent> handle(final T current, final Optional<T> previous) {
        return previous
            .map(p -> difference(current, p))
            .orElseGet(() -> definition(current));
    }

    final AuditHandler<T> prefixedWith(final BiFunction<T, String, String> prefixer) {
        return new PrefixedHandler<>(this, prefixer);
    }

    protected abstract List<AuditEvent> definition(T current);

    protected abstract List<AuditEvent> difference(T current, T previous);

    public static Map<Class<?>, AuditHandler<?>> handlers() {
        final Map<Class<?>, AuditHandler<?>> handlers = new HashMap<>();

        handlers.put(WithName.class, new WithNameAuditHandler());
        handlers.put(WithConfiguredProperties.class, new WithConfiguredPropertiesAuditHandler());
        handlers.put(Integration.class, new IntegrationAuditHandler());

        return Collections.unmodifiableMap(handlers);
    }

    static Optional<AuditEvent> propertyDifference(final String propertyName, final String current, final String previous) {
        if (!Objects.equals(previous, current)) {
            return Optional.of(AuditEvent.propertyChanged(propertyName, previous, current));
        }

        return Optional.empty();
    }

    private static List<AuditEvent> concat(final List<AuditEvent> a, final List<AuditEvent> b) {
        a.addAll(b);

        return a;
    }
}
