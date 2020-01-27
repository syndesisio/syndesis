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
package io.syndesis.connector.kafka.model.crd;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Address implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String host;

    private final int port;

    @JsonCreator
    public Address(@JsonProperty("host") final String host, @JsonProperty("port") final int port) {
        this.host = Objects.requireNonNull(host, "host");
        this.port = port;
    }

    @Override
    public boolean equals(final Object another) {
        if (!(another instanceof Address)) {
            return false;
        }

        final Address other = (Address) another;

        return Objects.equals(host, other.host)
            && port == other.port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public String toString() {
        return "{host=" + host + ",port=" + port + "}";
    }
}
