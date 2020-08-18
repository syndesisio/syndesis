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
package io.syndesis.server.openshift.crd;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.Affinity;
import io.fabric8.kubernetes.api.model.Toleration;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class IntegrationScheduling implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Affinity affinity;
    private final List<Toleration> tolerations;

    public IntegrationScheduling(@JsonProperty("affinity")Affinity affinity, @JsonProperty("tolerations") List<Toleration> tolerations) {
        this.affinity = affinity;
        this.tolerations = tolerations;
    }

    public Affinity getAffinity() {
        return affinity;
    }

    public List<Toleration> getTolerations() {
        return tolerations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IntegrationScheduling that = (IntegrationScheduling) o;
        return affinity.equals(that.affinity) &&
            tolerations.equals(that.tolerations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(affinity, tolerations);
    }

    @Override
    public String toString() {
        return "IntegrationScheduling{" +
            "affinity=" + affinity +
            ", tolerations=" + tolerations +
            '}';
    }
}
