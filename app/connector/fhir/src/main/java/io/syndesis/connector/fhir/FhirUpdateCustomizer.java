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
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeAware;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.fhir.internal.FhirApiCollection;
import org.apache.camel.component.fhir.internal.FhirUpdateApiMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FhirUpdateCustomizer implements ComponentProxyCustomizer, CamelContextAware, DataShapeAware {

    private static final Logger LOG = LoggerFactory.getLogger(FhirUpdateCustomizer.class);

    private String resource;
    private CamelContext camelContext;
    private DataShape inputDataShape;
    private DataShape outputDataShape;

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Override
    public DataShape getInputDataShape() {
        return inputDataShape;
    }

    @Override
    public void setInputDataShape(DataShape inputDataShape) {
        this.inputDataShape = inputDataShape;
    }

    @Override
    public DataShape getOutputDataShape() {
        return outputDataShape;
    }

    @Override
    public void setOutputDataShape(DataShape outputDataShape) {
        this.outputDataShape = outputDataShape;
    }

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        setApiMethod(options);
        component.setBeforeProducer(this::beforeProducer);
    }

    private void setApiMethod(Map<String, Object> options) {
        resource = (String) options.get("resource");
        options.put("apiName", FhirApiCollection.getCollection().getApiName(FhirUpdateApiMethod.class).getName());
        options.put("methodName", "resource");
    }

    private void beforeProducer(Exchange exchange) {
        final Message in = exchange.getIn();
        final FhirUpdateMessageModel fhirUpdateMessageModel = exchange.getIn().getBody(FhirUpdateMessageModel.class);

        if (fhirUpdateMessageModel != null && fhirUpdateMessageModel.getResource() != null) {
            this.resource = fhirUpdateMessageModel.getResource();
        } else {
            this.resource = in.getBody(String.class);
        }

        LOG.info("Updating resource" + this.resource);

        in.setHeader("CamelFhir.resourceAsString", this.resource);

    }
}
