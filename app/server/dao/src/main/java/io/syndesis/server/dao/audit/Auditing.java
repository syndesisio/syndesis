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
package io.syndesis.server.dao.audit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import io.syndesis.common.model.Audited;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.WithName;
import io.syndesis.server.dao.audit.AuditRecord.RecordType;
import io.syndesis.server.dao.audit.handlers.AuditHandler;

public final class Auditing {

    private static final Map<Class<?>, AuditHandler<?>> HANDLERS = AuditHandler.handlers();

    private final Map<Class<?>, List<AuditHandler<?>>> handlerCache = new ConcurrentHashMap<>();

    private final Supplier<Long> time;

    private final Supplier<String> username;

    public Auditing(final Supplier<Long> time, final Supplier<String> username) {
        this.time = time;
        this.username = username;
    }

    public Auditing(final Supplier<String> username) {
        this(System::currentTimeMillis, username);
    }

    public <T extends WithId<T>> Optional<AuditRecord> onCreate(final T created) {
        if (!shouldAudit(created)) {
            return Optional.empty();
        }

        return createdOrUpdated(created, RecordType.created);
    }

    public <T extends WithId<T>> Optional<AuditRecord> onDelete(final T deleted) {
        if (!shouldAudit(deleted)) {
            return Optional.empty();
        }

        final String id = deleted.getId().get();
        final String name = determineName(deleted);

        return Optional.of(new AuditRecord(id, modelName(deleted), name, time.get(), username.get(), RecordType.deleted, Collections.emptyList()));
    }

    public <T extends WithId<T>> Optional<AuditRecord> onUpdate(final T updated) {
        if (!shouldAudit(updated)) {
            return Optional.empty();
        }

        return createdOrUpdated(updated, RecordType.updated);
    }

    public <T extends WithId<T>> Optional<AuditRecord> onUpdate(final T previous, final T current) {
        if (!shouldAudit(current)) {
            return Optional.empty();
        }

        final List<AuditEvent> events = computeEvents(current, Optional.of(previous));

        if (events.isEmpty()) {
            return Optional.empty();
        }

        final String id = current.getId().orElse("*");
        final String name = determineName(current);

        return Optional.of(new AuditRecord(id, modelName(current), name, time.get(), username.get(), RecordType.updated, events));
    }

    <T extends WithId<T>> Collection<AuditHandler<T>> handlersFor(final T current) {
        // We're playing fast and loose with generic type erasure, that's what
        // we get when we play with generics at this complexity, hopefully code
        // coverage helps us with edge cases
        @SuppressWarnings({"unchecked", "rawtypes"})
        final Map<Class<T>, List<AuditHandler<T>>> muddled = (Map) handlerCache;

        @SuppressWarnings({"unchecked", "rawtypes"})
        final Class<T> clazz = (Class) current.getClass();

        return muddled.computeIfAbsent(clazz, Auditing::determineHandlersFor);
    }

    private <T extends WithId<T>> List<AuditEvent> computeEvents(final T current, final Optional<T> previous) {
        final List<AuditEvent> changes = new ArrayList<>();

        for (final AuditHandler<T> handler : handlersFor(current)) {
            final List<AuditEvent> changesFromHandler = handler.handle(current, previous);
            changes.addAll(changesFromHandler);
        }

        return changes;
    }

    private <T extends WithId<T>> Optional<AuditRecord> createdOrUpdated(final T object, final RecordType recordType) {
        final String id = object.getId().orElse("*");
        final String name = determineName(object);

        final List<AuditEvent> events = computeEvents(object, Optional.empty());

        return Optional.of(new AuditRecord(id, modelName(object), name, time.get(), username.get(), recordType, events));
    }

    static Set<Class<?>> allTypesIn(final Class<?> start) {
        Class<?> clazz = start;

        final Set<Class<?>> ret = new HashSet<>();

        while (clazz != null) {
            ret.add(clazz);
            for (final Class<?> inf : clazz.getInterfaces()) {
                if (ret.contains(inf)) {
                    continue;
                }

                ret.add(inf);
                ret.addAll(allTypesIn(inf));
            }

            clazz = clazz.getSuperclass();
        }

        return ret;
    }

    static <T extends WithId<T>> List<AuditHandler<T>> determineHandlersFor(final Class<T> clazz) {
        final List<AuditHandler<T>> handlers = new ArrayList<>();

        for (final Class<?> type : allTypesIn(clazz)) {
            // This is not correct & true, AuditHandler will not be
            // AuditHandler<T>, but of the `inf` type, generic type information
            // is erased by the compiler so it ends up working
            @SuppressWarnings("unchecked")
            final AuditHandler<T> handler = (AuditHandler<T>) HANDLERS.get(type);
            if (handler != null) {
                handlers.add(handler);
            }

        }

        return handlers;
    }

    static <T extends WithId<T>> boolean shouldAudit(final T object) {
        final Class<?> clazz = object.getClass();

        if (clazz.isAnnotationPresent(Audited.class)) {
            return true;
        }

        for (final Class<?> type : allTypesIn(clazz)) {
            if (type.isAnnotationPresent(Audited.class)) {
                return true;
            }
        }

        return false;
    }

    private static <T extends WithId<T>> String determineName(final T created) {
        if (created instanceof WithName) {
            return ((WithName) created).getName();
        }

        return "*";
    }

    private static <T extends WithId<T>> String modelName(final T created) {
        return created.getKind().getModelName();
    }
}
