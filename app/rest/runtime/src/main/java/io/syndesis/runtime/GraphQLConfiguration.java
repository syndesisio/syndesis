/*
 * Copyright (C) 2018 Red Hat, Inc.
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
package io.syndesis.runtime;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.syndesis.model.integration.Step;
import io.syndesis.rest.v1.handler.integration.IntegrationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static io.syndesis.core.IO.readAllBytes;
import static io.syndesis.core.IO.utf8;

/**
 * Used to setup a GraphQL endpoint.
 */
@Configuration
@ConditionalOnProperty("features.api.graphql.enabled")
public class GraphQLConfiguration {

    @Autowired
    IntegrationHandler integrationHandler;

    @Bean
    GraphQLSchema schema() throws IOException {

        String schema;
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema.graphqls") )  {
            schema = utf8(readAllBytes(is));
        }

        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, newRuntimeWiring()
            .type("Query", builder -> builder
                .dataFetcher("integrations", env->{
                    return integrationHandler.list(null);
                })
            )
            .type("Step", builder -> builder
                .dataFetcher("configuredProperties", env->{
                    Step step = env.getSource();
                    return toEntries(step.getConfiguredProperties());
                })
            )
            .build());

    }

    private static Set<Map.Entry<String, String>> toEntries(Map<String, String> configuredProperties) {
        if( configuredProperties == null ) {
            return null;
        }
        return configuredProperties.entrySet();
    }
}
