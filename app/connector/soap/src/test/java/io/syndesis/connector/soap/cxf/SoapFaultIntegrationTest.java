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
package io.syndesis.connector.soap.cxf;

public class SoapFaultIntegrationTest extends BasicAuthIntegrationTest {

    private static final String ERROR_PAYLOAD =
        "<soap:Fault>" +
            "<faultcode>soap:Server</faultcode>" +
            "<faultstring>Internal Server Error</faultstring>" +
            "<detail>" +
                "<ns1:sayHiError xmlns:ns1=\"http://camel.apache.org/cxf/wsrm\">" +
                    "<error xmlns=\"http://camel.apache.org/cxf/wsrm/\">Hello Error!!!</error>" +
                "</ns1:sayHiError>" +
            "</detail>" +
        "</soap:Fault>";

    @Override
    protected String getResponsePayload() {
        return ERROR_PAYLOAD;
    }
}
