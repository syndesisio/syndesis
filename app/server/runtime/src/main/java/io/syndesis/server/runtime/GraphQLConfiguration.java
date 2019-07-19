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

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static io.syndesis.common.util.IOStreams.readText;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.syndesis.common.model.integration.Step;
import io.syndesis.server.endpoint.v1.handler.integration.IntegrationHandler;

/**
 * Used to setup a GraphQL endpoint.
 */
@Configuration
//@ConditionalOnProperty("features.api.graphql.enabled")
public class GraphQLConfiguration {

    @Autowired
    IntegrationHandler integrationHandler;

    @Bean
    GraphQLSchema schema() throws IOException {

        String schema;
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema.graphqls") )  {
            schema = readText(is);
        }

        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, newRuntimeWiring()

            // Entities who's json shape matches that GraphQL schema,
            // just need a a bit of code like this to load the data for the query.
            .type("Query", builder -> builder
                .dataFetcher("integrations", env->{
                    return integrationHandler.list(null);
                })
            )

            // But if you a a field for specific GraphQL type which does not match
            // the syndesis data model exactly, you can do a little code section like this to
            // convert it to what you want.  You can also use this to lazy load additional data.
            .type("Step", builder -> builder

                // GraphQL does not support map/dictionary types.  Only objects with
                // know field names.  So covert this configuredProperties Map<String,String> type
                // to an array of objects. in other words:
                //    {"key1":"value1"} => [{"key": "key1", "value":"value1"}]
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
