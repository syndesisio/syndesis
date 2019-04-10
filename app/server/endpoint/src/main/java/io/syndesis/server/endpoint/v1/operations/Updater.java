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

import java.io.IOException;
import java.util.Set;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.PATCH;
import io.syndesis.common.util.Json;
import io.syndesis.server.dao.manager.WithDataManager;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.validation.AllValidations;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;

public interface Updater<T extends WithId<T>> extends Resource, WithDataManager {

    @PUT
    @Path(value = "/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    default void update(@NotNull @PathParam("id") @ApiParam(required = true) String id,
        @NotNull @Valid @ConvertGroup(from = Default.class, to = AllValidations.class) T obj) {
        getDataManager().update(obj);
    }

    @PATCH
    @Path(value = "/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    default void patch(@NotNull @PathParam("id") @ApiParam(required = true) String id, @NotNull JsonNode patchJson) throws IOException {
        Class<T> modelClass = resourceKind().getModelClass();
        final T existing = getDataManager().fetch(modelClass, id);
        if (existing == null) {
            throw new EntityNotFoundException();
        }

        JsonNode document = Json.reader().readTree(Json.writer().writeValueAsString(existing));

        // Attempt to apply the patch...
        final JsonMergePatch patch;
        try {
            patch = JsonMergePatch.fromJson(patchJson);
            document = patch.apply(document);
        } catch (JsonPatchException e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }

        // Convert the Json back to an entity.
        T obj = Json.reader().forType(modelClass).readValue(Json.writer().writeValueAsBytes(document));

        if (this instanceof Validating) {
            final Validator validator = ((Validating<?>) this).getValidator();
            final Set<ConstraintViolation<T>> violations = validator.validate(obj, AllValidations.class);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        }

        getDataManager().update(obj);
    }

}
