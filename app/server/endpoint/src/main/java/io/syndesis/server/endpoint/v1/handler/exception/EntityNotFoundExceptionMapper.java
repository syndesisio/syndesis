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
package io.syndesis.server.endpoint.v1.handler.exception;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.springframework.stereotype.Component;

@Component
@Provider
public class EntityNotFoundExceptionMapper extends BaseExceptionMapper<EntityNotFoundException> {

    public EntityNotFoundExceptionMapper() {
        super(Response.Status.NOT_FOUND, "Please check your request data");
    }

    @Override
    protected String developerMessage(final EntityNotFoundException exception) {
        return "Entity Not Found Exception " + exception.getMessage();
    }

}
