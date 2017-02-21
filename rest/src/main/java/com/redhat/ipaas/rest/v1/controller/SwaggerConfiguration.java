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
package com.redhat.ipaas.rest.v1.controller;

import com.redhat.ipaas.rest.AllowedOrigins;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.Path;

@Configuration
public class SwaggerConfiguration {

    @AllowedOrigins("*")
    @Path("/swagger.{type:json|yaml}")
    public static class IPaasApiListingResource extends ApiListingResource {
    }

    @Bean
    public IPaasApiListingResource apiListingResource() {
        return new IPaasApiListingResource();
    }

    @Bean
    public SwaggerSerializers swaggerSerializers() {
        return new SwaggerSerializers();
    }

}
