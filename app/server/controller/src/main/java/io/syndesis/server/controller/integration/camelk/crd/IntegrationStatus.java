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

    List<ConditionSpec> getConditions();
    List<String> getDependencies();
    @Nullable
    String getDigest();
    List<GeneratedResourceSpec> getGeneratedResources();
    @Nullable
    String getKit();
    @Nullable
    String getPhase();
    @Nullable
    String getPlatform();
    @Nullable
    String getProfile();
    @Nullable
    String getRuntimeProvider();
    @Nullable
    String getRuntimeVersion();
    @Nullable
    String getVersion();
    @Nullable
    String getImage();
    @Nullable
    Integer getReplicas();

    class Builder extends ImmutableIntegrationStatus.Builder {
        // make ImmutableIntegrationStatus.Builder which is package private
        // accessible
    }
}
