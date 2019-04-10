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
package io.syndesis.server.api.generator.swagger.util;

import java.util.Arrays;

import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class SwaggerHelperOperationDescriptionGenerationTest {

    private final OperationDescription expected;

    private final Operation operation;

    private final Swagger swagger;

    public SwaggerHelperOperationDescriptionGenerationTest(final String operationSummary, final String operationDescription,
        final String expectedName, final String expectedDescription) {
        operation = new Operation().description(operationDescription).summary(operationSummary);
        swagger = new Swagger().path("/test", new Path().get(operation));
        expected = new OperationDescription(expectedName, expectedDescription);
    }

    @Test
    public void shouldDetermineOperationDescriptions() {
        assertThat(SwaggerHelper.operationDescriptionOf(swagger, operation, (m, p) -> "Send " + m + " request to " + p)).isEqualTo(expected);
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
