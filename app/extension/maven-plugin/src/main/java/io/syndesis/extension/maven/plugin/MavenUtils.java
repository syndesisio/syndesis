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
package io.syndesis.extension.maven.plugin;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.syndesis.common.model.action.ActionDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

final class MavenUtils {

    private MavenUtils() {
        // utility class
    }

    static Artifact artifactFromId(final String id) {
        return new DefaultArtifact(id);
    }

    static List<ActionDescriptor.ActionDescriptorStep> createPropertiesDefinitionSteps(final JsonNode root) {

        final ArrayNode properties = (ArrayNode) root.get("properties");
        if (properties != null) {
            final ActionDescriptor.ActionDescriptorStep.Builder actionBuilder = new ActionDescriptor.ActionDescriptorStep.Builder();
            actionBuilder.name("extension-properties");
            actionBuilder.description("extension-properties");

            for (final JsonNode node : properties) {
                final ConfigurationProperty.Builder confBuilder = new ConfigurationProperty.Builder();

                Optional.ofNullable(node.get("description")).ifPresent(n -> confBuilder.description(n.textValue()));
                Optional.ofNullable(node.get("displayName")).ifPresent(n -> confBuilder.displayName(n.textValue()));
                Optional.ofNullable(node.get("componentProperty"))
                    .ifPresent(n -> confBuilder.componentProperty(n.booleanValue()));
                Optional.ofNullable(node.get("defaultValue")).ifPresent(n -> confBuilder.defaultValue(n.textValue()));
                Optional.ofNullable(node.get("deprecated")).ifPresent(n -> confBuilder.deprecated(n.booleanValue()));
                Optional.ofNullable(node.get("group")).ifPresent(n -> confBuilder.group(n.textValue()));
                Optional.ofNullable(node.get("javaType")).ifPresent(n -> confBuilder.javaType(n.textValue()));
                Optional.ofNullable(node.get("kind")).ifPresent(n -> confBuilder.kind(n.textValue()));
                Optional.ofNullable(node.get("label")).ifPresent(n -> confBuilder.label(n.textValue()));
                Optional.ofNullable(node.get("required")).ifPresent(n -> confBuilder.required(n.booleanValue()));
                Optional.ofNullable(node.get("secret")).ifPresent(n -> confBuilder.secret(n.booleanValue()));
                Optional.ofNullable(node.get("raw")).ifPresent(n -> confBuilder.raw(n.booleanValue()));
                Optional.ofNullable(node.get("type")).ifPresent(n -> confBuilder.type(n.textValue()));

                final ArrayNode tagArray = (ArrayNode) node.get("tags");
                if (tagArray != null) {
                    for (final JsonNode tagNode : tagArray) {
                        confBuilder.addTag(tagNode.asText().trim());
                    }
                }

                final ArrayNode enumArray = (ArrayNode) node.get("enums");
                if (enumArray != null) {
                    for (final JsonNode enumNode : enumArray) {
                        if (enumNode.has("value") && enumNode.has("label")) {
                            confBuilder.addEnum(new ConfigurationProperty.PropertyValue.Builder()
                                .value(enumNode.get("value").textValue()).label(enumNode.get("label").textValue())
                                .build());
                        }
                    }
                }

                Optional.ofNullable(node.get("name"))
                    .ifPresent(n -> actionBuilder.putProperty(n.textValue(), confBuilder.build()));
            }

            return Collections.singletonList(actionBuilder.build());
        }

        return Collections.emptyList();
    }

    static String toGav(final Artifact artifact) {
        // <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>
        return artifact.getGroupId()
            + ":" + artifact.getArtifactId()
            + (artifact.getExtension() == null ? "" : ":" + artifact.getExtension())
            + (artifact.getClassifier() == null ? "" : ":" + artifact.getClassifier())
            + ":" + artifact.getVersion();
    }

    static String versionlessKey(final Artifact artifact) {
        return artifact.getGroupId() + ":" + artifact.getArtifactId() + ":"
            + Optional.ofNullable(artifact.getExtension()).orElse("jar") + ":"
            + Optional.ofNullable(artifact.getClassifier()).orElse("");
    }
}
