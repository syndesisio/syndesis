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
package io.syndesis.connector.amqp;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for creating AMQP ConnectionFactory.
 *
 * @author dhirajsb
 */
public class AMQPUtil {

    private static final Logger LOG = LoggerFactory.getLogger(AMQPUtil.class);

    public static JmsConnectionFactory createConnectionFactory(String connectionUri, String username, String password,
               String brokerCertificate, String clientCertificate, boolean skipCertificateCheck) {

        final JmsConnectionFactory result;
        if (!connectionUri.contains("amqps:")) {
            result = new JmsConnectionFactory(username, password, connectionUri);
        } else {
            if (skipCertificateCheck) {
                if (!connectionUri.contains("transport.trustAll")) {
                    LOG.warn("Skipping Certificate check for AMQP Connection " + connectionUri);
                    connectionUri = connectionUri +
                            (connectionUri.contains("?") ? "&" : "?") +
                            "transport.trustAll=true&transport.verifyHost=false";
                }
                result = new JmsConnectionFactory(username, password, connectionUri);
            } else {
                // TODO add amqps connection support, see CAMEL-11780
                throw new IllegalArgumentException("SSL Not supported, yet!");
            }
        }

        return result;
    }
}
