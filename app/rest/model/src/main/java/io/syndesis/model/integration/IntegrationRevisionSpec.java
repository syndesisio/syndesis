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
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.model.connection.Connection;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = IntegrationRevisionSpec.Builder.class)
public interface IntegrationRevisionSpec {

    Optional<String> getConfiguration();

    @Value.Default
    default List<Connection> getConnections() {
        return Collections.emptyList();
    }

    @Value.Default
    default List<Step> getSteps() {
        return Collections.emptyList();
    }

    class Builder extends ImmutableIntegrationRevisionSpec.Builder {
        // allow access to ImmutableIntegrationRevisionSpec.Builder
    }

}
