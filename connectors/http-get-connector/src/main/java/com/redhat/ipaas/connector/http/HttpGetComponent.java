/**
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.ipaas.connector.http;

import java.util.Map;

import org.apache.camel.component.connector.DefaultConnectorComponent;

/**
 * Camel HTTP GET connector
 */
public class HttpGetComponent extends DefaultConnectorComponent {
    
    public HttpGetComponent() {
        super("http-get", "com.redhat.ipaas.connector.http.HttpGetComponent");
    }

    @Override
    public void addConnectorOption(Map<String, String> options, String name, String value) {
        if (name.equals("httpUri")) {
            // need to remove any http:// prefix from the http uri option as http4 component wont expect this
            if (value.startsWith("http://")) {
                value = value.substring(7);
            } else if (value.startsWith("http:")) {
                value = value.substring(5);
            }
            if (value.startsWith("https://")) {
                value = value.substring(8);
            } else if (value.startsWith("https:")) {
                value = value.substring(6);
            }
        }

        super.addConnectorOption(options, name, value);
    }

}
