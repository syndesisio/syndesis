package com.redhat.ipaas.openshift;

public class OpenShiftServiceNoOp implements OpenShiftService {

    @Override
    public void createOpenShiftResources(String name, String gitRepo, String webhookSecret) {
        // Empty no-op just for testing
    }

}
