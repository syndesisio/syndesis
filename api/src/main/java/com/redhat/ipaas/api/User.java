package com.redhat.ipaas.api;

import java.io.Serializable;
import java.util.Set;

public class User implements Serializable{
    
    private static final long serialVersionUID = -8963536197984599474L;
    String id;
    String name;
    String kind;
    Set<Integration> integrations;
    
    static User getHardcodedUser() {
    	User user = new User();
    	user.id = "1";
    	user.name = "Clint Eastwood";
    	user.kind = "UsuallyNot";
    	return user;
    }
}
