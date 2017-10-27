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
package io.syndesis.model.extension;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.model.Kind;
import io.syndesis.model.WithConfigurationProperties;
import io.syndesis.model.WithId;
import io.syndesis.model.WithName;
import io.syndesis.model.WithTags;
import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
@JsonDeserialize(builder = Extension.Builder.class)
public interface Extension extends WithId<Extension>, WithName, WithTags, WithConfigurationProperties, Serializable {

    @Override
    default Kind getKind() {
        return Kind.Extension;
    }

    String getDescription();

    @Override
    default Extension withId(String id) {
        return new Builder().createFrom(this).id(id).build();
    }

    class Builder extends ImmutableExtension.Builder {
    }
}
