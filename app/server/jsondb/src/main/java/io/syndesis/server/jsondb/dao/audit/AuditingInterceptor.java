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
package io.syndesis.server.jsondb.dao.audit;

import java.util.Optional;
import java.util.function.Supplier;

import io.syndesis.common.model.Kind;
import io.syndesis.common.model.WithId;
import io.syndesis.server.dao.audit.Auditing;
import io.syndesis.server.dao.audit.AuditingRecorder;
import io.syndesis.server.dao.audit.LoggingAuditingRecorder;
import io.syndesis.server.dao.manager.DataAccessObject;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Intercepts changes of the data model performed via the DAO layer and records
 * them as audit records.
 */
@Aspect
public final class AuditingInterceptor {

    private final Auditing auditing;

    private final AuditingRecorder recorder;

    private static final class Standin<T extends WithId<T>> implements WithId<T> {
        private final String id;
        private final Kind kind;

        private Standin(final String id, final Kind kind) {
            this.id = id;
            this.kind = kind;
        }

        @Override
        public Optional<String> getId() {
            return Optional.of(id);
        }

        @Override
        public Kind getKind() {
            return kind;
        }

        @Override
        public T withId(final String id) {
            return null;
        }
    }

    public AuditingInterceptor() {
        this(new Auditing(AuditingInterceptor::currentUsername),
            new LoggingAuditingRecorder());
    }

    public AuditingInterceptor(final Auditing auditing, final AuditingRecorder recorder) {
        this.auditing = auditing;
        this.recorder = recorder;
    }

    public AuditingInterceptor(final Supplier<Long> time, final AuditingRecorder auditingRecorder) {
        this(new Auditing(time, AuditingInterceptor::currentUsername),
            auditingRecorder);
    }

    @AfterReturning(pointcut = "execution(* io.syndesis.server.jsondb.dao.JsonDbDao.create(io.syndesis.common.model.WithId)) && args(given)")
    public <T extends WithId<T>> void created(final T given) {
        recordCreated(given);
    }

    @AfterReturning(pointcut = "execution(* io.syndesis.server.jsondb.dao.JsonDbDao.delete(java.lang.String)) && args(id) && this(that)")
    @SuppressWarnings("unchecked")
    public <T extends WithId<T>> void deleted(final DataAccessObject<T> that, final String id) {
        final Class<T> type = that.getType();

        final Kind kind = Kind.from(type);

        @SuppressWarnings("rawtypes")
        final WithId standin = new Standin<T>(id, kind);

        recordDeleted(standin);
    }

    @AfterReturning(pointcut = "execution(* io.syndesis.server.jsondb.dao.JsonDbDao.delete(io.syndesis.common.model.WithId)) && args(given)")
    public <T extends WithId<T>> void deleted(final T given) {
        recordDeleted(given);
    }

    @AfterReturning(pointcut = "execution(* io.syndesis.server.jsondb.dao.JsonDbDao.set(io.syndesis.common.model.WithId)) && args(given)")
    public <T extends WithId<T>> void set(final T given) {
        // set in JsonDB doesn't return a value, not to perform the lookup of
        // the old value first, we just record update with the current values
        recordUpdated(given);
    }

    @AfterReturning(pointcut = "execution(* io.syndesis.server.jsondb.dao.JsonDbDao.update(io.syndesis.common.model.WithId)) && args(given)",
        returning = "returned")
    public <T extends WithId<T>> void updated(final T given, final Object returned) {
        // return needs to be an Object, otherwise AspectJ won't match, could be
        // generic types, not sure, AuditingInterceptorTest fails with `T
        // returned`, not with `Object returned`
        @SuppressWarnings("unchecked")
        final T returnedAsT = (T) returned;

        recordUpdated(given, returnedAsT);
    }

    private <T extends WithId<T>> void recordCreated(final T given) {
        auditing.onCreate(given)
            .ifPresent(r -> recorder.record(r));
    }

    private <T extends WithId<T>> void recordDeleted(final T given) {
        auditing.onDelete(given)
            .ifPresent(r -> recorder.record(r));
    }

    private <T extends WithId<T>> void recordUpdated(final T given) {
        auditing.onUpdate(given)
            .ifPresent(r -> recorder.record(r));
    }

    private <T extends WithId<T>> void recordUpdated(final T given, final T returned) {
        auditing.onUpdate(returned, given)
            .ifPresent(r -> recorder.record(r));
    }

    private static String currentUsername() {
        final SecurityContext context = SecurityContextHolder.getContext();
        final Authentication authentication = context.getAuthentication();

        return authentication.getName();
    }
}
