package com.redhat.ipaas.api;

import java.io.Serializable;

public class Component implements Serializable {

    private static final long serialVersionUID = -4372417241895695792L;
    String id;
    String name;
    String icon;
    String properties;
    ComponentGroup componentGroup;
}
