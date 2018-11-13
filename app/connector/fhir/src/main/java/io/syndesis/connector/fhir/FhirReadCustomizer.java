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

import java.util.Map;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.fhir.internal.FhirApiCollection;
import org.apache.camel.component.fhir.internal.FhirReadApiMethod;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class FhirReadCustomizer implements ComponentProxyCustomizer {

    private String id;
    private String resourceType;
    private String version;
    private FhirContext fhirContext;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        String fhirVersion = (String) options.get("fhirVersion");
        FhirVersionEnum fhirVersionEnum = FhirVersionEnum.valueOf(fhirVersion);
        this.fhirContext = new FhirContext(fhirVersionEnum);
        setApiMethod(options);
        component.setBeforeProducer(this::beforeProducer);
        component.setAfterProducer(this::afterProducer);
    }

    private void setApiMethod(Map<String, Object> options) {
        id = (String) options.get("id");
        resourceType = (String) options.get("resourceType");
        version = (String) options.get("version");
        options.put("apiName", FhirApiCollection.getCollection().getApiName(FhirReadApiMethod.class).getName());
        options.put("methodName", "resourceById");
    }

    private void afterProducer(Exchange exchange) {
        Message in = exchange.getIn();
        IBaseResource body = in.getBody(IBaseResource.class);
        if (body == null) {
            return;
        }
        String s = fhirContext.newXmlParser().encodeResourceToString(body);
        in.setBody(s);
    }

    private void beforeProducer(Exchange exchange) {
        final Message in = exchange.getIn();
        in.setHeader("CamelFhir.resourceClass", resourceType);

        final FhirReadMessageModel read = exchange.getIn().getBody(FhirReadMessageModel.class);
        if (read != null) {
            if (read.getComplexId() != null) {
                in.setHeader("CamelFhir.id", read.getComplexId());
            } else {
                in.setHeader("CamelFhir.stringId", read.getId());
                in.setHeader("CamelFhir.version", read.getVersion());
            }
        } else {
            in.setHeader("CamelFhir.stringId", id);
            in.setHeader("CamelFhir.version", version);
        }
    }
}
