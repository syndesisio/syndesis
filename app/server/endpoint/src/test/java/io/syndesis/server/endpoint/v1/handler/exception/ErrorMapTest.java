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
package io.syndesis.server.endpoint.v1.handler.exception;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorMapTest {

    @Test
    public void testUnmarshalXML() {
        String rawMsg = read("/HttpClientErrorException.xml");
        assertThat(ErrorMap.from(rawMsg)).isEqualTo("Desktop applications only support the oauth_callback value 'oob'");
    }

    @Test
    public void testUnmarshalJSON() {
        String rawMsg = read("/HttpClientErrorException.json");
        assertThat(ErrorMap.from(rawMsg)).isEqualTo("Could not authenticate you.");
    }

    @Test
    public void testUnmarshalJSONVaryingFormats() {
        assertThat(ErrorMap.from("{\"error\": \"some error\"}")).isEqualTo("some error");
        assertThat(ErrorMap.from("{\"message\": \"some message\"}")).isEqualTo("some message");
    }

    @Test
    public void testUnmarshalImpossible() {
        String rawMsg = "This is just some other error format";
        assertThat(ErrorMap.from(rawMsg)).isEqualTo(rawMsg);
    }

    @Test
    public void shouldTryToLookupInJson() {
        final JsonNodeFactory factory = JsonNodeFactory.instance;
        final ObjectNode obj = factory.objectNode();
        obj.set("a", factory.arrayNode().add("b").add("c"));
        obj.set("x", factory.objectNode().set("y", factory.objectNode().put("z", "!")));

        assertThat(ErrorMap.tryLookingUp(obj, "a")).contains("b");
        assertThat(ErrorMap.tryLookingUp(obj, "a", "b")).isEmpty();
        assertThat(ErrorMap.tryLookingUp(obj, "x", "y")).contains("{\"z\":\"!\"}");
        assertThat(ErrorMap.tryLookingUp(obj, "x", "y", "z")).contains("!");
    }

    private static String read(final String path) {
        try {
            return String.join("", Files.readAllLines(Paths.get(ErrorMapTest.class.getResource(path).toURI())));
        } catch (IOException | URISyntaxException e) {
            throw new IllegalArgumentException("Unable to read from path: " + path, e);
        }
    }

}
