package com.redhat.ipaas.api;

import java.io.Serializable;

public class IntegrationTemplate implements Serializable {

    private static final long serialVersionUID = 588370289207256229L;
    String id;
    String name;
    Organization organization;
    String configuration;
}
