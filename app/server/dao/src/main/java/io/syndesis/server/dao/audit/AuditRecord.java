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

import java.util.List;
import java.util.Objects;

/**
 * Holds information on the diff change between two records on the database.
 */
public class AuditRecord {

    private String type;
    private String name;
    private Long timestamp;
    private String user;
    private List<AuditEvent> events;

    public AuditRecord(String type, String name, Long timestamp, String user, List<AuditEvent> events) {
        this.type = type;
        this.name = name;
        this.timestamp = timestamp;
        this.user = user;
        this.events = events;
    }

    public String type() {
        return type;
    }

    public String name() {
        return name;
    }

    public Long timestamp() {
        return timestamp;
    }

    public String user() {
        return user;
    }

    public List<AuditEvent> events() {
        return events;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuditRecord)) {
            return false;
        }
        AuditRecord that = (AuditRecord) o;
        return Objects.equals(type, that.type) &&
                   Objects.equals(name, that.name) &&
                   Objects.equals(timestamp, that.timestamp) &&
                   Objects.equals(user, that.user) &&
                   Objects.equals(events, that.events);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, timestamp, user, events);
    }
}
