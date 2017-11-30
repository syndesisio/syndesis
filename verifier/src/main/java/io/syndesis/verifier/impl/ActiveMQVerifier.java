package io.syndesis.verifier.impl;

import io.syndesis.connector.jms.ActiveMQConnectorVerifierExtension;

import org.springframework.stereotype.Component;

/**
 * Connection verifier for ActiveMQ.
 * @author dhirajsb
 */
@Component("activemq")
public class ActiveMQVerifier extends BaseVerifier {

    public ActiveMQVerifier() {
        super(ActiveMQConnectorVerifierExtension.class);
    }

    @Override
    protected String getConnectorAction() {
        // default to arbitrary action connector for stateless connection verification
        return "activemq-publish-connector";
    }

}
