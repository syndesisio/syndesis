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
package io.syndesis.integration.component.proxy;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.apache.camel.catalog.CamelCatalog;
import org.immutables.value.Value;

/**
 * Component model.
 */
@Value.Immutable
@Value.Style(builder = "new")
@JsonDeserialize(builder = ImmutableComponentDefinition.Builder.class)
@JsonIgnoreProperties({ "connectorProperties" })
public interface ComponentDefinition {

    @JsonProperty("component")
    Component getComponent();

    @Value.Default
    @JsonProperty("componentProperties")
    default Map<String, Property> getComponentProperties() {
        return Collections.emptyMap();
    }

    @Value.Default
    @JsonProperty("properties")
    default Map<String, Property> getEndpointProperties() {
        return Collections.emptyMap();
    }

    @Value.Immutable
    @Value.Style(builder = "new")
    @JsonDeserialize(builder = ImmutableComponent.Builder.class)
    interface Component {
        String getKind();
        String getScheme();
        String getSyntax();
        String getTitle();
        String getDescription();
        String getLabel();
        String getDeprecated();
        String getConsumerOnly();
        String getProducerOnly();
        String getJavaType();
        String getGroupId();
        String getArtifactId();
        String getVersion();
        Optional<String> getAlternativeSyntax();
        Optional<String> getAlternativeSchemes();
        Optional<String> getFirstVersion();
    }

    @Value.Immutable
    @Value.Style(builder = "new")
    @JsonDeserialize(builder = ImmutableWhen.Builder.class)
    interface When {
        String getId();
        String getValue();
    }

    @Value.Immutable
    @Value.Style(builder = "new")
    @JsonDeserialize(builder = ImmutablePropertyRelation.Builder.class)
    interface PropertyRelation {
        String getAction();
        List<When> getWhen();
    }

    @Value.Immutable
    @Value.Style(builder = "new")
    @JsonDeserialize(builder = ImmutableProperty.Builder.class)
    interface Property {
        Optional<String> getDisplayName();
        Optional<String> getKind();
        Optional<String> getGroup();
        Optional<String> getRequired();
        Optional<String> getType();
        Optional<String> getJavaType();
        Optional<String> getDeprecated();
        Optional<String> getSecret();
        Optional<String> getDescription();
        Optional<String> getControlHint();
        Optional<String> getLabelHint();
        Optional<String> getPlaceholder();
        Optional<String> getName();
        Optional<String> getDefaultValue();
        Optional<String> getEnums();
        Optional<String> getPrefix();
        Optional<String> getMultiValue();
        Optional<String> getEnumValues();
    }

    @JsonIgnore
    static ComponentDefinition forScheme(CamelCatalog catalog, String scheme) throws IOException {
        final String json = Optional.ofNullable(catalog.componentJSonSchema(scheme))
                                    .orElseThrow(() -> new IllegalArgumentException(String.format("Failed to find component definition for scheme '%s'." +
                                            " Missing component definition in classpath '%s/%s.json'", scheme, catalog.getRuntimeProvider().getComponentJSonSchemaDirectory(), scheme)));

        return new ObjectMapper()
            .registerModule(new Jdk8Module())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .readValue(json, ComponentDefinition.class);
    }
}
