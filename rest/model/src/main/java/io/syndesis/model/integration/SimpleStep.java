/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.model.integration;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.syndesis.core.immutable.SkipNulls;
import io.syndesis.model.Kind;
import io.syndesis.model.action.Action;
import io.syndesis.model.connection.Connection;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = SimpleStep.Builder.class)
public interface SimpleStep extends Step {

    @Override
    @Value.Default default Kind getKind() {
        return Kind.Step;
    }

    @Override
    Optional<Action> getAction();

    @Override
    Optional<Connection> getConnection();

    @Override
    String getStepKind();

    @Override
    @Value.Default
    @SkipNulls
    default Map<String, String> getConfiguredProperties() {
        return Collections.emptyMap();
    }

    @Override
    String getName();

    @Override
    default SimpleStep withId(String id) {
        return new Builder().createFrom(this).id(id).build();
    }

    class Builder extends ImmutableSimpleStep.Builder {
        // allow access to ImmutableSimpleStep.Builder
    }

}
