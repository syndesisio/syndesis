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

import java.util.Objects;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Kafka extends CustomResource {

    private static final long serialVersionUID = 1L;

    private final Status status;

    @JsonCreator
    public Kafka(@JsonProperty("status") final Status status) {
        this.status = Objects.requireNonNull(status, "status");
    }

    @Override
    public boolean equals(final Object another) {
        if (!(another instanceof Kafka)) {
            return false;
        }

        final Kafka other = (Kafka) another;

        if (!Objects.equals(getKind(), other.getKind())) {
            return false;
        }

        final ObjectMeta otherMeta = other.getMetadata();
        final ObjectMeta thisMeta = getMetadata();

        if ((thisMeta == null && otherMeta != null) || (otherMeta == null && thisMeta != null)) {
            return false;
        }

        if (thisMeta != null && otherMeta != null) {
            if (!Objects.equals(thisMeta.getName(), otherMeta.getName())) {
                return false;
            }

            if (!Objects.equals(thisMeta.getNamespace(), otherMeta.getNamespace())) {
                return false;
            }
        }

        return Objects.equals(status, other.status);
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public int hashCode() {
        final ObjectMeta metadata = getMetadata();

        if (metadata == null) {
            return Objects.hash(status);
        }

        return Objects.hash(getKind(), metadata.getNamespace(), metadata.getName(), status);
    }

    @Override
    public String toString() {
        return super.toString() + ", status=" + status;
    }
}
