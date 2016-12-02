package com.redhat.ipaas.api;

import java.io.Serializable;
import java.util.Set;

public class Organization implements Serializable{
  
    private static final long serialVersionUID = -3312188982109356653L;
    String id;
    String name;
    Set<Environment> environments;
    Set<User> users;
}
