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
package io.syndesis.connector.soap.cxf.auth;

public enum AuthenticationType {

    NONE("none"),
    BASIC("basic"),
    WSSE_UT("ws-security-ut");

    private final String value;

    AuthenticationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AuthenticationType fromValue(String value) {
        for (AuthenticationType type : AuthenticationType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Illegal authentication type: " + value);
    }
}
