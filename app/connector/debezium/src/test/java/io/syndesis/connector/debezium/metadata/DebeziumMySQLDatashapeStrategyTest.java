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
package io.syndesis.connector.debezium.metadata;

import java.util.Arrays;

import org.junit.Test;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

public class DebeziumMySQLDatashapeStrategyTest {

    @Test
    public void shouldBuildJsonSchema() {
        final String generated = DebeziumMySQLDatashapeStrategy.buildJsonSchema("table",
            Arrays.asList("\"prop1\":{\"type\":\"string\"}", "\"prop2\":{\"type\":\"string\"}", "\"prop3\":{\"type\":\"string\"}"));

        final String expected = "{\n" +
            "  \"$schema\":\"http://json-schema.org/draft-07/schema#\",\n" +
            "  \"title\":\"table\",\n" +
            "  \"type\":\"object\",\n" +
            "  \"properties\":{\n" +
            "    \"prop1\":{\n" +
            "      \"type\":\"string\"\n" +
            "    },\n" +
            "    \"prop2\":{\n" +
            "      \"type\":\"string\"\n" +
            "    },\n" +
            "    \"prop3\":{\n" +
            "      \"type\":\"string\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

        assertThatJson(generated).isEqualTo(expected);
    }

    @Test
    public void shouldConvertDDLtoJsonSchema() {
        final String ddl = "CREATE TABLE `roles` (\n" +
            "`id` varchar(32) NOT NULL,\n" +
            "`name` varchar(100) NOT NULL,\n" +
            "`context` varchar(20) NOT NULL,\n" +
            "`organization_id` int(11) DEFAULT NULL,\n" +
            "`client_id` varchar(32) NOT NULL,\n" +
            "`scope_action_ids` text NOT NULL,\n" +
            "PRIMARY KEY (`id`),\n" +
            "FULLTEXT KEY `scope_action_ids_idx` (`scope_action_ids`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";

        final String generated = DebeziumMySQLDatashapeStrategy.convertDDLtoJsonSchema(ddl, "roles");

        final String expected = "{\n" +
            "  \"$schema\":\"http://json-schema.org/draft-07/schema#\",\n" +
            "  \"title\":\"roles\",\n" +
            "  \"type\":\"object\",\n" +
            "  \"properties\":{\n" +
            "    \"id\":{\n" +
            "      \"type\":\"string\"\n" +
            "    },\n" +
            "    \"name\":{\n" +
            "      \"type\":\"string\"\n" +
            "    },\n" +
            "    \"context\":{\n" +
            "      \"type\":\"string\"\n" +
            "    },\n" +
            "    \"organization_id\":{\n" +
            "      \"type\":\"integer\"\n" +
            "    },\n" +
            "    \"client_id\":{\n" +
            "      \"type\":\"string\"\n" +
            "    },\n" +
            "    \"scope_action_ids\":{\n" +
            "      \"type\":\"string\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

        assertThatJson(generated).isEqualTo(expected);
    }
}
