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
package io.syndesis.server.runtime.audit;

import io.syndesis.server.jsondb.dao.audit.AuditingInterceptor;
import io.syndesis.server.runtime.AuditingConfiguration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

public class AuditingITCase {

    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = AuditingConfiguration.class)
    static class AuditingDisabledByDefault {
        @Test
        public void byDefaultAuditingShouldBeDisabled(@Autowired(required = false) AuditingInterceptor auditingInterceptor) {
            assertThat(auditingInterceptor).isNull();
        }
    }

    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = AuditingConfiguration.class)
    @TestPropertySource(properties = "features.auditing.enabled=true")
    static class AuditingCanBeEnabled {
        @Test
        public void byDefaultAuditingShouldBeDisabled(@Autowired AuditingInterceptor auditingInterceptor) {
            assertThat(auditingInterceptor).isNotNull();
        }
    }
}