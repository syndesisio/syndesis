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
package io.syndesis.connector.fhir;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.atlasmap.xml.v2.XmlComplexType;
import io.atlasmap.xml.v2.XmlDocument;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class FhirMetadataRetrievalTest {

    @Test
    public void includeResourcesInBundle() throws Exception {
        Path bundle = FileSystems.getDefault().getPath("target/classes/META-INF/syndesis/schemas/dstu3/bundle.json");

        String inspection;
        try (InputStream fileIn = Files.newInputStream(bundle)) {
            inspection = IOUtils.toString(fileIn, StandardCharsets.UTF_8);
        }

        String inspectionWithResources = FhirMetadataRetrieval.includeResources(inspection, "patient", "account");

        ObjectMapper mapper = io.atlasmap.v2.Json.mapper();
        XmlDocument xmlDocument = mapper.readValue(inspectionWithResources, XmlDocument.class);

        XmlComplexType resource = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assertions.assertThat(resource.getName()).isEqualTo("tns:Bundle");
        assertCorrectPath(resource, "tns:Bundle", "tns:entry", "tns:resource", "tns:Patient");
        assertCorrectPath(resource, "tns:Bundle", "tns:entry", "tns:resource", "tns:Patient", "tns:contained", "tns:Account");
        assertCorrectPath(resource, "tns:Bundle", "tns:entry", "tns:response", "tns:outcome", "tns:Patient");
        assertCorrectPath(resource, "tns:Bundle", "tns:entry", "tns:response", "tns:outcome", "tns:Patient", "tns:contained", "tns:Account");
        assertCorrectPath(resource, "tns:Bundle", "tns:entry", "tns:resource", "tns:Account");
        assertCorrectPath(resource, "tns:Bundle", "tns:entry", "tns:resource", "tns:Account", "tns:contained", "tns:Patient");
        assertCorrectPath(resource, "tns:Bundle", "tns:entry", "tns:response", "tns:outcome", "tns:Account");
        assertCorrectPath(resource, "tns:Bundle", "tns:entry", "tns:response", "tns:outcome", "tns:Account", "tns:contained", "tns:Patient");
    }

    @Test
    public void includeResourcesInPatient() throws Exception {
        Path patient = FileSystems.getDefault().getPath("target/classes/META-INF/syndesis/schemas/dstu3/patient.json");

        String inspection;
        try (InputStream fileIn = Files.newInputStream(patient)) {
            inspection = IOUtils.toString(fileIn, StandardCharsets.UTF_8);
        }

        String inspectionWithResources = FhirMetadataRetrieval.includeResources(inspection, "person", "account");

        ObjectMapper mapper = io.atlasmap.v2.Json.mapper();
        XmlDocument xmlDocument = mapper.readValue(inspectionWithResources, XmlDocument.class);

        XmlComplexType resource = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assertions.assertThat(resource.getName()).isEqualTo("tns:Patient");
        assertCorrectPath(resource, "tns:Patient", "tns:contained", "tns:Person");
        assertCorrectPath(resource, "tns:Patient", "tns:contained", "tns:Person", "tns:contained", "tns:Person");
        assertCorrectPath(resource, "tns:Patient", "tns:contained", "tns:Person", "tns:contained", "tns:Account");
        assertCorrectPath(resource, "tns:Patient", "tns:contained", "tns:Account");
        assertCorrectPath(resource, "tns:Patient", "tns:contained", "tns:Account", "tns:contained", "tns:Person");
        assertCorrectPath(resource, "tns:Patient", "tns:contained", "tns:Account", "tns:contained", "tns:Account");
    }

    public String getPath(XmlComplexType fields, String... path) {
        XmlComplexType element = FhirMetadataRetrieval.getElement(fields, 0, path);
        return element != null ? element.getPath() : null;
    }

    public void assertCorrectPath(XmlComplexType fields, String rootName, String... path) {
        String expectedPath = "/" + rootName + "/" + StringUtils.join(path, "/");
        Assertions.assertThat(getPath(fields, path)).isEqualTo(expectedPath);
    }
}
