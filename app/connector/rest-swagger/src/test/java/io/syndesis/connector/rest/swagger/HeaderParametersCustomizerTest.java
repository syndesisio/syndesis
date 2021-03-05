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
package io.syndesis.connector.rest.swagger;

import java.util.Set;

import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasParameter;
import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HeaderParametersCustomizerTest {

    OasDocument createTestDocument(final OasDocument doc) {
        doc.paths = doc.createPaths();

        final OasPathItem slash = doc.paths.createPathItem("/");
        doc.paths.addPathItem("slash", slash);

        final OasOperation get = slash.createOperation("get");
        get.operationId = "get";
        slash.setOperation(get);

        final OasParameter p1 = get.createParameter();
        p1.name = "p1";
        get.addParameter(p1);

        final OasParameter h1 = get.createParameter();
        h1.name = "h1";
        h1.in = "header";
        get.addParameter(h1);

        final OasOperation post = slash.createOperation("post");
        post.operationId = "post";
        slash.setOperation(post);

        final OasParameter h2 = post.createParameter();
        h2.name = "h2";
        h2.in = "header";
        post.addParameter(h2);

        final OasParameter h3 = post.createParameter();
        h3.name = "h3";
        h3.in = "header";
        post.addParameter(h3);

        return doc;
    }

    Set<String> findHeaderParametersIn(final OasDocument doc, final String operationId) {
        final String json = Library.writeDocumentToJSONString(doc);

        return HeaderParametersCustomizer.findHeaderParametersFor(json, operationId);
    }

    @Test
    void shouldFindHeaderParametersInOperationFromOpenApi2() {
        assertThat(findHeaderParametersIn(createTestDocument(new Oas20Document()), "get")).containsOnly("h1");
        assertThat(findHeaderParametersIn(createTestDocument(new Oas20Document()), "post")).containsOnly("h2", "h3");
    }

    @Test
    void shouldFindHeaderParametersInOperationFromOpenApi3() {
        assertThat(findHeaderParametersIn(createTestDocument(new Oas30Document()), "get")).containsOnly("h1");
        assertThat(findHeaderParametersIn(createTestDocument(new Oas30Document()), "post")).containsOnly("h2", "h3");
    }

    @Test
    void shouldNotFindAnyHeaderParametersInEmptyDocument() {
        final Oas20Document doc = new Oas20Document();
        final String json = Library.writeDocumentToJSONString(doc);

        assertThat(HeaderParametersCustomizer.findHeaderParametersFor(json, "")).isEmpty();
    }
}
