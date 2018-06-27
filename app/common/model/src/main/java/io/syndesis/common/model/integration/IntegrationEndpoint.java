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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.WithKind;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonDeserialize(builder = IntegrationEndpoint.Builder.class)
@SuppressWarnings("immutables")
public interface IntegrationEndpoint extends WithId<IntegrationEndpoint>, WithKind  {

    @Override
    default Kind getKind() {
        return Kind.IntegrationEndpoint;
    }

    Optional<String> getProtocol();

    Optional<String> getHost();

    Optional<String> getContextPath();

    default Optional<String> getUrl() {
        Optional<String> protocol = getProtocol();
        Optional<String> host = getHost();
        Optional<String> contextPath = getContextPath();
        if (protocol.isPresent() && host.isPresent()) {
            return Optional.of(protocol.get() + "://" + host.get() + contextPath.orElse(""));
        }
        return Optional.empty();
    }

    class Builder extends ImmutableIntegrationEndpoint.Builder {
        // allow access to ImmutableIntegrationEndpoint.Builder
    }

    default Builder builder() {
        return new Builder().createFrom(this);
    }
}
