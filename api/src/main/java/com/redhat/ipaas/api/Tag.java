package com.redhat.ipaas.api;

import java.io.Serializable;
import java.util.Set;

public class Tag implements Serializable {

    private static final long serialVersionUID = 3710243139895513231L;
    String id;
    String name;
    Set<IntegrationTemplate> integrationTemplate;
    Set<Connection> connections;
}
