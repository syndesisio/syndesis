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
package io.syndesis.common.model.integration;

import io.syndesis.common.model.Kind;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.validation.UniquenessRequired;
import io.syndesis.common.model.validation.integration.NoDuplicateIntegration;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Value.Immutable
@JsonDeserialize(builder = Integration.Builder.class)
@NoDuplicateIntegration(groups = UniquenessRequired.class)
@SuppressWarnings("immutables")
public interface Integration extends WithId<Integration>, IntegrationBase {

    @Override
    default Kind getKind() {
        return Kind.Integration;
    }

    class Builder extends ImmutableIntegration.Builder {
        // allow access to ImmutableIntegration.Builder
    }

    default Builder builder() {
        return new Builder().createFrom(this);
    }
}
