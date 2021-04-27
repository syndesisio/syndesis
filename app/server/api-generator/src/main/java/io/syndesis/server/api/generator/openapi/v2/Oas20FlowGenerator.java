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

package io.syndesis.server.api.generator.openapi.v2;

import java.util.Map;

import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.syndesis.server.api.generator.openapi.OpenApiFlowGenerator;

public class Oas20FlowGenerator extends OpenApiFlowGenerator<Oas20Document, Oas20Operation> {

    public Oas20FlowGenerator() {
        super(new UnifiedDataShapeGenerator());
    }

    @Override
    protected String getBasePath(Oas20Document openApiDoc) {
        return openApiDoc.basePath;
    }

    @Override
    protected Map<String, Oas20Operation> getOperationsMap(OasPathItem pathItem) {
        return Oas20ModelHelper.getOperationMap(pathItem);
    }
}
