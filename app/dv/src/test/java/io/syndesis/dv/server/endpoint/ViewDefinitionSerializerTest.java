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
import java.util.List;

import io.syndesis.dv.model.ViewDefinition;
import io.syndesis.dv.rest.JsonMarshaller;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SuppressWarnings("nls")
public class ViewDefinitionSerializerTest {

    private final String viewName = "myNewView";
    private final String viewDefinitionName = "testView";
    private final String description = "test view description text";
    private final boolean isComplete = true;
    private final boolean isUserDefined = false;
    private final String sourceTablePath1 = "path/to/source1";
    private final String sourceTablePath2 = "path/to/source2";
    private final String sourceTablePath3 = "path/to/source3";
    private final String sourceTablePath4 = "path/to/source4";

    private static String createViewEditorState() {
        return "{\n" +
                "  \"complete\" : true,\n" +
                "  \"dataVirtualizationName\" : \"dvName\",\n" +
                "  \"description\" : \"test view description text\",\n" +
                "  \"id\" : \"myNewView\",\n" +
                "  \"name\" : \"testView\",\n" +
                "  \"sourcePaths\" : [ \"path/to/source1\", \"path/to/source2\", \"path/to/source3\", \"path/to/source4\" ],\n" +
                "  \"userDefined\" : false\n" +
                "}";
    }

    @Test
    public void shouldImportJson() {
        String state = createViewEditorState();

        ViewDefinition viewEditorState = JsonMarshaller.unmarshall(state, io.syndesis.dv.model.ViewDefinition.class);
        assertEquals(viewName, viewEditorState.getId());

        assertNotNull(viewEditorState);
        assertEquals(viewDefinitionName, viewEditorState.getName());

        List<String> paths = viewEditorState.getSourcePaths();
        assertNotNull(paths);
        assertEquals(4, paths.size());
    }

    @Test
    public void shouldExportJson() {
        String[] sourceTablePaths = { sourceTablePath1, sourceTablePath2, sourceTablePath3, sourceTablePath4 };
        io.syndesis.dv.model.ViewDefinition viewDef = new io.syndesis.dv.model.ViewDefinition("dvName", viewDefinitionName);
        viewDef.setId(viewName);
        viewDef.setDescription(description);
        viewDef.setComplete(isComplete);
        viewDef.setUserDefined(isUserDefined);
        viewDef.setSourcePaths(Arrays.asList(sourceTablePaths));

        String expectedJson = createViewEditorState();

        String resultJson = JsonMarshaller.marshall(viewDef);

        assertEquals(expectedJson, resultJson);
    }
}
