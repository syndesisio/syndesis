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

import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(builder = IntegrationStatus.Builder.class)
// Immutables generates code that fails these checks
@SuppressWarnings({ "ArrayEquals", "ArrayHashCode", "ArrayToString" })
public interface IntegrationStatus extends Serializable {
    //type IntegrationStatus struct {
    //    Phase            IntegrationPhase `json:"phase,omitempty"`
    //    Digest           string           `json:"digest,omitempty"`
    //    Image            string           `json:"image,omitempty"`
    //    Dependencies     []string         `json:"dependencies,omitempty"`
    //    Context          string           `json:"context,omitempty"`
    //    GeneratedSources []SourceSpec     `json:"generatedSources,omitempty"`
    //    Failure          *Failure         `json:"failure,omitempty"`
    //    CamelVersion     string           `json:"camelVersion,omitempty"`
    //    RuntimeVersion   string           `json:"runtimeVersion,omitempty"`

    @Nullable
    String getPhase();
    @Nullable
    String getDigest();
    @Nullable
    String getImage();
    List<String> getDependencies();
    @Nullable
    String getContext();
    List<SourceSpec> getGeneratedSources();
    @Nullable
    Failure getFailure();
    @Nullable
    String getCamelVersion();
    @Nullable
    String getRuntimeVersion();

    class Builder extends ImmutableIntegrationStatus.Builder {
    }
}

