package com.redhat.ipaas.api;

import java.io.Serializable;

public class IntegrationConnectionStep implements Serializable {

    private static final long serialVersionUID = 2637202281003772023L;
    String id;
    Integration integration;
    Connection connection;
    Step step;
    Step previousStep;
    Step nextStep;
    String type;
}
