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
 * Camel HTTP POST connector
 */
public class HttpPostComponent extends DefaultConnectorComponent {
    
    public HttpPostComponent() {
        super("http-post", "com.redhat.ipaas.connector.http.HttpPostComponent");
    }

    @Override
    public void addConnectorOption(Map<String, String> options, String name, String value) {
        if (name.equals("httpUri")) {
            // need to remove any http:// prefix from the http uri option as http4 component wont expect this
            value = value.replaceFirst("^https?:(?://)?", "");
        }

        super.addConnectorOption(options, name, value);
    }

}
