/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.ipaas.component;

import org.apache.camel.Endpoint;
import org.apache.camel.spi.Metadata;

@Metadata(label = "verifiers", enums = "PARAMETERS,CONNECTIVITY")
public class HttpComponent extends org.apache.camel.component.http4.HttpComponent {

    @Metadata(label = "producer", enums = "http,https", defaultValue = "http", required = "true", description = "To use either HTTP or HTTPS")
    private String scheme;
    @Metadata(label = "producer", required = "true", description = "The hostname of the HTTP server")
    private String hostname;
    @Metadata(label = "producer", description = "The port number of the HTTP server")
    private Integer port;
    @Metadata(label = "producer", description = "The context-path")
    private String path;

    @Override
    public Endpoint createEndpoint(String uri) throws Exception {
        // build together from component level and uri which has the additional context path only
        String build = scheme + "://" + hostname;
        if (port != null) {
            build += ":" + port;
        }
        if (path != null) {
            build += "/" + path;
        }

        // remaining is to be appending
        if (uri != null) {
            build += "/" + uri;
        }

        // TODO: leading path problems, eg double path

        return super.createEndpoint(build);
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
