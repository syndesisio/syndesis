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
package io.syndesis.server.jsondb.impl;


import static io.syndesis.common.util.Resources.getResourceAsText;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.syndesis.server.jsondb.Filter;
import io.syndesis.server.jsondb.Filter.Op;
import io.syndesis.server.jsondb.GetOptions;
import io.syndesis.server.jsondb.JsonDBException;

/**
 * Unit Tests for the JsonDB implementation.
 */
public class JsonDBTest {

    private SqlJsonDB jsondb;
    private ObjectMapper mapper = new ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.ALWAYS);

    private GetOptions prettyPrint = new GetOptions().prettyPrint(true);

    @Before
    public void before() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        DBI dbi = new DBI(ds);

        this.jsondb = new SqlJsonDB(dbi, null,
            Arrays.asList(
                new Index("/pair", "key"),
                new Index("/users", "name"),
                new Index("/users", "age")
            )
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
    public void testGetLimit() throws IOException {

        jsondb.set("/test", mapper.writeValueAsString(map(
            "user1", "test 1",
            "user2", "test 2",
            "user3", "test 3",
            "user4", "test 4",
            "user5", "test 5",
            "user6", "test 6"
        )));

        String json = jsondb.getAsString("/test", new GetOptions().limitToFirst(3));
        assertThat(json).isEqualTo("{\"user1\":\"test 1\",\"user2\":\"test 2\",\"user3\":\"test 3\"}");
    }

    @Test
    public void testGetLimitDeeper() throws IOException {

        jsondb.update("/test", mapper.writeValueAsString(map(
            "user1/value", "test 1",
            "user2/value", "test 2",
            "user3/value", "test 3",
            "user4/value", "test 4",
            "user5/value", "test 5",
            "user6/value", "test 6"
        )));

        String json = jsondb.getAsString("/test", new GetOptions().limitToFirst(3));
        assertThat(json).isEqualTo("{\"user1\":{\"value\":\"test 1\"},\"user2\":{\"value\":\"test 2\"},\"user3\":{\"value\":\"test 3\"}}");
    }

    @Test
    public void testGetOrder() throws IOException {

        jsondb.set("/test", mapper.writeValueAsString(map(
            "user1", "test 1",
            "user2", "test 2",
            "user3", "test 3"
        )));

        // Default order is ASC
        String json = jsondb.getAsString("/test", new GetOptions());
        assertThat(json).isEqualTo("{\"user1\":\"test 1\",\"user2\":\"test 2\",\"user3\":\"test 3\"}");

        // Explicit ASC should give us the same.
        json = jsondb.getAsString("/test", new GetOptions().order(GetOptions.Order.ASC));
        assertThat(json).isEqualTo("{\"user1\":\"test 1\",\"user2\":\"test 2\",\"user3\":\"test 3\"}");

        // DESC ord should reverse the output order.
        json = jsondb.getAsString("/test", new GetOptions().order(GetOptions.Order.DESC));
        assertThat(json).isEqualTo("{\"user3\":\"test 3\",\"user2\":\"test 2\",\"user1\":\"test 1\"}");
    }


    @Test
    public void testGetStartAfter() throws IOException {

        jsondb.set("/test", mapper.writeValueAsString(map(
            "user1", "test 1",
            "user2", "test 2",
            "user3", "test 3",
            "user4", "test 4",
            "user5", "test 5",
            "user6", "test 6"
        )));

        String json = jsondb.getAsString("/test", new GetOptions().startAfter("user3"));
        assertThat(json).isEqualTo("{\"user4\":\"test 4\",\"user5\":\"test 5\",\"user6\":\"test 6\"}");
    }

    @Test
    public void testGetStartAfterWithDESC() throws IOException {

        jsondb.set("/test", mapper.writeValueAsString(map(
            "user1", "test 1",
            "user2", "test 2",
            "user3", "test 3",
            "user4", "test 4",
            "user5", "test 5",
            "user6", "test 6"
        )));

        String json = jsondb.getAsString("/test", new GetOptions().startAfter("user3").order(GetOptions.Order.DESC));
        assertThat(json).isEqualTo("{\"user2\":\"test 2\",\"user1\":\"test 1\"}");
    }


    @Test
    public void testGetStartAt() throws IOException {

        jsondb.set("/test", mapper.writeValueAsString(map(
            "user1", "test 1",
            "user2", "test 2",
            "user3", "test 3",
            "user4", "test 4",
            "user5", "test 5",
            "user6", "test 6"
        )));

        String json = jsondb.getAsString("/test", new GetOptions().startAt("user4"));
        assertThat(json).isEqualTo("{\"user4\":\"test 4\",\"user5\":\"test 5\",\"user6\":\"test 6\"}");
    }

    @Test
    public void testGetStartAtWithDESC() throws IOException {

        jsondb.set("/test", mapper.writeValueAsString(map(
            "user1", "test 1",
            "user2", "test 2",
            "user3", "test 3",
            "user4", "test 4",
            "user5", "test 5",
            "user6", "test 6"
        )));

        String json = jsondb.getAsString("/test", new GetOptions().startAt("user2").order(GetOptions.Order.DESC));
        assertThat(json).isEqualTo("{\"user2\":\"test 2\",\"user1\":\"test 1\"}");
    }

    @Test
    public void testGetEndBefore() throws IOException {

        jsondb.set("/test", mapper.writeValueAsString(map(
            "user1", "test 1",
            "user2", "test 2",
            "user3", "test 3",
            "user4", "test 4",
            "user5", "test 5",
            "user6", "test 6"
        )));

        String json = jsondb.getAsString("/test", new GetOptions().endBefore("user5"));
        assertThat(json).isEqualTo("{\"user1\":\"test 1\",\"user2\":\"test 2\",\"user3\":\"test 3\",\"user4\":\"test 4\"}");
    }

    @Test
    public void testGetEndBeforeWithDESC() throws IOException {

        jsondb.set("/test", mapper.writeValueAsString(map(
            "user1", "test 1",
            "user2", "test 2",
            "user3", "test 3",
            "user4", "test 4",
            "user5", "test 5",
            "user6", "test 6"
        )));

        String json = jsondb.getAsString("/test", new GetOptions().endBefore("user2").order(GetOptions.Order.DESC));
        assertThat(json).isEqualTo("{\"user6\":\"test 6\",\"user5\":\"test 5\",\"user4\":\"test 4\",\"user3\":\"test 3\"}");
    }


    @Test
    public void testGetEndAt() throws IOException {

        jsondb.set("/test", mapper.writeValueAsString(map(
            "user1", "test 1",
            "user2", "test 2",
            "user3", "test 3",
            "user4", "test 4",
            "user5", "test 5",
            "user6", "test 6"
        )));

        String json = jsondb.getAsString("/test", new GetOptions().endAt("user4"));
        assertThat(json).isEqualTo("{\"user1\":\"test 1\",\"user2\":\"test 2\",\"user3\":\"test 3\",\"user4\":\"test 4\"}");
    }

    @Test
    public void testGetEndAtWithDESC() throws IOException {

        jsondb.set("/test", mapper.writeValueAsString(map(
            "user1", "test 1",
            "user2", "test 2",
            "user3", "test 3",
            "user4", "test 4",
            "user5", "test 5",
            "user6", "test 6"
        )));

        String json = jsondb.getAsString("/test", new GetOptions().endAt("user3").order(GetOptions.Order.DESC));
        assertThat(json).isEqualTo("{\"user6\":\"test 6\",\"user5\":\"test 5\",\"user4\":\"test 4\",\"user3\":\"test 3\"}");
    }

    @Test
    public void testGetStartAtEndAt() throws IOException {
        jsondb.set("/test", mapper.writeValueAsString(map(
            "user1", "1",
            "user2:1", "2",
            "user2:2", "3",
            "user2:3", "4",
            "user4", "5"
        )));

        String json = jsondb.getAsString("/test", new GetOptions().startAt("user2:").endAt("user2:"));
        assertThat(json).isEqualTo("{\"user2:1\":\"2\",\"user2:2\":\"3\",\"user2:3\":\"4\"}");
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

        jsondb.delete("/test");
        jsondb.set("/test/a1/b1/c1", "1");
        jsondb.set("/test/a1/b2/c1", "2");
        jsondb.set("/test/a2/b3/c1", "3");
        jsondb.set("/test/a3/b4/c1", "4");
        jsondb.set("/test/a4/b5/c1", "5");

        json = jsondb.getAsString("/test", new GetOptions().depth(1));
        assertThat(json).isEqualTo("{\"a1\":true,\"a2\":true,\"a3\":true,\"a4\":true}");

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
        assertThat(items.get(0).get("name")).isEqualTo("Hiram Chirino");
        assertThat(items.get(1).get("name")).isEqualTo("Ana Chirino");
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
        assertThat(result1).isEqualTo(getResourceAsText("result1.json").trim());

        String result2 = jsondb.getAsString("/", prettyPrint).trim();
        assertThat(result2).isEqualTo(result1);

        String result3 = jsondb.getAsString("/users/u1000", prettyPrint).trim();
        assertThat(result3).isEqualTo(getResourceAsText("result3.json").trim());

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
        assertThat(result3).isEqualTo(getResourceAsText("result3.json").trim());

        // Verify that if we write over an existing /developer value, it get replaced
        // with an object
        jsondb.set("/developer/users/u1000", mapper.writeValueAsString(user));
        String result4 = jsondb.getAsString("", prettyPrint).trim();
        assertThat(result4).isEqualTo(getResourceAsText("result4.json").trim());


        // Writing a new object wipe the previous values at that path.
        HashMap<String, Object> user2 = map(
            "name", "Hiram",
            "city", "Tampa"
        );
        jsondb.set("/", mapper.writeValueAsString(user2));
        String result5 = jsondb.getAsString("", prettyPrint).trim();
        assertThat(result5).isEqualTo(getResourceAsText("result5.json").trim());

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
        assertThat(result6).isEqualTo(getResourceAsText("result6.json").trim());

        jsondb.set("/data/1", mapper.writeValueAsString("update"));
        String result7 = jsondb.getAsString("", prettyPrint).trim();
        assertThat(result7).isEqualTo(getResourceAsText("result7.json").trim());

        // Validate the large arrays stay sorted.
        jsondb.set("/", mapper.writeValueAsString(new Object[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13}));
        String result8 = jsondb.getAsString("", prettyPrint).trim();
        assertThat(result8).isEqualTo(getResourceAsText("result8.json").trim());

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


    @Test
    public void testGetFilter() throws IOException {

        jsondb.set("/users/u1", mapper.writeValueAsString(map(
            "name", "u1",
            "age", 9
        )));
        jsondb.set("/users/u2", mapper.writeValueAsString(map(
            "name", "u2",
            "age", 10
        )));
        jsondb.set("/users/u3", mapper.writeValueAsString(map(
            "name", "u3",
            "age", 21
        )));

        ////////////////////////////////////////
        //
        // Check filtering ops against strings..
        //
        ////////////////////////////////////////
        assertThat(jsondb.getAsString("/users", new GetOptions().filter(
            Filter.child("name", Op.EQ, "u2")
        ))).isEqualTo(
            "{\"u2\":{\"age\":10,\"name\":\"u2\"}}"
        );
        assertThat(jsondb.getAsString("/users", new GetOptions().filter(
            Filter.child("name", Op.NEQ, "u2")
        ))).isEqualTo(
            "{\"u1\":{\"age\":9,\"name\":\"u1\"},\"u3\":{\"age\":21,\"name\":\"u3\"}}"
        );
        assertThat(jsondb.getAsString("/users", new GetOptions().filter(
            Filter.child("name", Op.LT, "u2")
        ))).isEqualTo(
            "{\"u1\":{\"age\":9,\"name\":\"u1\"}}"
        );
        assertThat(jsondb.getAsString("/users", new GetOptions().filter(
            Filter.child("name", Op.GT, "u2")
        ))).isEqualTo(
            "{\"u3\":{\"age\":21,\"name\":\"u3\"}}"
        );
        assertThat(jsondb.getAsString("/users", new GetOptions().filter(
            Filter.child("name", Op.LTE, "u2")
        ))).isEqualTo(
            "{\"u1\":{\"age\":9,\"name\":\"u1\"},\"u2\":{\"age\":10,\"name\":\"u2\"}}"
        );
        assertThat(jsondb.getAsString("/users", new GetOptions().filter(
            Filter.child("name", Op.GTE, "u2")
        ))).isEqualTo(
            "{\"u2\":{\"age\":10,\"name\":\"u2\"},\"u3\":{\"age\":21,\"name\":\"u3\"}}"
        );

        ////////////////////////////////////////
        //
        // Check filtering ops against numbers..
        //
        ////////////////////////////////////////
        assertThat(jsondb.getAsString("/users", new GetOptions().filter(
            Filter.child("age", Op.EQ, 10)
        ))).isEqualTo(
            "{\"u2\":{\"age\":10,\"name\":\"u2\"}}"
        );
        assertThat(jsondb.getAsString("/users", new GetOptions().filter(
            Filter.child("age", Op.NEQ, 10)
        ))).isEqualTo(
            "{\"u1\":{\"age\":9,\"name\":\"u1\"},\"u3\":{\"age\":21,\"name\":\"u3\"}}"
        );
        assertThat(jsondb.getAsString("/users", new GetOptions().filter(
            Filter.child("age", Op.LT, 10)
        ))).isEqualTo(
            "{\"u1\":{\"age\":9,\"name\":\"u1\"}}"
        );
        assertThat(jsondb.getAsString("/users", new GetOptions().filter(
            Filter.child("age", Op.GT, 10)
        ))).isEqualTo(
            "{\"u3\":{\"age\":21,\"name\":\"u3\"}}"
        );
        assertThat(jsondb.getAsString("/users", new GetOptions().filter(
            Filter.child("age", Op.LTE, 10)
        ))).isEqualTo(
            "{\"u1\":{\"age\":9,\"name\":\"u1\"},\"u2\":{\"age\":10,\"name\":\"u2\"}}"
        );
        assertThat(jsondb.getAsString("/users", new GetOptions().filter(
            Filter.child("age", Op.GTE, 10)
        ))).isEqualTo(
            "{\"u2\":{\"age\":10,\"name\":\"u2\"},\"u3\":{\"age\":21,\"name\":\"u3\"}}"
        );

        ////////////////////////////////////////
        //
        // Check filtering on multiple conditions.
        //
        ////////////////////////////////////////
        assertThat(jsondb.getAsString("/users", new GetOptions().filter(
            Filter.and(Filter.child("age", Op.GT, 9),Filter.child("name", Op.LT, "u3"))
        ))).isEqualTo(
            "{\"u2\":{\"age\":10,\"name\":\"u2\"}}"
        );

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
