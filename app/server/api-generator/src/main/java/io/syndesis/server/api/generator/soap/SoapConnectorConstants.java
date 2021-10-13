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
package io.syndesis.server.api.generator.soap;

/**
 * Connector property names and constant values.
 * These need to match soap.json in soap-connector.
 */
public final class SoapConnectorConstants {

    public static final String DEFAULT_OPERATION_NAME_PROPERTY = "defaultOperationName";
    public static final String DEFAULT_OPERATION_NAMESPACE_PROPERTY = "defaultOperationNamespace";

    public static final String DATA_FORMAT_PROPERTY = "dataFormat";
    public static final String PAYLOAD_FORMAT = "PAYLOAD";

    static final String WSDL_URL_PROPERTY = "wsdlURL";
    static final String SPECIFICATION_PROPERTY = "specification";
    static final String SERVICE_NAME_PROPERTY = "serviceName";
    static final String PORT_NAME_PROPERTY = "portName";
    static final String ADDRESS_PROPERTY = "address";

    static final String USERNAME_PROPERTY = "username";
    static final String PASSWORD_PROPERTY = "password";

    static final String SOAP_VERSION_PROPERTY = "soapVersion";

    static final String SERVICES_PROPERTY = "services";
    static final String PORTS_PROPERTY = "ports";
    static final String ADRESSES_PROPERTY = "addresses";

    private SoapConnectorConstants() {
        // constants
    }
}
