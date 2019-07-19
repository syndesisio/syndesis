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

import io.syndesis.connector.fhir.FhirResourceQuery;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.fhir.internal.FhirSearchApiMethod;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.component.ApiMethod;
import org.hl7.fhir.dstu3.model.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FhirSearchCustomizer  extends FhirReadCustomizer {

    protected String query;

    @Override
    public Class<? extends ApiMethod> getApiMethodClass() {
        return FhirSearchApiMethod.class;
    }

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        super.customize(component, options);

        query = ConnectorOptions.extractOption(options, "query");

        options.put("methodName", "searchByUrl?inBody=url");
    }


    @Override
    public void beforeProducer(Exchange exchange) {
        final Message in = exchange.getIn();

        if (ObjectHelper.isNotEmpty(query)) {
            in.setBody(resourceType + "?" + query);
        }

        FhirResourceQuery body = in.getBody(FhirResourceQuery.class);
        if (body != null && ObjectHelper.isNotEmpty(body.getQuery())) {
            in.setBody(resourceType + "?" + body.getQuery());
        }
    }

    @Override
    public void afterProducer(Exchange exchange) {
        Message in = exchange.getIn();
        Bundle bundle = in.getBody(Bundle.class);
        if (bundle == null) {
            return;
        }

        List<String> results = new ArrayList<>();
        for (Bundle.BundleEntryComponent entry: bundle.getEntry()) {
            String resource = fhirContext.newXmlParser().encodeResourceToString(entry.getResource());
            results.add(resource);
        }

        in.setBody(results);
    }
}
