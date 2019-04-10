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
package io.syndesis.server.endpoint.v1.operations;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.syndesis.common.model.Violation;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.validation.AllValidations;

public interface Validating<T extends WithId<T>> extends Resource {

    Validator getValidator();

    @POST
    @Path(value = "/validation")
    @Consumes("application/json")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({//
        @ApiResponse(code = 204, message = "All validations pass"), //
        @ApiResponse(code = 400, message = "Found violations in validation", responseContainer = "Set",
            response = Violation.class)//
    })
    default Response validate(@NotNull final T obj) {
        final Set<ConstraintViolation<T>> constraintViolations = getValidator().validate(obj, AllValidations.class);

        if (constraintViolations.isEmpty()) {
            return Response.noContent().build();
        }

        throw new ConstraintViolationException(constraintViolations);
    }

}
