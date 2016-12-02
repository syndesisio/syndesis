package com.redhat.ipaas.api;

import java.io.Serializable;
import java.util.Set;

public class Environment implements Serializable {

    private static final long serialVersionUID = -4311560785106816407L;
    String id;
    String name;
    EnvironmentType environmentType;
    Set<Organization> organization;
    
}
