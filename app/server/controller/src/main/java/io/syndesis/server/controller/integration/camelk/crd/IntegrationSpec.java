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
package io.syndesis.server.controller.integration.camelk.crd;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(builder = IntegrationSpec.Builder.class)
// Immutables generates code that fails these checks
@SuppressWarnings({ "ArrayEquals", "ArrayHashCode", "ArrayToString" })
public interface IntegrationSpec {
//    type IntegrationSpec struct {
//        Replicas           *int32                          `json:"replicas,omitempty"`
//        Sources            []SourceSpec                    `json:"sources,omitempty"`
//        Resources          []ResourceSpec                  `json:"resources,omitempty"`
//        Context            string                          `json:"context,omitempty"`
//        Dependencies       []string                        `json:"dependencies,omitempty"`
//        Profile            TraitProfile                    `json:"profile,omitempty"`
//        Traits             map[string]TraitSpec            `json:"traits,omitempty"`
//        Configuration      []ConfigurationSpec             `json:"configuration,omitempty"`
//        Repositories       []string                        `json:"repositories,omitempty"`
//        ServiceAccountName string                          `json:"serviceAccountName,omitempty"`
    @Nullable
    Integer getReplicas();

    @Value.Default
    default List<SourceSpec> getSources() {
        return Collections.emptyList();
    }
    @Value.Default
    default List<ResourceSpec> getResources() {
        return Collections.emptyList();
    }

    @Nullable
    String getContext();

    @Value.Default
    default List<String> getDependencies() {
        return Collections.emptyList();
    }

    @Nullable
    String getProfile();

    @Value.Default
    default Map<String, TraitSpec> getTraits() {
        return Collections.emptyMap();
    }

    @Value.Default
    default List<ConfigurationSpec> getConfiguration() {
        return Collections.emptyList();
    }

    @Value.Default
    default List<String> getRepositories(){
        return Collections.emptyList();
    }

    @Nullable
    String getServiceAccountName();

    class Builder extends ImmutableIntegrationSpec.Builder {
         public Builder addConfiguration(String type, String value) {
             return addConfiguration(
                 new ConfigurationSpec.Builder()
                     .type(type)
                    .value(value)
                    .build()
             );
         }
    }
}
