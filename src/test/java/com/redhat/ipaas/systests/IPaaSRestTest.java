package com.redhat.ipaas.systests;

import org.arquillian.cube.kubernetes.annotations.PortForward;
import org.arquillian.cube.openshift.impl.requirement.RequiresOpenshift;
import org.arquillian.cube.requirement.ArquillianConditionalRunner;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;

import javax.inject.Named;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@RunWith(ArquillianConditionalRunner.class)
@RequiresOpenshift
public class IPaaSRestTest {

    @ArquillianResource
    @Named("ipaas-rest")
    @PortForward
    URL restUrl;

    @Test
    public void getVersionShouldRespondOk() throws IOException, InterruptedException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().get().url(restUrl).build();
        Response response = client.newCall(request).execute();
        Assert.assertTrue(response.isSuccessful());
    }

}
