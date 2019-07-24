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

import java.util.List;
import java.util.Map;

import org.apache.camel.RuntimeCamelException;
import com.google.common.base.Splitter;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;

import static org.apache.camel.component.jira.JiraConstants.ISSUE_KEY;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_WATCHERS_ADD;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_WATCHERS_REMOVE;

public class WatchersCustomizer implements ComponentProxyCustomizer {

    private String issueKey;
    private List<String> addWatchers;
    private List<String> removeWatchers;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        if (options.get(ISSUE_KEY) != null) {
            issueKey = options.get(ISSUE_KEY).toString();
        }

        try {
            addWatchers = ConnectorOptions.extractOptionAndMap(options, "addWatchers",
                (String watchers) -> Splitter.on(",").omitEmptyStrings().trimResults().splitToList(watchers));

            removeWatchers = ConnectorOptions.extractOptionAndMap(options, "removeWatchers",
                (String watchers) -> Splitter.on(",").omitEmptyStrings().trimResults().splitToList(watchers));
        } catch (Exception ex) {
            throw new RuntimeCamelException(ex);
        }

        component.setBeforeProducer(this::beforeProducer);
    }

    private void beforeProducer(Exchange exchange) {
        Map<String, Object> headers = exchange.getIn().getHeaders();
        if (headers.get(ISSUE_KEY) == null) {
            exchange.getIn().setHeader(ISSUE_KEY, issueKey);
        }
        if (headers.get(ISSUE_WATCHERS_ADD) == null) {
            exchange.getIn().setHeader(ISSUE_WATCHERS_ADD, addWatchers);
        }
        if (headers.get(ISSUE_WATCHERS_REMOVE) == null) {
            exchange.getIn().setHeader(ISSUE_WATCHERS_REMOVE, removeWatchers);
        }
    }
}
