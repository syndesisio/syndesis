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
package io.syndesis.example;

import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.component.extension.ComponentVerifierExtension;
import org.apache.camel.impl.DefaultCamelContext;

public class HttpPingCheck {

    public void ping() throws Exception {
        // need to create Camel
        CamelContext camel = new DefaultCamelContext();

        // get the connector to use
        Component get = camel.getComponent("http-get-connector");
        Optional<ComponentVerifierExtension> ext = get.getExtension(ComponentVerifierExtension.class);

        // the connector must support ping check if its verifiable
        if (ext.isPresent()) {
            ComponentVerifierExtension verifier = ext.get();

            Map<String, Object> parameters = loadParameters();
            ComponentVerifierExtension.Result result = verifier.verify(ComponentVerifierExtension.Scope.CONNECTIVITY, parameters);

            System.out.println("=============================================");
            System.out.println("");
            System.out.println("Ping check result: " + result.getStatus());
            System.out.println("");
            System.out.println("=============================================");

        } else {
            System.out.println("Component does not support ping check");
        }
    }

    /**
     * Helper to load parameters from a .properties file
     */
    private Map<String, Object> loadParameters() throws Exception {
        Properties prop = new Properties();
        prop.load(new FileInputStream("src/main/resources/application.properties"));

        Map<String, Object> answer = new HashMap<>();
        Enumeration<?> en = prop.propertyNames();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            Object value = prop.getProperty(key);
            answer.put(key, value);

        }
        return answer;
    }


}
