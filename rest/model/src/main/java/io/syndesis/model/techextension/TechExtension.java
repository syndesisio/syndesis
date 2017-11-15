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
package io.syndesis.model.techextension;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.model.Kind;
import io.syndesis.model.WithId;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = TechExtension.Builder.class)
public interface TechExtension extends WithId<TechExtension>, Serializable {

    @Override
    default Kind getKind() {
        return Kind.TechExtension;
    }

    // id field comes from the superclass

    String getExtensionId();
    String getVersion();
    String getName();
    Optional<String> getStatus();
    Optional<String> getDescription();
    Optional<String> getIcon();

    @Value.Default
    default List<String>getTags() {
        return Collections.emptyList();
    }

    List<TechExtensionAction> getActions();

    @Value.Default
    default List<String> getDependencies() {
        return Collections.emptyList();
    }

    class Builder extends ImmutableTechExtension.Builder {
    }
}
