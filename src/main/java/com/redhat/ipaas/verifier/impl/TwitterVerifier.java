package com.redhat.ipaas.verifier.impl;

import org.springframework.stereotype.Component;

/**
 * @author roland
 * @since 28/03/2017
 */
@Component("twitter")
public class TwitterVerifier extends BaseVerifier {

    protected String getConnectorAction() {
        return "twitter-mention";
    }
}
