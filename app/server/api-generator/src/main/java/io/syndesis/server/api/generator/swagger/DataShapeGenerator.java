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
package io.syndesis.server.api.generator.swagger;

import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface DataShapeGenerator {

    DataShape DATA_SHAPE_NONE = new DataShape.Builder().kind(DataShapeKinds.NONE).build();

    DataShape createShapeFromRequest(ObjectNode json, Swagger swagger, Operation operation);

    DataShape createShapeFromResponse(ObjectNode json, Swagger swagger, Operation operation);

}
