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
package com.redhat.ipaas.rest.v1.model.connection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.redhat.ipaas.rest.v1.model.WithId;
import com.redhat.ipaas.rest.v1.model.WithName;
import org.immutables.value.Value;

import java.io.Serializable;

/**
 * ConnectorGroups are labels in Camel.
 *
 * @author kstam
 *
 */
@Value.Immutable
@JsonDeserialize(builder = ConnectorGroup.Builder.class)
public interface ConnectorGroup extends WithId<ConnectorGroup>, WithName, Serializable {

    String KIND = "connector-group";

    @Override
    default String getKind() {
        return KIND;
    }

    @Override
    default ConnectorGroup withId(String id) {
        return new Builder().createFrom(this).id(id).build();
    }

    class Builder extends ImmutableConnectorGroup.Builder {
    }

}
