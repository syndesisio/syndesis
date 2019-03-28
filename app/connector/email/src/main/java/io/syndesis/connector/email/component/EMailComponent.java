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
package io.syndesis.connector.email.component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.camel.Component;
import org.apache.camel.Endpoint;
import org.apache.camel.component.mail.MailComponent;
import org.apache.camel.component.mail.MailConfiguration;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.jsse.SSLContextParameters;
import io.syndesis.connector.email.EMailConstants;
import io.syndesis.connector.email.EMailUtil;
import io.syndesis.connector.support.util.PropertyBuilder;
import io.syndesis.integration.component.proxy.ComponentDefinition;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;

public final class EMailComponent extends ComponentProxyComponent implements EMailConstants {

    /**
     * These fields are populated using reflection by the HandlerCustomizer class.
     *
     * The values are resolved then the appropriate setter called, while the original
     * key/value pairs are removed from the options map.
     *
     * Note:
     * Should a property be secret then its raw value is the property placeholder and
     * the resolving process converts it accordingly hence the importance of doing it
     * this way rather than using the options map directly.
     */
    private String protocol;
    private boolean secure;
    private String host;
    private int port = -1;
    private String username;
    private String password;
    private String serverCertificate;
    private boolean unseenOnly;

    // Consumer properties
    private long delay = -1;
    private int maxResults = 5;

    EMailComponent(String componentId, String componentScheme) {
        super(componentId, componentScheme);
    }

    public String getProtocol() {
        //
        // Will convert, for example,  imap to imaps
        // if the protocol has been specified as secure
        //
        if (isSecure() && (protocol != null && !protocol.endsWith("s"))) {
            return protocol + "s";
        }

        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServerCertificate() {
        return serverCertificate;
    }

    public void setServerCertificate(String serverCertificate) {
        this.serverCertificate = serverCertificate;
    }

    public boolean isUnseenOnly() {
        return unseenOnly;
    }

    public void setUnseenOnly(boolean unseenOnly) {
        this.unseenOnly = unseenOnly;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    private Map<String, Object> bundleOptions() {
        PropertyBuilder<Object> builder = new PropertyBuilder<Object>();
        return builder
            .propertyIfNotNull(PROTOCOL, getProtocol())
            .propertyIfNotNull(HOST, getHost())
            .propertyIfNotNull(PORT, getPort())
            .propertyIfNotNull(USER, getUsername())
            .propertyIfNotNull(PASSWORD, getPassword())
            .propertyIfNotNull(SERVER_CERTIFICATE, getServerCertificate())
            .propertyIfNotNull(UNSEEN_ONLY, isUnseenOnly())
            .propertyIfNotNull(MAX_MESSAGES, getMaxResults())
            .build();
    }

    @Override
    protected ComponentDefinition getDefinition() {
        try {
            /*
             * The definition set on construction is the placeholder 'email'
             * so find the underlying defintion based on the specified protocol
             */
            return ComponentDefinition.forScheme(getCatalog(), getProtocol());
        } catch (IOException ex) {
            throw ObjectHelper.wrapRuntimeCamelException(ex);
        }
    }

    @SuppressWarnings("PMD")
    @Override
    protected Optional<Component> createDelegateComponent(ComponentDefinition definition, Map<String, Object> options) throws Exception {
        MailComponent component = new MailComponent(getCamelContext());
        MailConfiguration configuration = new MailConfiguration(getCamelContext());

        if (getProtocol() == null) {
            throw new IllegalStateException("No protocol specified for email component");
        }

        configuration.setProtocol(getProtocol());
        configuration.setHost(getHost());
        configuration.setPort(getPort());
        configuration.setUsername(getUsername());
        configuration.setPassword(getPassword());
        configuration.setUnseen(isUnseenOnly());

        Map<String, Object> resolvedOptions = bundleOptions();
        SSLContextParameters sslContextParameters = EMailUtil.createSSLContextParameters(resolvedOptions);
        if (sslContextParameters != null) {
            configuration.setSslContextParameters(sslContextParameters);
        }

        configuration.setFetchSize(getMaxResults());

        // Decode mime headers like the subject from Quoted-Printable encoding to normal text
        configuration.setMimeDecodeHeaders(true);

        component.setConfiguration(configuration);
        return Optional.of(component);
    }

    @Override
    protected Endpoint createDelegateEndpoint(
                                              ComponentDefinition definition, String scheme, Map<String, String> options)
                                              throws Exception {

        Endpoint endpoint = super.createDelegateEndpoint(getDefinition(), scheme, options);

        Protocols protocol = Protocols.getValueOf(getProtocol())    ;
        if (protocol.isReceiver()) { // only receivers are consumers
            /**
             * Need to apply these consumer properties after the creation
             */
            Map<String, Object> properties = new HashMap<>();

            if (getDelay() > -1) {
                properties.put(CONSUMER + DOT + DELAY, Long.toString(getDelay()));
            }

            if (! properties.isEmpty()) {
                endpoint.configureProperties(properties);
            }
        }

        return endpoint;
    }
}
