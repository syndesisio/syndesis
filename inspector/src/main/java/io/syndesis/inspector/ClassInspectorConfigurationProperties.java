/*
 * Copyright (C) 2016 Red Hat, Inc.
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
package io.syndesis.inspector;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("inspector")
public class ClassInspectorConfigurationProperties {

    private static final String DEFAULT_INSPECTOR_HOST = "syndesis-datamapper";

    private String host = DEFAULT_INSPECTOR_HOST;
    private int port = 80;
    private boolean strict = true;

    public ClassInspectorConfigurationProperties() {
    }

    public ClassInspectorConfigurationProperties(String host, int port, boolean strict) {
        this.host = host;
        this.port = port;
        this.strict = strict;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }
}
