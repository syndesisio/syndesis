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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.model.Kind;
import io.syndesis.model.connection.Action;
import io.syndesis.model.connection.Connection;
import org.immutables.value.Value;

import java.util.Map;
import java.util.Optional;

@Value.Immutable
@JsonDeserialize(builder = SimpleStep.Builder.class)
public interface SimpleStep extends Step {

    @Override
    default Kind getKind() {
        return Kind.Step;
    }

    Optional<Action> getAction();

    Optional<Connection> getConnection();

    String getStepKind();

    Optional<Map<String, String>> getConfiguredProperties();

    String getName();

    @Override
    default SimpleStep withId(String id) {
        return new Builder().createFrom(this).id(id).build();
    }

    class Builder extends ImmutableSimpleStep.Builder {
    }

}
