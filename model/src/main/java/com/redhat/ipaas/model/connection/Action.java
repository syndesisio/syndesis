/**
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
package com.redhat.ipaas.model.connection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.redhat.ipaas.model.Kind;
import com.redhat.ipaas.model.WithId;
import com.redhat.ipaas.model.WithName;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Map;

@Value.Immutable
@JsonDeserialize(builder = Action.Builder.class)
public interface Action extends WithId<Action>, WithName, Serializable {

    @Override
    default Kind getKind() {
        return Kind.Action;
    }

    String getConnectorId();

    Map<String, ComponentProperty> getProperties();

    String getDescription();

    String getCamelConnectorGAV();

    String getCamelConnectorPrefix();

    DataShape getInputDataShape();

    DataShape getOutputDataShape();

    @Override
    default Action withId(String id) {
        return new Builder().createFrom(this).id(id).build();
    }

    class Builder extends ImmutableAction.Builder {
    }

}
