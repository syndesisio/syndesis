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
package io.syndesis.connector.email.verifier;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.component.extension.verifier.DefaultComponentVerifierExtension;
import org.apache.camel.component.mail.JavaMailSender;
import org.apache.camel.component.mail.MailConfiguration;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.support.jsse.SSLContextParameters;
import io.syndesis.connector.email.EMailConstants;
import io.syndesis.connector.email.EMailUtil;
import io.syndesis.connector.support.util.ConnectorOptions;

public abstract class AbstractEMailVerifier extends DefaultComponentVerifierExtension implements EMailConstants {

    protected static final long DEFAULT_CONNECTION_TIMEOUT = 5000L;
    public static final String MAIL_PREFIX = "mail.";

    protected AbstractEMailVerifier(String defaultScheme) {
        super(defaultScheme);
    }

    protected AbstractEMailVerifier(String defaultScheme, CamelContext camelContext) {
        super(defaultScheme, camelContext);
    }

    protected AbstractEMailVerifier(String defaultScheme, CamelContext camelContext, Component component) {
        super(defaultScheme, camelContext, component);
    }

    protected void secureProtocol(Map<String, Object> parameters) {
        Protocol protocol = ConnectorOptions.extractOptionAndMap(parameters,
            PROTOCOL, Protocol::getValueOf, null);
        if (ObjectHelper.isEmpty(protocol)) {
            return;
        }

        SecureType secureType = ConnectorOptions.extractOptionAndMap(parameters,
            SECURE_TYPE, SecureType::secureTypeFromId, null);
        if (ObjectHelper.isEmpty(secureType) || protocol.isSecure()) {
            return;
        }

        switch (secureType) {
            case STARTTLS:
                Properties properties = new Properties();
                properties.put(MAIL_PREFIX + protocol + ".starttls.enable", "true");
                properties.put(MAIL_PREFIX + protocol + ".starttls.required", "true");
                parameters.put(ADDITIONAL_MAIL_PROPERTIES, properties);
                break;
            case SSL_TLS:
                parameters.put(PROTOCOL, protocol.toSecureProtocol().id());
                break;
            default:
                // Nothing required
        }
    }

    private static void setJavaMailProperty(MailConfiguration configuration, String key, String value) {
        configuration.getAdditionalJavaMailProperties().setProperty(key, value);
    }

    /**
     * Sets the connection timeout property, eg. mail.imap.connectiontimeout, to 'value' seconds.
     * This is necessary for situations where requests to initiate mail connections have themselves a
     * default timeout, eg. http requests, and therefore this timeout needs to be shortened.
     */
    protected void setConnectionTimeoutProperty(Map<String, Object> parameters,
                                                MailConfiguration configuration, String timeoutValue) {
        Protocol protocol = ConnectorOptions.extractOptionAndMap(parameters,
            PROTOCOL, Protocol::getValueOf, null);
        Protocol plainProtocol = protocol.toPlainProtocol();
        Protocol secureProtocol = protocol.toSecureProtocol();

        setJavaMailProperty(configuration, MAIL_PREFIX + plainProtocol.id() + ".connectiontimeout", timeoutValue);
        setJavaMailProperty(configuration, MAIL_PREFIX + plainProtocol.id() + ".timeout", timeoutValue);
        setJavaMailProperty(configuration, MAIL_PREFIX + secureProtocol.id() + ".connectiontimeout", timeoutValue);
        setJavaMailProperty(configuration, MAIL_PREFIX + secureProtocol.id() + ".timeout", timeoutValue);
    }

    protected MailConfiguration createConfiguration(Map<String, Object> parameters) {
        secureProtocol(parameters);
        SSLContextParameters sslContextParameters = EMailUtil.createSSLContextParameters(parameters);
        parameters.put(SSL_CONTEXT_PARAMETERS, sslContextParameters);

        //
        // setProperties will strip parameters key/values so copy the map
        //
        try {
            MailConfiguration configuration = setProperties(new MailConfiguration(), new HashMap<>(parameters));
            Protocol protocol = ConnectorOptions.extractOptionAndMap(parameters,
                                                                     PROTOCOL, Protocol::getValueOf, null);
            configuration.configureProtocol(protocol.id());
            return configuration;
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to set parameters", e);
        }
    }

    protected JavaMailSender createJavaMailSender(MailConfiguration configuration) throws ReflectiveOperationException {
            Method method = MailConfiguration.class.getDeclaredMethod("createJavaMailSender");
            method.setAccessible(true);
            return (JavaMailSender) method.invoke(configuration);
        }

}
