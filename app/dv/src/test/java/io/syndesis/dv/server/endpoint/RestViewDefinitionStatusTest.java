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

import static org.junit.Assert.*;

import org.junit.Test;

import io.syndesis.dv.model.ViewDefinition;
import io.syndesis.dv.rest.JsonMarshaller;
import io.syndesis.dv.server.endpoint.RestViewDefinitionStatus;

@SuppressWarnings( { "javadoc", "nls" } )
public final class RestViewDefinitionStatusTest {

    @Test public void shouldSerialize() {
        RestViewDefinitionStatus status = new RestViewDefinitionStatus();
        status.setStatus("FINE");
        status.setMessage("Maybe a warning?");
        assertEquals("{\n" +
                "  \"status\" : \"FINE\",\n" +
                "  \"message\" : \"Maybe a warning?\"\n" +
                "}", JsonMarshaller.marshall(status));

        ViewDefinition vd = new ViewDefinition("x", "y");
        status.setViewDefinition(vd);
        assertEquals("{\n" +
                "  \"status\" : \"FINE\",\n" +
                "  \"message\" : \"Maybe a warning?\",\n" +
                "  \"complete\" : false,\n" +
                "  \"dataVirtualizationName\" : \"x\",\n" +
                "  \"name\" : \"y\",\n" +
                "  \"sourcePaths\" : [ ],\n" +
                "  \"userDefined\" : false\n" +
                "}", JsonMarshaller.marshall(status));
    }

}
