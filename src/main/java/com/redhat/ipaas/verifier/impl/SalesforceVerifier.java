package com.redhat.ipaas.verifier.impl;

import com.redhat.ipaas.verifier.impl.BaseVerifier;
import org.springframework.stereotype.Component;

/**
 * @author roland
 * @since 28/03/2017
 */
@Component("salesforce")
public class SalesforceVerifier extends BaseVerifier {
    protected String getConnectorAction() {
        return "salesforce-upsert-contact";
    }
}
