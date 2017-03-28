package com.redhat.ipaas.verifier;

import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * @author roland
 * @since 28/03/2017
 */
@Component
public class VerifierRegistry {

    private Map<String, Verifier> verifiers;

    public VerifierRegistry(Map<String, Verifier> verifiers) {
        this.verifiers = verifiers;
    }

    public Verifier getVerifier(String connectorId) {
        return verifiers.get(connectorId);
    }
}
