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
package io.syndesis.server.inspector;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("inspector")
public class ClassInspectorConfigurationProperties {

    private static final String DEFAULT_INSPECTOR_HOST = "syndesis-atlasmap";
    private static final String DEFAULT_INSPECTOR_PATH = "/v2/atlas/java/class";
    private static final String DEFAULT_CLASSNAME_PARAMETER = "className";

    private String host = DEFAULT_INSPECTOR_HOST;
    private int port = 80;
    private String path = DEFAULT_INSPECTOR_PATH;
    private String classNameParameter = DEFAULT_CLASSNAME_PARAMETER;

    private boolean strict = true;

    public ClassInspectorConfigurationProperties() {
        // behave like a Java bean
    }

    public ClassInspectorConfigurationProperties(String host, int port, boolean strict) {
        this(host, port, DEFAULT_INSPECTOR_PATH, DEFAULT_CLASSNAME_PARAMETER, strict);
    }

    public ClassInspectorConfigurationProperties(String host, int port, String path, String classNameParameter, boolean strict) {
        this.host = host;
        this.port = port;
        this.path = path;
        this.classNameParameter = classNameParameter;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getClassNameParameter() {
        return classNameParameter;
    }

    public void setClassNameParameter(String classNameParameter) {
        this.classNameParameter = classNameParameter;
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }
}
