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
package io.syndesis.connector.jira.customizer;

import java.util.Map;

import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;

import static org.apache.camel.component.jira.JiraConstants.ISSUE_KEY;

public class AttachFileCustomizer implements ComponentProxyCustomizer {

    private String issueKey;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        if (options.get(ISSUE_KEY) != null) {
            issueKey  = options.get(ISSUE_KEY).toString();
        }
        component.setBeforeProducer(this::beforeProducer);
    }

    private void beforeProducer(Exchange exchange) {
        if (exchange.getIn().getHeader(ISSUE_KEY) == null) {
            exchange.getIn().setHeader(ISSUE_KEY, issueKey);
        }
    }
}
