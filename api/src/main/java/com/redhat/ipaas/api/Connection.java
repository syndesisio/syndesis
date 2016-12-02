package com.redhat.ipaas.api;

import java.io.Serializable;
import java.util.Set;

public class Connection implements Serializable {

    private static final long serialVersionUID = -1860337496976921351L;
    String id;
    String name;
    Organization organization;
    Component component;
    String configurationProperties;
    String icon;
    String description;
    String position;
    Set<Tag> tags;
}
