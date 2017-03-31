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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.redhat.ipaas.model.Kind;
import com.redhat.ipaas.model.WithId;
import com.redhat.ipaas.model.WithName;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = Connector.Builder.class)
public interface Connector extends WithId<Connector>, WithName, Serializable {

    @Override
    default Kind getKind() {
        return Kind.Connector;
    }

    Optional<ConnectorGroup> getConnectorGroup();

    Optional<String> getConnectorGroupId();

    String getIcon();

    Map<String, ComponentProperty> getProperties();

    String getDescription();

    List<Action> getActions();

    @Override
    default Connector withId(String id) {
        return new Builder().createFrom(this).id(id).build();
    }

    /**
     * Filters the properties that the {@link Connector} considers sensitive / secret.
     * @param properties        The specified configuration.
     * @return                  A map with just the sensitive data.
     */
    default Map<String, String> filterSecrets(Map<String, String> properties) {
        return filterSecrets(properties, e -> e.getValue());
    }

    /**
     * Filters the properties that the {@link Connector} considers sensitive / secret.
     * @param properties        The specified configuration.
     * @param valueConverter    A {@link Function} that is applies to each {@link Map.Entry} of the configuration.
     * @return                  A map with just the sensitive data.
     */
    default Map<String, String> filterSecrets(Map<String, String> properties, Function<Map.Entry<String, String>, String> valueConverter) {
        return properties.entrySet()
            .stream()
            .filter(e -> this.getProperties() != null
                && this.getProperties().containsKey(e.getKey())
                && Boolean.TRUE.equals(this.getProperties().get(e.getKey()).getSecret()))
            .collect(Collectors.toMap(e -> e.getKey(), e -> valueConverter.apply(e)));
    }

    class Builder extends ImmutableConnector.Builder {
    }

}
