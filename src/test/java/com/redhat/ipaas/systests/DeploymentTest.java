package com.redhat.ipaas.systests;

import org.arquillian.cube.kubernetes.api.Session;
import org.arquillian.cube.openshift.impl.requirement.RequiresOpenshift;
import org.arquillian.cube.requirement.ArquillianConditionalRunner;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Named;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.readiness.Readiness;
import io.fabric8.openshift.api.model.DeploymentConfig;

@RunWith(ArquillianConditionalRunner.class)
@RequiresOpenshift
public class DeploymentTest {

    @ArquillianResource
    @Named("ipaas-ui")
    DeploymentConfig ipassUi;

    @ArquillianResource
    @Named("ipaas-rest")
    DeploymentConfig ipassRest;

    @ArquillianResource
    @Named("ipaas-keycloak")
    DeploymentConfig ipassKeycloak;

    @ArquillianResource
    KubernetesClient client;

    @Test
    public void uiShouldBeReady() {
        Assert.assertTrue(Readiness.isDeploymentConfigReady(ipassUi));
    }

    @Test
    public void restShouldBeReady() {
        Assert.assertTrue(Readiness.isDeploymentConfigReady(ipassRest));
    }

    @Test
    public void keycloakShouldBeReady() {
        Assert.assertTrue(Readiness.isDeploymentConfigReady(ipassKeycloak));
    }
}
