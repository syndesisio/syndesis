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
package io.syndesis.dv.openshift;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ProtocolType {
    @JsonProperty("odata") ODATA(8080,8080),
    @JsonProperty("jdbc") JDBC(31000,31000),
    @JsonProperty("pg") PG(35432,5432),
    @JsonProperty("jolokia") JOLOKIA(8778,8778),
    @JsonProperty("prometheus") PROMETHEUS(9779,9779);

    private int sourcePort;
    private int targetPort;

    private ProtocolType(int sourcePort, int targetPort) {
        this.sourcePort = sourcePort;
        this.targetPort = targetPort;
    }

    public String id() {
        return this.name().toLowerCase();
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public int getTargetPort() {
        return targetPort;
    }

}
