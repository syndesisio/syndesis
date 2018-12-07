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
package io.syndesis.connector.fhir.processor;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FhirResourceProcessor implements Function<File, File> {

    @SuppressWarnings({"PMD.AvoidThrowingRawExceptionTypes"})
    @Override
    public File apply(File file) {
        if (!file.getName().endsWith(".xsd")) {
            return file;
        }

        try {
            String specification = buildSpecification(file);

            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                IOUtils.write(specification, fileOut);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read input", e);
        }

        return file;
    }

    @SuppressWarnings({"PMD.UseStringBufferForStringAppends"})
    String buildSpecification(File file) throws IOException {
        String specification;
        try (FileInputStream fileIn = new FileInputStream(file)) {
            specification = IOUtils.toString(fileIn);
        }
        String fhirBaseTemplate;
        try (FileInputStream baseIn = new FileInputStream(new File(file.getParentFile(), "fhir-base-template.xml"))) {
            fhirBaseTemplate = IOUtils.toString(baseIn);
        }
        String fhirCommonTemplate;
        try (FileInputStream commonIn = new FileInputStream(new File(file.getParentFile(), "fhir-common-template.xml"))) {
            fhirCommonTemplate = IOUtils.toString(commonIn);
        }

        Pattern pattern = Pattern.compile("<xs:element name=\"(\\w+)\" type=\"(\\w+)\">");
        Matcher matcher = pattern.matcher(specification);
        matcher.find();
        String type = matcher.group(1);

        String resourceContainer = "<xs:complexType name=\"ResourceContainer\"><xs:choice><xs:element ref=\"" + type + "\"/></xs:choice></xs:complexType>";
        fhirBaseTemplate = StringUtils.replaceOnce(fhirBaseTemplate, "<!-- RESOURCE CONTAINER PLACEHOLDER -->", resourceContainer);

        fhirBaseTemplate += fhirCommonTemplate;
        specification = StringUtils.replaceOnce(specification, "<xs:include schemaLocation=\"fhir-base.xsd\"/>", fhirBaseTemplate);
        return specification;
    }
}
