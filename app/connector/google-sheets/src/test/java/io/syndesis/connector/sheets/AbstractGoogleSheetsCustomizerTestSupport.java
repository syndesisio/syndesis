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

package io.syndesis.connector.sheets;

import java.util.UUID;

import io.syndesis.integration.component.proxy.ComponentProxyComponent;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;

public abstract class AbstractGoogleSheetsCustomizerTestSupport {

    private final ComponentProxyComponent component = new ComponentProxyComponent("google-sheets-1", "google-sheets");
    private final String spreadsheetId = UUID.randomUUID().toString();

    protected static CamelContext createCamelContext() {
        final DefaultCamelContext context = new DefaultCamelContext();
        context.disableJMX();
        context.setAutoStartup(false);

        return context;
    }

    /**
     * Gets the test component that is about to be customized.
     */
    public ComponentProxyComponent getComponent() {
        return component;
    }

    /**
     * Gets the test spreadsheetId.
     */
    public String getSpreadsheetId() {
        return spreadsheetId;
    }
}
