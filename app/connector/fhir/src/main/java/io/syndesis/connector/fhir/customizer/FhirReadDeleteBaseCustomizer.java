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

import ca.uhn.fhir.context.FhirContext;
import io.syndesis.connector.fhir.FhirResourceId;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.component.ApiMethod;

import java.util.Map;

public abstract class FhirReadDeleteBaseCustomizer implements ComponentProxyCustomizer {
    protected FhirContext fhirContext;
    protected String resourceType;
    protected String id;
    protected String version;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        fhirContext = FhirCustomizerHelper.newFhirContext(options);

        resourceType = ConnectorOptions.extractOption(options, "resourceType");
        id = ConnectorOptions.extractOption(options, "id");
        version = ConnectorOptions.extractOption(options, "version");

        options.put("apiName", FhirCustomizerHelper.getFhirApiName(getApiMethodClass()));
        options.put("methodName", "resourceById");

        component.setBeforeProducer(this::beforeProducer);
        component.setAfterProducer(this::afterProducer);
    }

    public abstract Class<? extends ApiMethod> getApiMethodClass();

    @SuppressWarnings({"PMD.EmptyMethodInAbstractClassShouldBeAbstract"})
    public void afterProducer(Exchange exchange) {
        //By default do nothing
    }

    public void beforeProducer(Exchange exchange) {
        final Message in = exchange.getIn();
        in.setHeader("CamelFhir.resourceClass", resourceType);

        in.setHeader("CamelFhir.stringId", id);
        in.setHeader("CamelFhir.version", version);

        final FhirResourceId body = in.getBody(FhirResourceId.class);
        if (body != null) {
            if (body.getComplexId() != null) {
                in.setHeader("CamelFhir.id", body.getComplexId());
            } else if (ObjectHelper.isNotEmpty(body.getId())) {
                in.setHeader("CamelFhir.stringId", body.getId());
                in.setHeader("CamelFhir.version", body.getVersion());
            }
        }
    }
}
