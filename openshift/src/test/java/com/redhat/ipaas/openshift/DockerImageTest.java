package com.redhat.ipaas.openshift;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DockerImageTest {

    @Test
    public void testDockerImageParsing() throws Exception {
        String data[] = {
            "bla", "bla", "bla", "latest",

            "bla/foo", "bla/foo", "foo", "latest",

            "bla/foo:2.0.0", "bla/foo:2.0.0", "foo", "2.0.0",
        };

        for (int i = 0; i < data.length; i +=4) {
            OpenShiftServiceImpl.DockerImage img = new OpenShiftServiceImpl.DockerImage(data[i]);
            assertEquals(img.getImage(), data[i+1]);
            assertEquals(img.getShortName(), data[i+2]);
            assertEquals(img.getTag(), data[i+3]);
        }
    }

}
