package io.syndesis.systests;

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
import static io.fabric8.kubernetes.assertions.Assertions.assertThat;


@RunWith(ArquillianConditionalRunner.class)
@RequiresOpenshift
public class DeploymentTest {

    @ArquillianResource
    @Named("syndesis-ui")
    DeploymentConfig ui;

    @ArquillianResource
    @Named("syndesis-rest")
    DeploymentConfig rest;

    @ArquillianResource
    @Named("syndesis-keycloak")
    DeploymentConfig keycloak;

    @ArquillianResource
    KubernetesClient client;

    @Test
    public void uiShouldBeReady() {
        Assert.assertTrue(Readiness.isDeploymentConfigReady(ui));
        assertThat(client).replicationController("syndesis-rest").has()
    }

    @Test
    public void restShouldBeReady() {
        Assert.assertTrue(Readiness.isDeploymentConfigReady(rest));
    }

    @Test
    public void keycloakShouldBeReady() {
        Assert.assertTrue(Readiness.isDeploymentConfigReady(keycloak));
    }
}
