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
package io.syndesis.server.api.generator.openapi.util;

import java.util.stream.Stream;

import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20PathItem;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

public class OperationDescriptionGenerationTest {

    @ParameterizedTest
    @MethodSource("parameters")
    public void shouldDetermineOperationDescriptions(final Oas20Document openApiDoc, final Oas20Operation operation, final OperationDescription expected) {
        assertThat(OasModelHelper.operationDescriptionOf(openApiDoc, operation, (m, p) -> "Send " + m + " request to " + p)).isEqualTo(expected);
    }

    public static Stream<Arguments> parameters() {
        return Stream.of(
            argument(null, null, "GET /test", "Send GET request to /test"),
            argument("null", "null", "GET /test", "Send GET request to /test"),
            argument(null, "", "GET /test", "Send GET request to /test"),
            argument("null", "", "GET /test", "Send GET request to /test"),
            argument("", null, "GET /test", "Send GET request to /test"),
            argument("", "null", "GET /test", "Send GET request to /test"),
            argument("", "", "GET /test", "Send GET request to /test"),
            argument("Test summary", "Test description", "Test summary", "Test description"),
            argument("", "Test description", "GET /test", "Test description"),
            argument(null, "Test description", "GET /test", "Test description"),
            argument("null", "Test description", "GET /test", "Test description"),
            argument("Test summary", "", "Test summary", "Send GET request to /test"),
            argument("Test summary", null, "Test summary", "Send GET request to /test"),
            argument("Test summary", "null", "Test summary", "Send GET request to /test"));
    }

    static Arguments argument(final String summary, final String description, final String expectedName, final String expectedDescription) {
        final Oas20Operation operation = new Oas20Operation("get");
        operation.description = description;
        operation.summary = summary;

        final Oas20PathItem pathItem = new Oas20PathItem("/test");
        pathItem.get = operation;

        final Oas20Document openApiDoc = new Oas20Document();
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/test", pathItem);

        final OperationDescription expected = new OperationDescription(expectedName, expectedDescription);

        return Arguments.of(openApiDoc, operation, expected);
    }

}
