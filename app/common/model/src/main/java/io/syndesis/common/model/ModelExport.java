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
package io.syndesis.common.model;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * A ModelExport is used to hold an list of models and information about their
 * version.
 */
@Value.Immutable
@JsonDeserialize(builder = ImmutableModelExport.Builder.class)
@SuppressWarnings("varargs")
public interface ModelExport {

    int schemaVersion();

    static ModelExport of(final int version) {
        return ImmutableModelExport.builder().schemaVersion(version).build();
    }

}
