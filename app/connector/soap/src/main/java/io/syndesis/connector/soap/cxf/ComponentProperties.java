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

public final class ComponentProperties {

    public static final String ADDRESS = "address";
    public static final String SPECIFICATION = "specification";
    public static final String SERVICE_NAME = "serviceName";
    public static final String PORT_NAME = "portName";
    public static final String SOAP_VERSION = "soapVersion";
    public static final String WSDL_URL = "wsdlURL";

    public static final String AUTHENTICATION_TYPE = "authenticationType";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String ADD_TIMESTAMP = "addTimestamp";

    public static final String EXCEPTION_MESSAGE_CAUSE_ENABLED = "exceptionMessageCauseEnabled";

    private ComponentProperties() {
        // singleton
    }
}
