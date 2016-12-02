package com.redhat.ipaas.api;

import java.io.Serializable;

public class Step implements Serializable {
   
    private static final long serialVersionUID = 1588083078080824197L;
    String id;
    IntegrationPattern integrationPattern;
    String configuredProperties;
    
}
