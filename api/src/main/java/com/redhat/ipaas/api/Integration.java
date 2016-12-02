package com.redhat.ipaas.api;

import java.io.Serializable;
import java.util.Set;

public class Integration implements Serializable{
   
    private static final long serialVersionUID = -1557934137547343303L;
    String id;
    String name;
    String configuration;
    IntegrationTemplate integrationTemplate;
    Set<User> users;
    Set<Tag> tags;
}
