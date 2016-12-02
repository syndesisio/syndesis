package com.redhat.ipaas.api;

public final class Version {

    private Version() {
    }

    public static String getVersion() {
        return "${project.version}";
    }


}
