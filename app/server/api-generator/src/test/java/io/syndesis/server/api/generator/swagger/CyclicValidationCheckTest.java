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

import java.io.IOException;

import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.RefParameter;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.RefProperty;
import io.syndesis.common.util.Resources;
import io.syndesis.common.util.openapi.OpenApiHelper;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CyclicValidationCheckTest {

    @Test
    public void shouldFindCyclicReferenceInOpenHabSwagger() throws IOException {
        final Swagger swagger = OpenApiHelper.parse(Resources.getResourceAsText("swagger/openhab.swagger.json"));

        assertThat(CyclicValidationCheck.hasCyclicReferences(swagger)).isTrue();
    }

    @Test
    public void shouldFindCyclicSelfReferencesInParameters() {
        final Swagger swagger = new Swagger()
            .path("/api", new Path()
                .post(new Operation()
                    .parameter(new RefParameter("#/definitions/A"))));

        swagger.addDefinition("A", new ModelImpl().type("object").property("cyclic", new RefProperty("#/definitions/A")));

        assertThat(CyclicValidationCheck.hasCyclicReferences(swagger)).isTrue();
    }

    @Test
    public void shouldFindCyclicSelfReferencesInResponses() {
        final Swagger swagger = new Swagger()
            .path("/api", new Path()
                .post(new Operation()
                    .response(200, new Response()
                        .responseSchema(new RefModel("#/definitions/A")))));

        swagger.addDefinition("A", new ModelImpl().type("object").property("cyclic", new RefProperty("#/definitions/A")));

        assertThat(CyclicValidationCheck.hasCyclicReferences(swagger)).isTrue();
    }

    @Test
    public void shouldFindMultipleStepCyclicReferencesInParameters() {
        final Swagger swagger = new Swagger()
            .path("/api", new Path()
                .post(new Operation()
                    .parameter(new RefParameter("#/definitions/A"))));

        for (int c = 'A'; c < 'Z'; c++) {
            final String current = String.valueOf((char) c);
            final String next = String.valueOf((char) (c + 1));
            swagger.addDefinition(current, new ModelImpl().type("object").property(next, new RefProperty("#/definitions/" + next)));
        }
        swagger.addDefinition("Z", new ModelImpl().type("object").property("a", new RefProperty("#/definitions/A")));

        assertThat(CyclicValidationCheck.hasCyclicReferences(swagger)).isTrue();
    }

    @Test
    public void shouldFindMultipleStepCyclicReferencesInResponses() {
        final Swagger swagger = new Swagger()
            .path("/api", new Path()
                .post(new Operation()
                    .response(200, new Response()
                        .responseSchema(new RefModel("#/definitions/A")))));

        for (int c = 'A'; c < 'Z'; c++) {
            final String current = String.valueOf((char) c);
            final String next = String.valueOf((char) (c + 1));
            swagger.addDefinition(current, new ModelImpl().type("object").property(next, new RefProperty("#/definitions/" + next)));
        }
        swagger.addDefinition("Z", new ModelImpl().type("object").property("a", new RefProperty("#/definitions/A")));

        assertThat(CyclicValidationCheck.hasCyclicReferences(swagger)).isTrue();
    }

    @Test
    public void shouldFindNoCyclicReferencesWhenThereAreNone() {
        final Swagger swagger = new Swagger()
            .path("/api", new Path()
                .post(new Operation()
                    .parameter(new RefParameter("#/definitions/Request"))
                    .response(200, new Response()
                        .responseSchema(new RefModel("#/definitions/Response")))));

        swagger.addDefinition("Request", new ModelImpl().type("object").property("reqval", new IntegerProperty()));
        swagger.addDefinition("Response", new ModelImpl().type("object").property("reqval", new IntegerProperty()));

        assertThat(CyclicValidationCheck.hasCyclicReferences(swagger)).isFalse();
    }

    @Test
    public void shouldFindThreeStepCyclicReferencesInParameters() {
        final Swagger swagger = new Swagger()
            .path("/api", new Path()
                .post(new Operation()
                    .parameter(new RefParameter("#/definitions/A"))));

        swagger.addDefinition("A", new ModelImpl().type("object").property("b", new RefProperty("#/definitions/B")));
        swagger.addDefinition("B", new ModelImpl().type("object").property("c", new RefProperty("#/definitions/C")));
        swagger.addDefinition("C", new ModelImpl().type("object").property("a", new RefProperty("#/definitions/A")));

        assertThat(CyclicValidationCheck.hasCyclicReferences(swagger)).isTrue();
    }

    @Test
    public void shouldFindTwoStepCyclicReferencesInParameters() {
        final Swagger swagger = new Swagger()
            .path("/api", new Path()
                .post(new Operation()
                    .parameter(new RefParameter("#/definitions/A"))));

        swagger.addDefinition("A", new ModelImpl().type("object").property("b", new RefProperty("#/definitions/B")));
        swagger.addDefinition("B", new ModelImpl().type("object").property("a", new RefProperty("#/definitions/A")));

        assertThat(CyclicValidationCheck.hasCyclicReferences(swagger)).isTrue();
    }

    @Test
    public void shouldFindTwoStepCyclicReferencesInResponses() {
        final Swagger swagger = new Swagger()
            .path("/api", new Path()
                .post(new Operation()
                    .response(200, new Response()
                        .responseSchema(new RefModel("#/definitions/A")))));

        swagger.addDefinition("A", new ModelImpl().type("object").property("b", new RefProperty("#/definitions/B")));
        swagger.addDefinition("B", new ModelImpl().type("object").property("a", new RefProperty("#/definitions/A")));

        assertThat(CyclicValidationCheck.hasCyclicReferences(swagger)).isTrue();
    }

    @Test
    public void shouldTolerateNullValue() {
        assertThat(CyclicValidationCheck.hasCyclicReferences(null)).isFalse();
    }

    @Test
    public void shouldTolerateTrivialValue() {
        assertThat(CyclicValidationCheck.hasCyclicReferences(new Swagger())).isFalse();
    }
}
