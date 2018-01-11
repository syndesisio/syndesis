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
package io.syndesis.jsondb.impl;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.syndesis.jsondb.GetOptions;
import io.syndesis.jsondb.JsonDBException;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Unit Tests for the JsonDB implementation.
 */
public class JsonDBTest {

    private SqlJsonDB jsondb;
    private ObjectMapper mapper = new ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_ABSENT);

    private GetOptions prettyPrint = new GetOptions().prettyPrint(true);

    @Before
    public void before() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        DBI dbi = new DBI(ds);

        this.jsondb = new SqlJsonDB(dbi, null,
            Arrays.asList(new Index("/pair", "key"))
        );

        try {
            this.jsondb.dropTables();
        } catch (Exception e) {
        }
        this.jsondb.createTables();
    }

    @Test
    public void testInvalidKeys() throws IOException {

        for (String s : Arrays.asList("[", "]", ".", "%", "$", "#", "\n")) {
            try {
                jsondb.set("/test"+s, mapper.writeValueAsString(map(
                    "key", "Hiram Chirino"
                )));
                fail("Excpected JsonDBException");
            } catch (JsonDBException e) {
                assertThat(e.getMessage()).startsWith("Invalid key.");
            }
        }

        for (String s : Arrays.asList("[", "]", ".", "%", "$", "#", "/", "\n")) {
            try {
                jsondb.set("/test", mapper.writeValueAsString(map(
                    "bad"+s+"key", "Hiram Chirino"
                )));
                fail("Excpected JsonDBException");
            } catch (JsonDBException e) {
                assertThat(e.getMessage()).startsWith("Invalid key.");
            }
        }

    }

    @Test
    public void testUpdate() throws IOException {

        jsondb.set("/test", mapper.writeValueAsString(map(
            "name", "Hiram Chirino",
            "props", map(
                "city", "Tampa",
                "state", "FL"
            )
        )));

        // Verify that only the name fields change and the we can use
        // a path for the keys.
        jsondb.update("/test", mapper.writeValueAsString(map(
            "name", "Ana Chirino",
            "props/city", "Miami"
        )));

        String json = jsondb.getAsString("/test");
        assertThat(json).isEqualTo("{\"name\":\"Ana Chirino\",\"props\":{\"city\":\"Miami\",\"state\":\"FL\"}}");

        jsondb.update("/test", mapper.writeValueAsString(map(
            "props", null
        )));

        json = jsondb.getAsString("/test");
        assertThat(json).isEqualTo("{\"name\":\"Ana Chirino\",\"props\":null}");

    }

    @Test
    public void testGetMissingKey() throws IOException {

        jsondb.set("/test", mapper.writeValueAsString(map(
            "name", "Hiram Chirino"
        )));

        String json = jsondb.getAsString("/bar");
        assertThat(json).isNull();

    }


    @Test
    public void testGetDepth1() throws IOException {

        jsondb.set("/test", mapper.writeValueAsString(map(
            "name", "Hiram Chirino",
            "props", map(
                "city", "Tampa",
                "state", "FL"
            )
        )));

        String json = jsondb.getAsString("/test", new GetOptions().depth(1));
        assertThat(json).isEqualTo("{\"name\":\"Hiram Chirino\",\"props\":true}");
    }

    @Test
    public void testGetDepth2() throws IOException {

        jsondb.set("/test", mapper.writeValueAsString(map(
            "name", "Hiram Chirino",
            "props", map(
                "city", "Tampa",
                "state", "FL",
                "more-props", map(
                    "city", "Tampa",
                    "state", "FL"
                )
            )
        )));

        String json = jsondb.getAsString("/test", new GetOptions().depth(2));
        assertThat(json).isEqualTo("{\"name\":\"Hiram Chirino\",\"props\":{\"city\":\"Tampa\",\"state\":\"FL\",\"props\":true}}");
    }

    @Test
    public void testGetCallback() throws IOException {

        jsondb.set("/test", mapper.writeValueAsString(map(
            "name", "Hiram Chirino"
        )));

        String json = jsondb.getAsString("/", new GetOptions().callback("myfunction"));
        assertThat(json).isEqualTo("myfunction({\"test\":{\"name\":\"Hiram Chirino\"}})");

    }

    @Test
    public void testGetPrettyPrint() throws IOException {

        jsondb.set("/test", mapper.writeValueAsString(map(
            "name", "Hiram Chirino"
        )));

        // Check that the result is pretty printed
        String json = jsondb.getAsString("/", new GetOptions().prettyPrint(true));
        assertThat(json).isEqualTo("{\n" +
            "  \"test\" : {\n" +
            "    \"name\" : \"Hiram Chirino\"\n" +
            "  }\n" +
            "}");

        // Check that the result is not pretty printed
        json = jsondb.getAsString("/", new GetOptions().prettyPrint(false));
        assertThat(json).isEqualTo("{\"test\":{\"name\":\"Hiram Chirino\"}}");

        // We default to not pretty printing
        json = jsondb.getAsString("/");
        assertThat(json).isEqualTo("{\"test\":{\"name\":\"Hiram Chirino\"}}");
    }

    @Test
    public void testPush() throws IOException {

        jsondb.push("/test", mapper.writeValueAsString(map(
            "name", "Hiram Chirino"
        )));
        jsondb.push("/test", mapper.writeValueAsString(map(
            "name", "Ana Chirino"
        )));

        String json = jsondb.getAsString("/test", prettyPrint);
        @SuppressWarnings("unchecked")
        List<Map<?, ?>> items = new ArrayList<>(mapper.readValue(json, LinkedHashMap.class).values());
        assertThat(items).hasSize(2);
        assertThat((items.get(0)).get("name")).isEqualTo("Hiram Chirino");
        assertThat((items.get(1)).get("name")).isEqualTo("Ana Chirino");
    }

    @Test
    public void testArrayOfObject() throws IOException {
        Object[] original = new Object[]{map(
            "id", "foo"
        )};
        jsondb.set("/test", mapper.writeValueAsString(original));
        String result1 = jsondb.getAsString("", prettyPrint).trim();
        assertThat(result1).isEqualTo("{\n" +
            "  \"test\" : [ {\n" +
            "    \"id\" : \"foo\"\n" +
            "  } ]\n" +
            "}".trim());
    }

    @Test
    public void testDataTypes() throws IOException {

        HashMap<String, Object> user = map(
            "name", "Joe",
            "developer", false,
            "admin", true,
            "age", 25,
            "gpa", 3.52,
            "token", null
        );

        jsondb.set("/users/u1000", mapper.writeValueAsString(user));
        String result1 = jsondb.getAsString("", prettyPrint).trim();
        assertThat(result1).isEqualTo(load("result1.json").trim());

        String result2 = jsondb.getAsString("/", prettyPrint).trim();
        assertThat(result2).isEqualTo(result1);

        String result3 = jsondb.getAsString("/users/u1000", prettyPrint).trim();
        assertThat(result3).isEqualTo(load("result3.json").trim());

        assertThat(jsondb.getAsString("/users/u1000/name")).isEqualTo("\"Joe\"");
        assertThat(jsondb.getAsString("/users/u1000/developer")).isEqualTo("false");
        assertThat(jsondb.getAsString("/users/u1000/admin")).isEqualTo("true");
        assertThat(jsondb.getAsString("/users/u1000/age")).isEqualTo("25");
        assertThat(jsondb.getAsString("/users/u1000/gpa")).isEqualTo("3.52");
        assertThat(jsondb.getAsString("/users/u1000/token")).isEqualTo("null");
        assertThat(jsondb.getAsString("/users/u1000/error")).isNull();

    }

    @Test
    public void testSet() throws IOException {

        HashMap<String, Object> user = map(
            "name", "Joe",
            "developer", false,
            "admin", true,
            "age", 25,
            "gpa", 3.52,
            "token", null
        );

        jsondb.set("/", mapper.writeValueAsString(user));
        String result3 = jsondb.getAsString("", prettyPrint).trim();
        assertThat(result3).isEqualTo(load("result3.json").trim());

        // Verify that if we write over an existing /developer value, it get replaced
        // with an object
        jsondb.set("/developer/users/u1000", mapper.writeValueAsString(user));
        String result4 = jsondb.getAsString("", prettyPrint).trim();
        assertThat(result4).isEqualTo(load("result4.json").trim());


        // Writing a new object wipe the previous values at that path.
        HashMap<String, Object> user2 = map(
            "name", "Hiram",
            "city", "Tampa"
        );
        jsondb.set("/", mapper.writeValueAsString(user2));
        String result5 = jsondb.getAsString("", prettyPrint).trim();
        assertThat(result5).isEqualTo(load("result5.json").trim());

    }

    @Test
    public void testArrays() throws IOException {

        jsondb.set("/", mapper.writeValueAsString(new Object[]{"hi", 100}));
        assertThat(jsondb.getAsString("", prettyPrint).trim()).isEqualTo("[ \"hi\", 100 ]");

        HashMap<String, Object> user = map(
            "data", new Object[]{"hi", 100, "other"}
        );
        jsondb.set("/", mapper.writeValueAsString(user));
        String result6 = jsondb.getAsString("", prettyPrint).trim();
        assertThat(result6).isEqualTo(load("result6.json").trim());

        jsondb.set("/data/1", mapper.writeValueAsString("update"));
        String result7 = jsondb.getAsString("", prettyPrint).trim();
        assertThat(result7).isEqualTo(load("result7.json").trim());

        // Validate the large arrays stay sorted.
        jsondb.set("/", mapper.writeValueAsString(new Object[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13}));
        String result8 = jsondb.getAsString("", prettyPrint).trim();
        assertThat(result8).isEqualTo(load("result8.json").trim());

    }

    @Test
    public void testDelete() throws IOException {

        HashMap<String, Object> user = map(
            "name", "Joe",
            "developer", false
        );

        jsondb.set("/", mapper.writeValueAsString(user));
        Map<?, ?> result = mapper.readValue(jsondb.getAsString(""), HashMap.class);
        assertThat(result).hasSize(2);

        assertThat(jsondb.delete("/badpath")).isFalse();
        result = mapper.readValue(jsondb.getAsString(""), HashMap.class);
        assertThat(result).hasSize(2);

        assertThat(jsondb.delete("/name")).isTrue();
        result = mapper.readValue(jsondb.getAsString(""), HashMap.class);
        assertThat(result).hasSize(1);

    }

    @Test
    public void testExists() throws IOException {

        HashMap<String, Object> user = map(
            "name", "Joe",
            "developer", false
        );
        jsondb.set("/", mapper.writeValueAsString(user));

        assertThat(jsondb.exists("/badpath")).isFalse();
        assertThat(jsondb.exists("/name")).isTrue();

    }

    @Test
    public void shouldAscertainPropertyPairExistence() {
        jsondb.set("/pair/:id", "{\"key\": \"value\"}");

        assertThat(jsondb.fetchIdsByPropertyValue("/pair", "key", "value")).containsOnly("/pair/:id");
        assertThat(jsondb.fetchIdsByPropertyValue("/pair", "key", "nope")).isEmpty();
    }

    private String load(String file) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(file)) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            copy(is, os);
            return new String(os.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private void copy(InputStream is, ByteArrayOutputStream os) throws IOException {
        int c;
        while( (c=is.read())>=0 ) {
            os.write(c);
        }
    }

    // Helper method to help construct maps with concise syntax
    private HashMap<String, Object> map(Object... values) {
        HashMap<String, Object> rc = new HashMap<String, Object>() {
            @Override
            public String toString() {
                try {
                    return mapper.writeValueAsString(this);
                } catch (JsonProcessingException e) {
                    throw new JsonDBException(e);
                }
            }
        };
        for (int i = 0; i + 1 < values.length; i += 2) {
            rc.put(values[i].toString(), values[i + 1]);
        }
        return rc;
    }

}
