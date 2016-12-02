package com.redhat.ipaas.api;

import java.io.Serializable;

public class IntegrationRuntime implements Serializable {
    
    private static final long serialVersionUID = 1055380626430029862L;
    String id;
    String state;
    Integration integration;
    Environment environment;
}
