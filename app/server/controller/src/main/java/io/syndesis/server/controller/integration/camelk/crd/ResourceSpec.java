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

@Value.Immutable
@JsonDeserialize(builder = ResourceSpec.Builder.class)
// Immutables generates code that fails these checks
@SuppressWarnings({ "ArrayEquals", "ArrayHashCode", "ArrayToString" })
public interface ResourceSpec {
//    type ResourceSpec struct {
//        DataSpec
//        Type ResourceType `json:"type,omitempty"`
//    }
// type DataSpec struct {
//        Name        string `json:"name,omitempty"`
//        Content     string `json:"content,omitempty"`
//        ContentRef  string `json:"contentRef,omitempty"`
//        Compression bool   `json:"compression,omitempty"`
//    }
    @Nullable
    String getName();
    @Nullable
    String getContent();
    @Nullable
    String getContentRef();
    @Nullable
    Boolean getCompression();
    @Nullable
    String getType();

    class Builder extends ImmutableResourceSpec.Builder {
    }
}
