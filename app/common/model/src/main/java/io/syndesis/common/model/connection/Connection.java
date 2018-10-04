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
package io.syndesis.common.model.connection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.validation.UniqueProperty;
import io.syndesis.common.model.validation.UniquenessRequired;
import io.syndesis.common.util.IndexedProperty;
import org.immutables.value.Value;

/**
 * A connection is basically a Camel endpoint configuration (parameters) and
 * some metadata describing which parameters are available to configure.
 */
@Value.Immutable
@JsonDeserialize(builder = Connection.Builder.class)
@UniqueProperty(value = "name", groups = UniquenessRequired.class)
@SuppressWarnings("immutables")
@IndexedProperty("connectorId")
public interface Connection extends WithId<Connection>, ConnectionBase {

    @Override
    default Kind getKind() {
        return Kind.Connection;
    }

    class Builder extends ImmutableConnection.Builder {
    }

    default Builder builder() {
        return new Builder().createFrom(this);
    }
}
