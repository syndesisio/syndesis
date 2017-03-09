package com.redhat.ipaas.openshift;

public interface OpenShiftService {

    void createOpenShiftResources(String name, String gitRepo, String webhookSecret);

}
