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
package io.syndesis.server.runtime.integration;

import java.io.IOException;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.Resources;

final class SwaggerHickups {

    private SwaggerHickups() {
        // utility class
    }

    static String reparse(final String path) throws IOException {
        final String source = Resources.getResourceAsText(path);
        final Swagger swagger = new SwaggerParser().parse(source);

        return Json.toString(swagger);
    }

}
