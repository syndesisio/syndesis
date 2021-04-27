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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class AuditEvent {

    static final int MAX_LENGTH = 30;

    @JsonProperty(index = 4)
    private final String current;

    private int hashCode = Integer.MIN_VALUE;

    @JsonProperty(index = 3)
    private final String previous;

    @JsonProperty(index = 2)
    private final String property;

    @JsonProperty(index = 1)
    private final String type;

    private AuditEvent(final String type, final String property, final String value) {
        this(type, property, null, value);
    }

    private AuditEvent(final String type, final String property, final String previous, final String current) {
        this.type = type;
        this.property = property;
        this.previous = abbreviate(previous);
        this.current = abbreviate(current);
    }

    public String current() {
        return current;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuditEvent)) {
            return false;
        }
        final AuditEvent that = (AuditEvent) o;
        return hashCode() == that.hashCode() &&
            Objects.equals(type, that.type) &&
            Objects.equals(property, that.property) &&
            Objects.equals(previous, that.previous) &&
            Objects.equals(current, that.current);
    }

    @Override
    public int hashCode() {
        if (hashCode == Integer.MIN_VALUE) {
            hashCode = calculateHashCode();
        }

        return hashCode;
    }

    public String previous() {
        return previous;
    }

    public String property() {
        return property;
    }

    @Override
    public String toString() {
        return "AuditEvent{" +
            "type='" + type + '\'' +
            ", property='" + property + '\'' +
            ", previous='" + previous + '\'' +
            ", current='" + current + '\'' +
            '}';
    }

    public String type() {
        return type;
    }

    public AuditEvent withProperty(final String newProperty) {
        return new AuditEvent(type, newProperty, previous, current);
    }

    private int calculateHashCode() {
        return Objects.hash(type, property, previous, current);
    }

    public static AuditEvent propertyChanged(final String propertyName, final String previous, final String current) {
        return new AuditEvent("change", propertyName, previous, current);
    }

    public static AuditEvent propertySet(final String propertyName, final String value) {
        return new AuditEvent("set", propertyName, value);
    }

    static String abbreviate(final String value) {
        if (value == null) {
            return null;
        }

        if (value.length() > MAX_LENGTH) {
            return value.substring(0, MAX_LENGTH - 3) + "...";
        }

        return value;
    }
}
