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

import ca.uhn.fhir.rest.api.MethodOutcome;
import io.syndesis.common.model.integration.Step;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.okXml;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class FhirUpdateTest extends FhirTestBase {

    @Override
    protected List<Step> createSteps() {
        return Arrays.asList(newSimpleEndpointStep(
            "direct",
            builder -> builder.putConfiguredProperty("name", "start")),
            newFhirEndpointStep("io.syndesis:fhir-update-connector", builder -> {
                builder.putConfiguredProperty("resourceType", "Patient");
            }));
    }

    @Test
    public void updateTest() {
        stubFhirRequest(put(urlEqualTo("/Patient/1234")).willReturn(okXml(toXml(new OperationOutcome()))));

        template.requestBody("direct:start",
            toXml(new Patient().addName(new HumanName().setFamily("Smith")).setId("1234")), MethodOutcome.class);
    }


}
