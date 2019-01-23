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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FhirResourcesProcessor implements BiConsumer<Path, Path> {

    @SuppressWarnings({"PMD.AvoidThrowingRawExceptionTypes"})
    @Override
    public void accept(Path inputDir, Path outputDir) {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(inputDir, "*.xsd")){
            for (Path file : files) {
                String schema = buildSchema(file);

                try (OutputStream fileOut = Files.newOutputStream(outputDir.resolve(file.getFileName()))) {
                    IOUtils.write(schema, fileOut, StandardCharsets.UTF_8);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to process files in " + inputDir, e);
        }
    }

    @SuppressWarnings({"PMD.UseStringBufferForStringAppends"})
    String buildSchema(Path file) throws IOException {
        String specification;
        try (InputStream fileIn = Files.newInputStream(file)) {
            specification = IOUtils.toString(fileIn, StandardCharsets.UTF_8);
        }

        final Path parent = file.getParent();
        if (parent == null) {
            throw new IllegalArgumentException(file + " needs to be within a directory");
        }

        String fhirBaseTemplate;
        try (InputStream baseIn = Files.newInputStream(parent.resolve("fhir-base-template.xml"))) {
            fhirBaseTemplate = IOUtils.toString(baseIn, StandardCharsets.UTF_8);
        }
        String fhirCommonTemplate;
        try (InputStream commonIn = Files.newInputStream(parent.resolve("fhir-common-template.xml"))) {
            fhirCommonTemplate = IOUtils.toString(commonIn, StandardCharsets.UTF_8);
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
