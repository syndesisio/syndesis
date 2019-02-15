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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@Value.Immutable
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
//        Traits             map[string]IntegrationTraitSpec `json:"traits,omitempty"`
//        Configuration      []ConfigurationSpec             `json:"configuration,omitempty"`
//        Repositories       []string                        `json:"repositories,omitempty"`
//        ServiceAccountName string                          `json:"serviceAccountName,omitempty"`
    @Nullable
    Integer getReplicas();
    List<SourceSpec> getSources();
    List<ResourceSpec> getResources();
    @Nullable
    String getContext();
    List<String> getDependencies();
    @Nullable
    String getProfile();
    Map<String,IntegrationTraitSpec> getTraits();
    List<ConfigurationSpec> getConfiguration();
    List<String> getRepositories();
    @Nullable
    String getServiceAccountName();

    class Builder extends ImmutableIntegrationSpec.Builder {
    }
}
