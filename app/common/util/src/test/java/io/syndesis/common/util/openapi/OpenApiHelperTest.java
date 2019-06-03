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
package io.syndesis.common.util.openapi;

import java.util.Collections;
import java.util.List;

import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.Property;
import io.swagger.util.Json;
import io.syndesis.common.util.openapi.OpenApiHelper.BaseIntegerProperty;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

public class OpenApiHelperTest {

    @Test
    public void shouldDeserializeSecurityRequirements() throws JSONException {
        final Swagger deserialized = OpenApiHelper.parse("{\"swagger\":\"2.0\",\"paths\":{\"/api\":{\"get\":{\"security\":[{\"secured\":[]}]}}}}");
        assertThat(deserialized.getPath("/api").getGet().getSecurity()).containsOnly(Collections.singletonMap("secured", Collections.emptyList()));
    }

    @Test
    public void shouldDeserializeSerializeWithoutLoosingEnumValues() throws JSONException {
        final String document = "{\"swagger\":\"2.0\",\"definitions\":{\"Test\":{\"type\":\"object\",\"properties\":{\"key\":{\"type\":\"integer\",\"enum\":[1,2,3]}}}}}}}}}";
        final Swagger parsed = OpenApiHelper.parse(document);

        final String serialized = OpenApiHelper.serialize(parsed);

        JSONAssert.assertEquals(document,
            serialized, JSONCompareMode.STRICT);
    }

    @Test
    public void shouldSerializeSecurityRequirements() throws JSONException {
        final Operation api = new Operation();
        api.addSecurity("secured", Collections.emptyList());
        final Swagger document = new Swagger().path("/api", new Path().get(api));

        final String serialized = OpenApiHelper.serialize(document);
        JSONAssert.assertEquals(
            "{\"swagger\":\"2.0\",\"paths\":{\"/api\":{\"get\":{\"security\":[{\"secured\":[]}]}}}}",
            serialized, JSONCompareMode.STRICT);
    }

    @Test
    public void shouldSerializeWithoutAddingResponseSchema() throws JSONException {
        final Swagger document = new Swagger().path("/api", new Path()
            .get(new Operation()
                .response(200, new Response()
                    .responseSchema(new ModelImpl()
                        .type("object")
                        .property("key", new IntegerProperty())))));

        final String serialized = OpenApiHelper.serialize(document);

        JSONAssert.assertEquals(
            "{\"swagger\":\"2.0\",\"paths\":{\"/api\":{\"get\":{\"responses\":{\"200\":{\"schema\":{\"type\":\"object\",\"properties\":{\"key\":{\"type\":\"integer\",\"format\":\"int32\"}}}}}}}}}",
            serialized, JSONCompareMode.STRICT);
    }

    @Test
    public void shouldSetupSwaggerJacksonPatches() {
        OpenApiHelper.mapper(); // setup
        final ObjectMapper mapper = Json.mapper();

        assertThat(mapper.findMixInClassFor(Property.class)).isEqualTo(BaseIntegerProperty.class);
        assertThat(mapper.configOverride(List.class).getInclude()).isEqualTo(JsonInclude.Value.construct(Include.NON_EMPTY, null));
    }

}
