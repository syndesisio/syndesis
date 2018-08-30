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

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import javax.validation.constraints.NotNull;

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
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.action.WithActions;
import io.syndesis.common.model.validation.AllValidations;
import io.syndesis.common.model.validation.NonBlockingValidations;
import io.syndesis.common.model.validation.extension.NoDuplicateExtension;
import io.syndesis.common.util.IndexedProperty;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = Extension.Builder.class)
@NoDuplicateExtension(groups = NonBlockingValidations.class)
@JsonPropertyOrder({"schemaVersion", "name", "description", "icon", "extensionId", "version", "tags", "actions", "dependencies"})
@IndexedProperty.Multiple({
    @IndexedProperty("extensionId"),
    @IndexedProperty("status")
})
@SuppressWarnings("immutables")
public interface Extension extends WithId<Extension>, WithActions<Action>, WithName, WithTags, WithProperties, WithDependencies, WithMetadata, Serializable {

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

    /**
     * Provides number of integrations using this connection
     * <p>
     * Note:
     * Excluded from {@link #hashCode()} and {@link #equals(Object)}
     *
     * @return count of integrations
     */
    @Value.Auxiliary
    OptionalInt getUses();

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

    /**
     * A weaker form of equality to {@link #equals(Object)}.
     * Compares a defining subset of properties to {code}another{code}'s
     * and in turn tests those properties for equivalence.
     *<p>
     * An equals test of a null field and an empty {@link Optional}
     * will return false whilst they are equivalent so this method will return true.
     * <p>
     * Items not tested include:
     * <ul>
     * <li>Version id -
     *        this id can be updated yet the rest of the object is still unchanged;
     * <li>Updated Date -
     *        an object can be modified then reverted yet the updated value will be different.
     * </ul>
     * <p>
     * Note
     * Method can result in 2 instances being equivalent even though some
     * properties are different. Thus, this should only be used in appropriate
     * situations.
     *
     * @param another a {@link Extension} to compare with
     * @return true if this is equivalent to {code}another{code}, false otherwise
     */
    @SuppressWarnings("PMD.NPathComplexity")
    default boolean equivalent(Extension another) {
        if (this == another) {
            return true;
        }

        if (another == null) {
            return false;
        }

        List<Action> myActions = getActions();
        if (myActions == null) {
            if (another.getActions() != null) {
                return false;
            }
        } else {
            for (Action myAction : myActions) {
                Action anotherAction = another.findActionById(myAction.getId().get()).orElse(null);
                if (! myAction.equivalent(anotherAction)) {
                    return false;
                }
            }
        }

        return Objects.equals(getExtensionId(), another.getExtensionId())
                        && Objects.equals(getSchemaVersion(), another.getSchemaVersion())
                        && Objects.equals(getStatus(), another.getStatus())
                        && Objects.equals(getIcon(), another.getIcon())
                        && Objects.equals(getDescription(), another.getDescription())
                        && Objects.equals(getUserId(), another.getUserId())
                        && Objects.equals(getExtensionType(), another.getExtensionType())
                        && Objects.equals(getId(), another.getId())
                        && Objects.equals(getName(), another.getName())
                        && getTags().equals(another.getTags())
                        && getProperties().equals(another.getProperties())
                        && getConfiguredProperties().equals(another.getConfiguredProperties())
                        && getDependencies().equals(another.getDependencies())
                        && getMetadata().equals(another.getMetadata());
    }
}
