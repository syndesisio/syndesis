package io.syndesis.server.runtime.integration;
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


import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.camel.generator.openapi.RestDslGenerator;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.core.models.Document;
import io.apicurio.datamodels.openapi.models.OasDocument;

public class ProjectDSLGeneratorTest {

    // ***************************
    // Tests
    // ***************************
    @Test
    public void testGenerateRestDSL() throws Exception {

        final InputStream is = this.getClass().getResourceAsStream("/io/syndesis/server/runtime/task-api.json");
        byte[] openApiBytes = IOUtils.toByteArray(is);

        final Document openApiDoc = Library.readDocumentFromJSONString(new String(openApiBytes, StandardCharsets.UTF_8));

        final StringBuilder code = new StringBuilder();
        RestDslGenerator.toAppendable((OasDocument) openApiDoc)
            .withClassName("RestRoute")
            .withPackageName("io.syndesis.example")
            .withoutSourceCodeTimestamps()
            .generate(code);

        Assert.assertTrue(code.length() > 0);
    }
}
