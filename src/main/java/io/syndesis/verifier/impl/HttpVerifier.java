package io.syndesis.verifier.impl;

import org.springframework.stereotype.Component;

/**
 * @author roland
 * @since 28/03/2017
 */
@Component("http")
public class HttpVerifier extends BaseVerifier {
    protected String getConnectorAction() {
        return "http-get";
    }
}
