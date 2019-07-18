/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.systests;

import org.arquillian.cube.kubernetes.annotations.PortForward;
import org.arquillian.cube.openshift.impl.requirement.RequiresOpenshift;
import org.arquillian.cube.requirement.ArquillianConditionalRunner;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;

import javax.inject.Named;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Ignore("#12: SyndesisRestTest is unreliable")
@RunWith(ArquillianConditionalRunner.class)
@RequiresOpenshift
public class SyndesisRestTest {

    @ArquillianResource
    @Named("syndesis-server")
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
