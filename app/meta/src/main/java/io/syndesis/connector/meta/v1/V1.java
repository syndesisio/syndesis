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
package io.syndesis.connector.meta.v1;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@ApplicationPath("/api/v1")
@Component
@Configuration
@SuppressWarnings("PMD.ShortClassName")
public class V1 extends Application {

    @Bean
    public OpenApiResource openApiResource() {
        return new OpenApiResource();
    }
}
