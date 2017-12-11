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
package io.syndesis.model.extension;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.SortedSet;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.core.immutable.SkipNulls;
import io.syndesis.model.Kind;
import io.syndesis.model.WithConfigurationProperties;
import io.syndesis.model.WithId;
import io.syndesis.model.WithName;
import io.syndesis.model.WithTags;
import io.syndesis.model.action.ExtensionAction;
import io.syndesis.model.action.WithActions;
import io.syndesis.model.validation.NonBlockingValidations;
import io.syndesis.model.validation.extension.NoDuplicateExtension;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = Extension.Builder.class)
@NoDuplicateExtension(groups = NonBlockingValidations.class)
@JsonPropertyOrder({ "name", "description", "icon", "extensionId", "version", "tags", "actions", "dependencies"})
@SuppressWarnings("immutables")
public interface Extension extends WithId<Extension>, WithActions<ExtensionAction>, WithName, WithTags, WithConfigurationProperties, Serializable {

    enum Status {
        Draft,
        Installed,
        Deleted
    }

    @Override
    default Kind getKind() {
        return Kind.Extension;
    }

    String getVersion();

    String getExtensionId();

    Optional<Status> getStatus();

    String getIcon();

    String getDescription();

    @Value.NaturalOrder
    @SkipNulls
    SortedSet<String> getDependencies();

    OptionalInt getUses();

    Optional<String> getUserId();

    Optional<Date> getLastUpdated();

    Optional<Date> getCreatedDate();

    @Override
    default Extension withId(String id) {
        return new Builder().createFrom(this).id(id).build();
    }

    class Builder extends ImmutableExtension.Builder {
    }
}
