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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Holds information on the changes between two records on the database.
 */
public final class AuditRecord {

    @JsonProperty(index = 6)
    private final List<AuditEvent> events;

    @JsonProperty(index = 3)
    private final String name;

    @JsonProperty(index = 4)
    private final Long timestamp;

    @JsonProperty(index = 2)
    private final String type;

    @JsonProperty(index = 5)
    private final String user;

    public AuditRecord(final String type, final String name, final Long timestamp, final String user, final List<AuditEvent> events) {
        this.type = type;
        this.name = name;
        this.timestamp = timestamp;
        this.user = user;
        this.events = events;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuditRecord)) {
            return false;
        }
        final AuditRecord that = (AuditRecord) o;
        return Objects.equals(type, that.type) &&
            Objects.equals(name, that.name) &&
            Objects.equals(timestamp, that.timestamp) &&
            Objects.equals(user, that.user) &&
            Objects.equals(events, that.events);
    }

    public List<AuditEvent> events() {
        return events;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, timestamp, user, events);
    }

    public String name() {
        return name;
    }

    public Long timestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "AuditRecord" +
            "type='" + type + '\'' +
            ", name='" + name + '\'' +
            ", timestamp='" + timestamp + '\'' +
            ", user='" + user + '\'' +
            ", events='" + events + '\'' +
            '}';
    }

    public String type() {
        return type;
    }

    public String user() {
        return user;
    }
}
