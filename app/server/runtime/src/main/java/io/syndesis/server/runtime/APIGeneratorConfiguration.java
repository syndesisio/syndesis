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
package io.syndesis.server.runtime;

import java.util.Objects;
import java.util.concurrent.Future;

import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.api.generator.APIGenerator;
import io.syndesis.server.api.generator.ConnectorGenerator;
import io.syndesis.server.api.generator.swagger.SwaggerAPIGenerator;
import io.syndesis.server.api.generator.swagger.SwaggerUnifiedShapeConnectorGenerator;
import io.syndesis.server.dao.manager.DataManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class APIGeneratorConfiguration {

    private final DataManager dataManager;

    private final Migrations migrations;

    public APIGeneratorConfiguration(final DataManager dataManager, final Migrations migrations) {
        this.dataManager = dataManager;
        this.migrations = migrations;
    }

    @Bean
    public APIGenerator apiGenerator() {
        return new SwaggerAPIGenerator();
    }

    @Bean("swagger-connector-template")
    public Future<ConnectorGenerator> swaggerConnectorGenerator() {
        return migrations.migrationsDone().thenApply(v -> {
            final Connector restSwaggerConnector = Objects.requireNonNull(dataManager.fetch(Connector.class, "rest-swagger"),
                "No Connector with ID `rest-swagger` in the database");

            return new SwaggerUnifiedShapeConnectorGenerator(restSwaggerConnector);
        });
    }
}
