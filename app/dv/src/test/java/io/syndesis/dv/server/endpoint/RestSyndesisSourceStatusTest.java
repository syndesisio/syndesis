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

import java.util.Arrays;

import io.syndesis.dv.rest.JsonMarshaller;
import io.syndesis.dv.server.endpoint.RestSyndesisSourceStatus.EntityState;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("nls")
public class RestSyndesisSourceStatusTest {

    @Test public void testSerialization() {
        RestSyndesisSourceStatus rsss = new RestSyndesisSourceStatus("x");
        rsss.setId("id");
        rsss.setTeiidName("tName");
        rsss.setErrors(Arrays.asList("some error"));
        rsss.setSchemaState(EntityState.ACTIVE);
        rsss.setLoading(true);

        String value = JsonMarshaller.marshall(rsss);
        assertEquals("{\n" +
                "  \"sourceName\" : \"x\",\n" +
                "  \"teiidName\" : \"tName\",\n" +
                "  \"errors\" : [ \"some error\" ],\n" +
                "  \"schemaState\" : \"ACTIVE\",\n" +
                "  \"id\" : \"id\",\n" +
                "  \"loading\" : true\n" +
                "}", value);
    }

}
