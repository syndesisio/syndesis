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
package io.syndesis.common.model.openapi;

import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.WithMetadata;
import io.syndesis.common.model.WithName;
import io.syndesis.common.model.WithVersion;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = OpenApi.Builder.class)
@SuppressWarnings("immutables")
public interface OpenApi extends WithId<OpenApi>, WithName, WithVersion, WithMetadata, Serializable {

    String OPERATION_ID = "openapi-operationid";

    @Override
    default Kind getKind() {
        return Kind.OpenApi;
    }

    /**
     * The OpenAPI document.
     */
    byte[] getDocument();

    class Builder extends ImmutableOpenApi.Builder {
        // allow access to ImmutableOpenApi.Builder
    }
}
