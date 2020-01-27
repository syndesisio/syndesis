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

package io.syndesis.connector.kafka.model.crd;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Status implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Listener> listeners;

    @JsonCreator
    public Status(@JsonProperty("listeners") List<Listener> listeners) {
        this.listeners = Objects.requireNonNull(listeners, "listeners");
    }

    public List<Listener> getListeners() {
        return listeners;
    }

    @Override
    public String toString() {
        return listeners.toString();
    }

    @Override
    public boolean equals(Object another) {
        if (!(another instanceof Status)) {
            return false;
        }

        Status other = (Status) another;

        return Objects.equals(listeners, other.listeners);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(listeners);
    }
}
