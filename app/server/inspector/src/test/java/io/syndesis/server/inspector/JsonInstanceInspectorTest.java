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

package io.syndesis.server.inspector;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Christoph Deppisch
 */
public class JsonInstanceInspectorTest {

    private final String JSON_INSTANCE_KIND = "json-instance";
    private JsonInstanceInspector inspector = new JsonInstanceInspector();

    @Test
    public void shouldCollectPathsFromJsonObject() {
        final List<String> paths = inspector.getPaths(JSON_INSTANCE_KIND, "", getJsonInstance(), Optional.empty());
        assertProperties(paths);
        assertThat(paths).doesNotContainAnyElementsOf(JsonInstanceInspector.COLLECTION_PATHS);
    }

    @Test
    public void shouldCollectPathsFromEmptyJsonObject() {
        final List<String> paths = inspector.getPaths(JSON_INSTANCE_KIND, "", "{}", Optional.empty());
        assertThat(paths).isEmpty();
    }

    @Test
    public void shouldCollectPathsFromJsonArray() {
        final List<String> paths = inspector.getPaths(JSON_INSTANCE_KIND, "", getJsonArrayInstance(), Optional.empty());
        assertArrayProperties(paths);
        assertThat(paths).containsAll(JsonInstanceInspector.COLLECTION_PATHS);
    }

    @Test
    public void shouldCollectPathsFromEmptyJsonArray() {
        final List<String> paths = inspector.getPaths(JSON_INSTANCE_KIND, "", "[]", Optional.empty());
        assertThat(paths).isEqualTo(JsonInstanceInspector.COLLECTION_PATHS);
    }

    private String getJsonInstance() {
        return "{" +
                    "\"id\": \"" + UUID.randomUUID().toString() + "\"," +
                    "\"firstName\": \"Foo\"," +
                    "\"lastName\": \"Bar\"," +
                    "\"isAlive\": true," +
                    "\"age\": 30," +
                    "\"address\": {" +
                        "\"streetAddress\": \"21 1st Fake Street\"," +
                        "\"city\": \"Fake Town\"," +
                        "\"state\": \"ZZ\"," +
                        "\"postalCode\": \"10000-0000\"" +
                    "}," +
                    "\"phoneNumbers\": [" +
                        "{" +
                            "\"type\": \"home\"," +
                            "\"number\": \"000 555-1234\"" +
                        "}," +
                        "{" +
                            "\"type\": \"office\"," +
                            "\"number\": \"000 555-4567\"" +
                        "}," +
                        "{" +
                            "\"type\": \"mobile\"," +
                            "\"number\": \"123 456-7890\"" +
                        "}" +
                    "]," +
                    "\"children\": []," +
                    "\"spouse\": null," +
                    "\"image\": {}" +
                "}";
    }

    private String getJsonArrayInstance() {
        return "[" + getJsonInstance() + "]";
    }

    private void assertProperties(List<String> paths) {
        assertProperties(paths, null);
    }

    private void assertArrayProperties(List<String> paths) {
        assertProperties(paths, "[]");
    }

    private void assertProperties(List<String> paths, String context) {
        List<String> expectedPaths = Arrays.asList("id", "isAlive",
                "firstName", "lastName", "address.city", "address.state",
                "phoneNumbers[].type", "phoneNumbers.size()", "phoneNumbers[].number",
                "children.size()", "children[]", "spouse", "image");

        assertThat(paths).containsAll(expectedPaths.stream()
                .map(item -> Optional.ofNullable(context).map(path -> path + ".").orElse("") + item)
                .collect(Collectors.toList()));
    }
}