package com.redhat.ipaas.api;

import java.io.Serializable;

public class IntegrationPattern implements Serializable {

    private static final long serialVersionUID = 6691059068932717391L;
    String id;
    String name;
    String icon;
    String properties;
    IntegrationPatternGroup integrationPatternGroup;
}
