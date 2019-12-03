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

import java.util.Arrays;

import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20PathItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class OperationDescriptionGenerationTest {

    private final OperationDescription expected;

    private final Oas20Operation operation;

    private final Oas20Document openApiDoc;

    public OperationDescriptionGenerationTest(final String operationSummary, final String operationDescription,
                                              final String expectedName, final String expectedDescription) {
        operation = new Oas20Operation("get");
        operation.description = operationDescription;
        operation.summary = operationSummary;

        openApiDoc = new Oas20Document();
        Oas20PathItem pathItem = new Oas20PathItem("/test");
        pathItem.get = operation;
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/test", pathItem);
        expected = new OperationDescription(expectedName, expectedDescription);
    }

    @Test
    public void shouldDetermineOperationDescriptions() {
        assertThat(OasModelHelper.operationDescriptionOf(openApiDoc, operation, (m, p) -> "Send " + m + " request to " + p)).isEqualTo(expected);
    }

    @Parameters
    public static Iterable<Object[]> parameters() {
        return Arrays.<Object[]>asList(//
            new Object[] {null, null, "GET /test", "Send GET request to /test"}, //
            new Object[] {"null", "null", "GET /test", "Send GET request to /test"}, //
            new Object[] {null, "", "GET /test", "Send GET request to /test"}, //
            new Object[] {"null", "", "GET /test", "Send GET request to /test"}, //
            new Object[] {"", null, "GET /test", "Send GET request to /test"}, //
            new Object[] {"", "null", "GET /test", "Send GET request to /test"}, //
            new Object[] {"", "", "GET /test", "Send GET request to /test"}, //
            new Object[] {"Test summary", "Test description", "Test summary", "Test description"}, //
            new Object[] {"", "Test description", "GET /test", "Test description"}, //
            new Object[] {null, "Test description", "GET /test", "Test description"}, //
            new Object[] {"null", "Test description", "GET /test", "Test description"}, //
            new Object[] {"Test summary", "", "Test summary", "Send GET request to /test"}, //
            new Object[] {"Test summary", null, "Test summary", "Send GET request to /test"}, //
            new Object[] {"Test summary", "null", "Test summary", "Send GET request to /test"});
    }

}
