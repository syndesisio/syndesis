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
import org.hl7.fhir.dstu3.model.Account;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToXml;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingXPath;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okXml;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class FhirTransactionTest extends FhirTestBase {

    @Override
    protected List<Step> createSteps() {
        return Arrays.asList(newSimpleEndpointStep(
            "direct",
            builder -> builder.putConfiguredProperty("name", "start")),
            newFhirEndpointStep("io.syndesis:fhir-transaction-connector", builder -> {
                builder.putConfiguredProperty("containedResourceTypes", "Patient,Account");
            }));
    }

    @Test
    public void transactionTest() {
        stubFhirRequest(post(urlEqualTo("/")).withRequestBody(containing(
            "<type value=\"transaction\"/><total value=\"2\"/><link><relation value=\"fhir-base\"/></link>" +
                "<link><relation value=\"self\"/></link>" +
                "<entry><resource><Account xmlns=\"http://hl7.org/fhir\"><name value=\"Joe\"/></Account></resource>" +
                "<request><method value=\"POST\"/></request></entry><entry><resource>" +
                "<Patient xmlns=\"http://hl7.org/fhir\"><name><family value=\"Jackson\"/></name></Patient></resource>" +
                "<request><method value=\"POST\"/></request></entry>")).willReturn(okXml(toXml(new Bundle()))));

        template.requestBody("direct:start",
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<tns:Transaction xmlns:tns=\"http://hl7.org/fhir\">" +
                "<tns:Account><tns:name value=\"Joe\"/></tns:Account>" +
                "<tns:Patient><name><tns:family value=\"Jackson\"/></name></tns:Patient></tns:Transaction>");
    }


}
