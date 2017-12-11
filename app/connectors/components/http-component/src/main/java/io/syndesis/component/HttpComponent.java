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
package io.syndesis.component;

import java.util.Optional;

import org.apache.camel.Endpoint;
import org.apache.camel.component.extension.ComponentExtension;
import org.apache.camel.component.extension.ComponentVerifierExtension;
import org.apache.camel.spi.Metadata;
import org.apache.camel.util.FileUtil;
import org.apache.camel.util.StringHelper;

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
    @Metadata(label = "security", description = "Enable usage of global SSL context parameters")
    private boolean useGlobalSslContextParameters;

    public HttpComponent() {

    }

    @Override
    public void doStart() throws Exception {
        // required options
        StringHelper.notEmpty(scheme, "scheme");
        StringHelper.notEmpty(hostname, "hostname");

        super.doStart();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ComponentExtension> Optional<T> getExtension(Class<T> extensionType) {
        Optional<T> extension = super.getExtension(extensionType);

        if (extension.isPresent()) {
            T ext =  extension.get();

            if (ComponentVerifierExtension.class.isInstance(ext)) {
                // hack to add custom parameters to the syndesis custom http
                // component
                final ComponentVerifierExtension verifier = (scope, map) -> {
                    // build parameters into httpUri which is expected in the real
                    String s = map.getOrDefault("scheme", "http").toString();
                    String h = map.getOrDefault("hostname", "").toString();
                    String p = map.getOrDefault("port", "").toString();
                    String c = map.getOrDefault("path", "").toString();

                    String url = buildUrl(s, h, p, c, null);
                    map.put("httpUri", url);

                    return ComponentVerifierExtension.class.cast(ext).verify(scope, map);
                };

                return Optional.of((T)verifier);
            }
        }

        return extension;
    }

    @Override
    public Endpoint createEndpoint(String uri) throws Exception {
        String build = buildUrl(scheme, hostname, port, path, uri);
        return super.createEndpoint(build);
    }

    private static String buildUrl(String scheme, String hostname, Object port, String path, String uri) {
        // build together from component level and given uri that has additional context path to append
        String build = scheme + "://" + hostname;
        if (port != null) {
            build += ":" + port;
        }
        if (path != null) {
            build = FileUtil.stripTrailingSeparator(build);
            build += "/" + path;
        }

        String query = null;
        if (uri != null && uri.contains("?")) {
            query = StringHelper.after(uri, "?");
            uri = StringHelper.before(uri, "?");
            uri = StringHelper.after(uri, "://");
        }

        // remaining is to be appending
        if (uri != null) {
            build = FileUtil.stripTrailingSeparator(build);
            build += "/" + uri;
        }

        if (query != null) {
            build += "?" + query;
        }
        return build;
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

    @Override
    public boolean isUseGlobalSslContextParameters() {
        return super.isUseGlobalSslContextParameters();
    }

    @Override
    public void setUseGlobalSslContextParameters(boolean useGlobalSslContextParameters) {
        super.setUseGlobalSslContextParameters(useGlobalSslContextParameters);
    }
}
