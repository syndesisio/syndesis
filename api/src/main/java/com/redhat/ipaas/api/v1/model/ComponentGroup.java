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
package com.redhat.ipaas.api.v1.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.io.Serializable;

/**
 * ComponentGroups are labels in Camel.
 *
 * https://github.com/apache/camel/blob/master/platforms/catalog/src/main/java/org/apache/camel/catalog/CamelCatalog.java#L233
 *
 * @author kstam
 *
 */
@Value.Immutable
@JsonDeserialize(builder = ComponentGroup.Builder.class)
public interface ComponentGroup extends WithId<ComponentGroup>, WithName, Serializable {

    String KIND = "componentgroup";

    @Override
    default String kind() {
        return KIND;
    }

    @Override
    default ComponentGroup withId(String id) {
        return new Builder().createFrom(this).id(id).build();
    }

    class Builder extends ImmutableComponentGroup.Builder {
    }

}
