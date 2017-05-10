package io.syndesis.verifier.impl;

import java.util.Map;

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

    @Override
    protected void customize(Map<String, Object> params) {
        params.put("operationName", "UPSERT_SOBJECT");
    }
}
