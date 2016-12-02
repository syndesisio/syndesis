package com.redhat.ipaas.api;

import java.io.Serializable;
import java.util.Set;

public class Role implements Serializable {
    
    private static final long serialVersionUID = 6858315958004649852L;
    String id;
    String name;
    Set<Permission> permissions;
}
