package com.redhat.ipaas.api;

import java.io.Serializable;

public class IntegrationTemplateConnectionStep implements Serializable{
   
    private static final long serialVersionUID = -5781903473692657024L;
    String id;
    IntegrationTemplate integrationTemplate;
    Connection connection;
    IntegrationTemplateConnectionStep previousStep;
    IntegrationTemplateConnectionStep nextStep;
}
