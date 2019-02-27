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
package io.syndesis.common.model.extension;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.immutables.value.Value;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.WithDependencies;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.WithMetadata;
import io.syndesis.common.model.WithName;
import io.syndesis.common.model.WithProperties;
import io.syndesis.common.model.WithTags;
import io.syndesis.common.model.WithUsage;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.action.WithActions;
import io.syndesis.common.model.validation.AllValidations;
import io.syndesis.common.model.validation.NonBlockingValidations;
import io.syndesis.common.model.validation.extension.NoDuplicateExtension;
import io.syndesis.common.util.IndexedProperty;

@Value.Immutable
@JsonDeserialize(builder = Extension.Builder.class)
@NoDuplicateExtension(groups = NonBlockingValidations.class)
@JsonPropertyOrder({"schemaVersion", "name", "description", "icon", "extensionId", "version", "tags", "actions", "dependencies"})
@IndexedProperty.Multiple({
    @IndexedProperty("extensionId"),
    @IndexedProperty("status")
})
@SuppressWarnings("immutables")
public interface Extension extends WithId<Extension>, WithActions<Action>, WithName, WithTags, WithProperties, WithDependencies, WithMetadata, WithUsage {

    enum Status {
        Draft,
        Installed,
        Deleted
    }

    enum Type {
        Steps,
        Connectors,
        Libraries
    }

    @Override
    default Kind getKind() {
        return Kind.Extension;
    }

    /**
     * The artifact version of the extension (usually computed from the project.version maven property)
     */
    String getVersion();

    /**
     * A correlation id shared among all versions of the same extension.
     */
    String getExtensionId();

    /**
     * The public schema version used in the JAR file containing the extension.
     */
    @NotNull(groups = AllValidations.class)
    String getSchemaVersion();

    Optional<Status> getStatus();

    String getIcon();

    String getDescription();

    Optional<String> getUserId();

    Optional<Date> getLastUpdated();

    Optional<Date> getCreatedDate();

    @NotNull(groups = AllValidations.class)
    Type getExtensionType();

    @JsonIgnore
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    default List<StepAction> getStepActions() {
        return getActions(StepAction.class);
    }

    @JsonIgnore
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    default List<ConnectorAction> getConnectorActions() {
        return getActions(ConnectorAction.class);
    }

    @Override
    default Extension withId(String id) {
        return new Builder().createFrom(this).id(id).build();
    }

    class Builder extends ImmutableExtension.Builder {
    }
}
