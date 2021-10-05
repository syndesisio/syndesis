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
package io.syndesis.server.api.generator.openapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

import io.syndesis.common.model.connection.ConnectorTemplate;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import io.syndesis.common.util.json.JsonUtils;

final class ApiConnectorTemplate {

    static final ConnectorTemplate SWAGGER_TEMPLATE = fetchSwaggerConnectorTemplateFromDeployment();

    private ApiConnectorTemplate() {
        // needed for loading the template
    }

    private static ConnectorTemplate fetchSwaggerConnectorTemplateFromDeployment() {
        final Configuration configuration = Configuration.builder()//
            .jsonProvider(new JacksonJsonProvider(JsonUtils.copyObjectMapperConfiguration()))//
            .mappingProvider(new JacksonMappingProvider(JsonUtils.copyObjectMapperConfiguration()))//
            .build();

        try (InputStream deployment = ApiConnectorTemplate.class.getResourceAsStream("/io/syndesis/server/dao/deployment.json")) {
            final List<ConnectorTemplate> templates = JsonPath.using(configuration)
                .parse(deployment)
                .read("$..[?(@['id'] == 'swagger-connector-template')]", new TypeRef<List<ConnectorTemplate>>() {
                    // type token pattern
                });

            return templates.get(0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
