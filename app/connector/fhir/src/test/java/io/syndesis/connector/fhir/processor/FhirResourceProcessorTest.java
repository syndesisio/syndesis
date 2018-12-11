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

import ca.uhn.fhir.context.FhirVersionEnum;
import io.atlasmap.xml.inspect.SchemaInspector;
import io.syndesis.connector.fhir.FhirMetaDataExtension;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

@SuppressWarnings({"PMD.SignatureDeclareThrowsException", "PMD.JUnitTestsShouldIncludeAssert"})
public class FhirResourceProcessorTest {

    @Test
    public void buildSpecificationShouldIncludeFhirBase() throws Exception {
        FhirResourceProcessor fhirResourceProcessor = new FhirResourceProcessor();

        File account = new File("src/main/resources/META-INF/syndesis/schemas/dstu3/account.xsd");

        String accountSpecification = fhirResourceProcessor.buildSpecification(account);

        Assertions.assertThat(accountSpecification).doesNotContain("<xs:include schemaLocation=\"fhir-base.xsd\"/>");
        Assertions.assertThat(accountSpecification).containsSequence("<xs:complexType name=\"ResourceContainer\"><xs:choice><xs:element ref=\"Account\"/></xs:choice></xs:complexType>");

        File patient = new File("src/main/resources/META-INF/syndesis/schemas/dstu3/patient.xsd");

        String patientSpecification = fhirResourceProcessor.buildSpecification(patient);

        Assertions.assertThat(patientSpecification).doesNotContain("<xs:include schemaLocation=\"fhir-base.xsd\"/>");
        Assertions.assertThat(patientSpecification).containsSequence("<xs:complexType name=\"ResourceContainer\"><xs:choice><xs:element ref=\"Patient\"/></xs:choice></xs:complexType>");
    }

    @Test
    public void dstu3SpecificationsShouldBeValid() throws Exception {
        Assertions.assertThat(FhirMetaDataExtension.getResources(FhirVersionEnum.DSTU3)).hasSize(117);

        SchemaInspector inspector = new SchemaInspector();
        FhirResourceProcessor fhirResourceProcessor = new FhirResourceProcessor();

        List<Exception> errors = new ArrayList<>();
        for (String resource : FhirMetaDataExtension.getResources(FhirVersionEnum.DSTU3)) {
            File file = new File("src/main/resources/META-INF/syndesis/schemas/dstu3/" + resource.toLowerCase() + ".xsd");
            String specification = fhirResourceProcessor.buildSpecification(file);
            try {
                inspector.inspect(specification);

            } catch (Exception e) {
                errors.add(new RuntimeException(resource + " specification failed validation due to " + e.getMessage(), e));
            }
        }

        Assertions.assertThat(errors).hasSize(0);
    }
}
