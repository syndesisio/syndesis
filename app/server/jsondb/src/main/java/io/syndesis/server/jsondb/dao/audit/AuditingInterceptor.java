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

import java.util.function.Supplier;

import io.syndesis.common.model.connection.Connection;
import io.syndesis.server.dao.audit.Auditing;
import io.syndesis.server.dao.audit.AuditingRecorder;
import io.syndesis.server.dao.audit.LoggingAuditingRecorder;

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

    @AfterReturning(pointcut = "execution(void io.syndesis.server.jsondb.dao.JsonDbDao.set(io.syndesis.common.model.WithId)) && args(given)")
    public void set(final Connection given) {
        record(given);
    }

    @AfterReturning(pointcut = "execution(* io.syndesis.server.jsondb.dao.JsonDbDao.update(io.syndesis.common.model.WithId)) && args(given)",
        returning = "returned")
    public void updated(final Connection given, final Connection returned) {
        record(given, returned);
    }

    private void record(final Connection given) {
        auditing.create(given)
            .ifPresent(r -> recorder.record(r));
    }

    private void record(final Connection given, final Connection returned) {
        auditing.create(returned, given)
            .ifPresent(r -> recorder.record(r));
    }

    private static String currentUsername() {
        final SecurityContext context = SecurityContextHolder.getContext();
        final Authentication authentication = context.getAuthentication();

        return authentication.getName();
    }
}
