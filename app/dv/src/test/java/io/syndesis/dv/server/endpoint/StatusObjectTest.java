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

package io.syndesis.dv.server.endpoint;

import io.syndesis.dv.rest.JsonMarshaller;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StatusObjectTest {

    @Test public void testJsonRoundtrip() {
        StatusObject kso = new StatusObject("x");
        kso.addAttribute("attribute", "message");
        kso.addAttribute("attribute1", "message1");

        String value = JsonMarshaller.marshall(kso);
        assertEquals("{\n" +
                "  \"title\" : \"x\",\n" +
                "  \"attributes\" : {\n" +
                "    \"attribute\" : \"message\",\n" +
                "    \"attribute1\" : \"message1\"\n" +
                "  }\n" +
                "}", value);

        StatusObject other = JsonMarshaller.unmarshall(value, StatusObject.class);

        assertEquals(kso, other);
    }

}
