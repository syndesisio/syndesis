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
package io.syndesis.model.integration;

import java.io.Serializable;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.model.Kind;
import io.syndesis.model.WithConfiguredProperties;
import io.syndesis.model.WithDependencies;
import io.syndesis.model.WithId;
import io.syndesis.model.action.Action;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.extension.Extension;

@JsonDeserialize(using = StepDeserializer.class)
@JsonIgnoreProperties(value = {"dependencies"}, allowGetters = true)
public interface Step extends WithId<Step>, WithConfiguredProperties, WithDependencies, Serializable {

    @Override
    default Kind getKind() {
        return Kind.Step;
    }

    Optional<Action<?>> getAction();

    Optional<Connection> getConnection();

    Optional<Extension> getExtension();

    String getStepKind();

    String getName();

    class Builder extends ImmutableIntegration.Builder {
        // allow access to ImmutableIntegration.Builder
    }
}
