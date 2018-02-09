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
package io.syndesis.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.component.extension.ComponentVerifierExtension;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * This CLI app expects:
 *
 *  1. cli arguments: scope camel-component-prefix
 *  2. a property file delivered via stdin
 *
 * The result is in property file format on stdout.
 * Debug info can be found on stderr.
 *
 * We use property file formats for input and output encoding
 * to avoid needing additional dependencies on the classpath
 * like a json parser.
 */
public class ConnectorVerifier {

    public static void main(String[] args) throws IOException {
        ConnectorVerifier check = new ConnectorVerifier();

        // Replace the stdout stream so that only we output to it..
        PrintStream originalOut = System.out;
        System.setOut(System.err);

        Properties request = toProperties(System.in);
        ComponentVerifierExtension.Scope scope = ComponentVerifierExtension.Scope.valueOf(args[0]);
        String componentPrefix = args[1];

        Properties response = check.verify(scope, componentPrefix, request);
        response.store(originalOut, null);
    }

    private static Properties toProperties(InputStream inputStream) throws IOException {
        Properties result = new Properties();
        result.load(inputStream);
        return result;
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    public Properties verify(ComponentVerifierExtension.Scope scope, String component, Properties request) {
        Properties result = new Properties();
        CamelContext camel = null;
        try {
            // need to create Camel
            camel = new DefaultCamelContext();
            camel.start();

            // get the connector to use
            Component get = camel.getComponent(component, true, false);
            Optional<ComponentVerifierExtension> ext = get.getExtension(ComponentVerifierExtension.class);

            if (ext.isPresent()) {
                ComponentVerifierExtension verifier = ext.get();
                Map<String, Object> parameters = toMap(request);
                ComponentVerifierExtension.Result verificationResult = verifier.verify(scope, parameters);

                switch (verificationResult.getStatus()) {
                    case OK:
                        result.put("value", "ok");
                        break;
                    case UNSUPPORTED:
                        result.put("value", "unsupported");
                        break;
                    case ERROR:
                        // TODO: think about how to encode the error messages better.
                        result.put("value", "error");
                        int i = 0;
                        for (ComponentVerifierExtension.VerificationError error : verificationResult.getErrors()) {
                            if (error.getCode() != null) {
                                result.put("error." + i + ".code", error.getCode());
                            }
                            if (error.getDescription() != null) {
                                result.put("error." + i + ".description", error.getDescription());
                            }
                            i++;
                        }
                        break;
                    default:
                        throw new IllegalStateException("Unsupported verification result status: " +verificationResult.getStatus());
                }
            } else {
                result.put("value", "unsupported");
            }

            camel.stop();

        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
            e.printStackTrace(); // NOPMD
            result.put("value", "error");
            result.put("error", "System Error, try again later");
        }
        return result;
    }

    private Map<String, Object> toMap(Properties props) {
        Map<String, Object> answer = new HashMap<>();
        Enumeration<?> en = props.propertyNames();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            Object value = props.getProperty(key);
            answer.put(key, value);
        }
        return answer;
    }
}
