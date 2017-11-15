package io.syndesis.verifier.impl;

import org.springframework.stereotype.Component;

/**
 * @author roland
 * @since 28/03/2017
 */
@Component("salesforce")
public class SalesforceVerifier extends BaseVerifier {
    @Override
    protected String getConnectorAction() {
        return "salesforce";
    }
}
