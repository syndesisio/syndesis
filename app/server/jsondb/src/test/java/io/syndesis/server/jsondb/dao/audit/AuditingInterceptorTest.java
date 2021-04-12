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

import io.syndesis.common.model.connection.Connection;
import io.syndesis.server.dao.ConnectionDao;
import io.syndesis.server.dao.audit.AuditEvent;
import io.syndesis.server.dao.audit.AuditRecord;
import io.syndesis.server.dao.audit.AuditingRecorder;
import io.syndesis.server.jsondb.dao.JsonDbDao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static java.util.Collections.singletonList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

@ExtendWith(SpringExtension.class)
@WithMockUser(username = "testuser")
public class AuditingInterceptorTest {

    final static Connection previous = new Connection.Builder().name("previous").build();

    @Configuration
    @EnableAspectJAutoProxy
    public static class TestConfiguration {

        @Bean
        public AuditingInterceptor auditingInterceptor(final AuditingRecorder auditingRecorder) {
            return new AuditingInterceptor(() -> 123456789L, auditingRecorder);
        }

        @Bean
        public AuditingRecorder auditingRecorder() {
            return mock(AuditingRecorder.class);
        }

        @Bean
        public ConnectionDao dao() {
            return (ConnectionDao) mock(JsonDbDao.class, withSettings().extraInterfaces(ConnectionDao.class).defaultAnswer(invocation -> {
                final Class<?> returnType = invocation.getMethod().getReturnType();
                if (void.class.equals(returnType)) {
                    return null;
                }

                return previous;
            }));
        }
    }

    @BeforeEach
    public void resetMocks(@Autowired final ConnectionDao dao, @Autowired final AuditingRecorder auditingRecorder) {
        reset(dao, auditingRecorder);
    }

    @Test
    public void shouldAuditConnectionCreation(@Autowired final ConnectionDao dao, @Autowired final AuditingRecorder auditingRecorder) {
        final Connection fresh = new Connection.Builder().id("id").name("fresh").build();

        dao.create(fresh);

        verify(auditingRecorder)
            .record(new AuditRecord("id", "Connection", "fresh", 123456789L, "testuser", singletonList(AuditEvent.propertySet("name", "fresh"))));
    }

    @Test
    public void shouldAuditConnectionSet(@Autowired final ConnectionDao dao, @Autowired final AuditingRecorder auditingRecorder) {
        final Connection fresh = new Connection.Builder().id("id").name("fresh").build();

        dao.set(fresh);

        verify(auditingRecorder)
            .record(new AuditRecord("id", "Connection", "fresh", 123456789L, "testuser", singletonList(AuditEvent.propertySet("name", "fresh"))));
    }

    @Test
    public void shouldAuditConnectionUpdates(@Autowired final ConnectionDao dao, @Autowired final AuditingRecorder auditingRecorder) {
        final Connection current = new Connection.Builder().id("id").name("current").build();

        dao.update(current);

        verify(auditingRecorder)
            .record(new AuditRecord("id", "Connection", "current", 123456789L, "testuser",
                singletonList(AuditEvent.propertyChanged("name", "previous", "current"))));
    }
}
