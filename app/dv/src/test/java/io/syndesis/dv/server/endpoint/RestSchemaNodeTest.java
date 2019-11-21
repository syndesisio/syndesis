/*
 * Copyright (C) 2013 Red Hat, Inc.
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

package io.syndesis.dv.server.endpoint;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import io.syndesis.dv.rest.JsonMarshaller;
import io.syndesis.dv.server.endpoint.RestSchemaNode;

public class RestSchemaNodeTest {

    @Test public void testJsonRoundtrip() {
        RestSchemaNode rsn = new RestSchemaNode("x", "y", "z");
        rsn.addChild(new RestSchemaNode("a", "b", "c"));

        String value = JsonMarshaller.marshall(rsn);
        String expected = "{\n" +
                "  \"children\" : [ {\n" +
                "    \"children\" : [ ],\n" +
                "    \"name\" : \"b\",\n" +
                "    \"connectionName\" : \"a\",\n" +
                "    \"type\" : \"c\",\n" +
                "    \"queryable\" : false\n" +
                "  } ],\n" +
                "  \"name\" : \"y\",\n" +
                "  \"connectionName\" : \"x\",\n" +
                "  \"type\" : \"z\",\n" +
                "  \"queryable\" : false\n" +
                "}";
        assertEquals(expected, value);

        RestSchemaNode other = JsonMarshaller.unmarshall(value, RestSchemaNode.class);

        assertEquals(expected, JsonMarshaller.marshall(other));

        value = JsonMarshaller.marshall(new Object[] {new RestSchemaNode(), new RestSchemaNode()}, true);

        assertEquals("[ {\n" +
                "  \"children\" : [ ],\n" +
                "  \"queryable\" : false\n" +
                "}, {\n" +
                "  \"children\" : [ ],\n" +
                "  \"queryable\" : false\n" +
                "} ]", value);
    }

}
