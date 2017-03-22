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
package com.redhat.ipaas.connector;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.ComponentVerifier;
import org.apache.camel.VerifiableComponent;
import org.apache.camel.impl.DefaultCamelContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConnectorVerifier {

    public static void main(String[] args) throws Exception {
        ConnectorVerifier check = new ConnectorVerifier();

        // Replace the stdout stream so that only we output to it..
        PrintStream originalOut = System.out;
        System.setOut(System.err);

        Properties request = toProperties(System.in);
        Properties response = check.verify(args[0], request);
        response.store(originalOut, null);

        System.exit(0);
    }

    private static Properties toProperties(InputStream inputStream) throws IOException {
        Properties result = new Properties();
        result.load(inputStream);
        return result;
    }

    static private void copy(InputStream is, OutputStream out) throws IOException {
        int c;
        while ((c = is.read()) >= 0) {
            out.write(c);
        }
    }


    public Properties verify(String component, Properties request) throws Exception {
        Properties result = new Properties();
        CamelContext camel = null;
        try {
            // need to create Camel
            camel = new DefaultCamelContext();
            camel.start();

            // get the connector to use
            Component get = camel.getComponent(component);

            // the connector must support ping check if its verifiable
            if (get instanceof VerifiableComponent) {
                VerifiableComponent vc = (VerifiableComponent) get;

                ComponentVerifier verifier = vc.getVerifier();

                Map<String, Object> parameters = toMap(request);
                ComponentVerifier.Result verificationResult = verifier.verify(ComponentVerifier.Scope.CONNECTIVITY, parameters);

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
                        StringBuilder message = new StringBuilder();
                        for (ComponentVerifier.Error error : verificationResult.getErrors()) {
                            message.append("" + error.getCode() + ":" + error.getDescription());
                        }
                        result.put("error", message);
                        break;
                }


            } else {
                result.put("value", "unsupported");
            }
            camel.stop();

        } catch (Exception e) {
            e.printStackTrace();
            result.put("value", "error");
            result.put("error", "System Error, try again later");
        }
        return result;
    }

    private Map<String, Object> toMap(Properties props) throws Exception {
        Map<String, Object> answer = new HashMap<>();
        Enumeration en = props.propertyNames();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            Object value = props.getProperty(key);
            answer.put(key, value);
        }
        return answer;
    }
}
