/*
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

package io.syndesis.project.converter.visitor;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.connector.catalog.ConnectorCatalog;
import io.syndesis.integration.model.Flow;
import io.syndesis.project.converter.GenerateProjectRequest;
import io.syndesis.project.converter.ProjectGeneratorProperties;

import java.util.Map;


/**
 * We want this class to be mutable.
 * In particular we want to add stuff to `contents`. That's why we don't generate it using Immutables.
 */
@JsonDeserialize(builder = GeneratorContext.Builder.class)
public class GeneratorContext {

    private final GenerateProjectRequest request;
    private final Flow flow;
    private final Map<String, byte[]> contents;
    private final ConnectorCatalog connectorCatalog;
    private final ProjectGeneratorProperties generatorProperties;
    private final StepVisitorFactoryRegistry visitorFactoryRegistry;

    public static class Builder {

        private GenerateProjectRequest request;
        private Flow flow;
        private Map<String, byte[]> contents;
        private ConnectorCatalog connectorCatalog;
        private ProjectGeneratorProperties generatorProperties;
        private StepVisitorFactoryRegistry visitorFactoryRegistry;

        public Builder request(GenerateProjectRequest request) {
            this.request = request;
            return this;
        }

        public Builder flow(Flow flow) {
            this.flow = flow;
            return this;
        }

        public Builder contents(Map<String, byte[]> contents) {
            this.contents = contents;
            return this;
        }

        public Builder connectorCatalog(ConnectorCatalog connectorCatalog) {
            this.connectorCatalog = connectorCatalog;
            return this;
        }

        public Builder generatorProperties(ProjectGeneratorProperties projectGeneratorProperties) {
            this.generatorProperties = projectGeneratorProperties;
            return this;
        }

        public Builder visitorFactoryRegistry(StepVisitorFactoryRegistry visitorFactoryRegistry) {
            this.visitorFactoryRegistry = visitorFactoryRegistry;
            return this;
        }

        public GeneratorContext build() {
            return new GeneratorContext(request, flow, contents, connectorCatalog, generatorProperties, visitorFactoryRegistry);
        }
    }

    private GeneratorContext(GenerateProjectRequest request, Flow flow, Map<String, byte[]> contents, ConnectorCatalog connectorCatalog, ProjectGeneratorProperties generatorProperties, StepVisitorFactoryRegistry visitorFactoryRegistry) {
        this.request = request;
        this.flow = flow;
        this.contents = contents;
        this.connectorCatalog = connectorCatalog;
        this.generatorProperties = generatorProperties;
        this.visitorFactoryRegistry = visitorFactoryRegistry;
    }

    public GenerateProjectRequest getRequest() {
        return request;
    }

    public Flow getFlow() {
        return flow;
    }

    public Map<String, byte[]> getContents() {
        return contents;
    }

    public ConnectorCatalog getConnectorCatalog() {
        return connectorCatalog;
    }

    public ProjectGeneratorProperties getGeneratorProperties() {
        return generatorProperties;
    }

    public StepVisitorFactoryRegistry getVisitorFactoryRegistry() {
        return visitorFactoryRegistry;
    }
}
