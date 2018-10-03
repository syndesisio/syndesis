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
package io.syndesis.connector.servicenow.customizers;

import java.util.Map;

import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.component.servicenow.ServiceNowConstants;
import org.apache.camel.component.servicenow.model.ImportSetResult;

public class ServiceNowImportSetCustomizer implements ComponentProxyCustomizer {
    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> properties) {
        component.setBeforeProducer(this::beforeProducer);
    }

    //
    // we need to set heades with the support of a customizer as there's
    // no yet a way to mark a property as header value.
    //
    // https://github.com/syndesisio/syndesis/issues/2819
    //
    private void beforeProducer(final Exchange exchange) {
        exchange.getIn().setHeader(ServiceNowConstants.RESOURCE, ServiceNowConstants.RESOURCE_IMPORT);
        exchange.getIn().setHeader(ServiceNowConstants.ACTION, ServiceNowConstants.ACTION_CREATE);
        exchange.getIn().setHeader(ServiceNowConstants.RETRIEVE_TARGET_RECORD, false);
        exchange.getIn().setHeader(ServiceNowConstants.REQUEST_MODEL, String.class);
        exchange.getIn().setHeader(ServiceNowConstants.RESPONSE_MODEL, ImportSetResult.class);
    }
}
