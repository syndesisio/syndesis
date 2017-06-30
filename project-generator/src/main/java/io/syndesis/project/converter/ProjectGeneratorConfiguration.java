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
package io.syndesis.project.converter;

import io.syndesis.connector.catalog.ConnectorCatalog;

import io.syndesis.project.converter.visitor.DataMapperStepVisitor;
import io.syndesis.project.converter.visitor.EndpointStepVisitor;
import io.syndesis.project.converter.visitor.StepVisitorFactory;
import io.syndesis.project.converter.visitor.StepVisitorFactoryRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(ProjectGeneratorProperties.class)
public class ProjectGeneratorConfiguration {

    @Bean
    public ProjectGenerator projectConverter(ConnectorCatalog connectorCatalog, ProjectGeneratorProperties properties, StepVisitorFactoryRegistry registry) {
        return new DefaultProjectGenerator(connectorCatalog, properties, registry);
    }

    @Bean
    public StepVisitorFactoryRegistry registry(List<StepVisitorFactory> factories) {
        return new StepVisitorFactoryRegistry(factories);
    }

    @Bean
    public StepVisitorFactory<EndpointStepVisitor> endpointStepVisitorStepVisitorFactory() {
        return new EndpointStepVisitor.Factory();
    }

    @Bean
    public StepVisitorFactory<DataMapperStepVisitor> dataMapperStepVisitorStepVisitorFactory() {
        return new DataMapperStepVisitor.Factory();
    }
}
