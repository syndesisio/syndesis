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
import org.apache.camel.component.extension.ComponentVerifierExtension.Result;
import org.apache.camel.impl.DefaultCamelContext;

import io.syndesis.connector.sql.SqlConnectorVerifierExtension;

public class SqlStoredPingCheck {

    public void ping() throws Exception {
        // need to create Camel
        CamelContext camel = new DefaultCamelContext();
        camel.start();

        // get the connector to use
        Component sqlstored = camel.getComponent("sql-stored-connector");

        // the connector must support ping check if its verifiable
        Optional<SqlConnectorVerifierExtension> vce = sqlstored.getExtension(SqlConnectorVerifierExtension.class);
        if (vce.isPresent()) {
            ComponentVerifierExtension verifier = vce.get();

            Map<String, Object> parameters = loadParameters();
            
            ComponentVerifierExtension.Result result = verifier.verify(ComponentVerifierExtension.Scope.PARAMETERS, parameters);
            
            System.out.println("=============================================");
            System.out.println("");
            System.out.println("Parameters check result: " + result.getStatus());
            if (result.getStatus().equals(Result.Status.ERROR)) {
                System.out.println(result.getErrors());
            }
            System.out.println("");
            System.out.println("=============================================");
            
            ComponentVerifierExtension.Result result2 = verifier.verify(ComponentVerifierExtension.Scope.CONNECTIVITY, parameters);

            System.out.println("=============================================");
            System.out.println("");
            System.out.println("Ping check result: " + result2.getStatus());
            if (result2.getStatus().equals(Result.Status.ERROR)) {
                System.out.println(result2.getErrors());
            }
            System.out.println("");
            System.out.println("=============================================");

        } else {
            System.out.println("Component does not support ping check");
        }

        camel.stop();
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
            if (key.startsWith("sql-stored-connector.")) {
                String shortKey = key.substring(21, key.length());
                Object value = prop.getProperty(key);
                answer.put(shortKey, value);
            }
        }
        return answer;
    }


}
