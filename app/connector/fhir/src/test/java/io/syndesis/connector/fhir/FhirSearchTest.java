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

import io.syndesis.common.model.integration.Step;
import org.assertj.core.api.Assertions;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okXml;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class FhirSearchTest extends FhirTestBase {

    @Override
    protected List<Step> createSteps() {
        return Arrays.asList(newSimpleEndpointStep(
            "direct",
            builder -> builder.putConfiguredProperty("name", "start")),
            newFhirEndpointStep("io.syndesis:fhir-search-connector", builder -> {
                builder.putConfiguredProperty("resourceType", "Patient");
            }));
    }

    @Test
    public void searchTest() {
        Patient one = new Patient();
        one.setId("one");
        one.setGender(Enumerations.AdministrativeGender.UNKNOWN);
        Patient two = new Patient();
        two.setId("two");
        two.setGender(Enumerations.AdministrativeGender.UNKNOWN);

        Bundle bundle = new Bundle();
        bundle.getEntry().add(new Bundle.BundleEntryComponent().setResource(one));
        bundle.getEntry().add(new Bundle.BundleEntryComponent().setResource(two));

        stubFhirRequest(get(urlEqualTo("/Patient?gender=unknown")).willReturn(okXml(toXml(bundle))));

        FhirResourceQuery query = new FhirResourceQuery();
        query.setQuery("gender=unknown");

        String result = template.requestBody("direct:start", query, String.class);

        Assertions.assertThat(result).isEqualTo(
            "[<Patient xmlns=\"http://hl7.org/fhir\"><id value=\"one\"/><gender value=\"unknown\"/></Patient>, " +
            "<Patient xmlns=\"http://hl7.org/fhir\"><id value=\"two\"/><gender value=\"unknown\"/></Patient>]");
    }


}
