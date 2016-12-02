package com.redhat.ipaas.api;

import java.io.Serializable;
import java.util.Set;

public class User implements Serializable{
    
    private static final long serialVersionUID = -8963536197984599474L;
    String id;
    String name;
    String kind;
    Set<Integration> integrations;
}
