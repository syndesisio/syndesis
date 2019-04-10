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
package io.syndesis.connector.fhir.customizer;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.fhir.internal.FhirReadApiMethod;
import org.apache.camel.util.component.ApiMethod;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class FhirReadCustomizer extends FhirReadDeleteBaseCustomizer {

    @Override
    public Class<? extends ApiMethod> getApiMethodClass() {
        return FhirReadApiMethod.class;
    }

    @Override
    public void afterProducer(Exchange exchange) {
        Message in = exchange.getIn();
        IBaseResource body = in.getBody(IBaseResource.class);
        if (body == null) {
            return;
        }

        String s = fhirContext.newXmlParser().encodeResourceToString(body);
        in.setBody(s);
    }

}
