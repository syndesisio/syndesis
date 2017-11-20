/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.syndesis.inspector;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import com.google.common.io.Resources;
import io.fabric8.mockwebserver.DefaultMockServer;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

public class DataMapperClassInspectorTest {

    @Rule
    public InfinispanCache infinispan = new InfinispanCache();

    private DefaultMockServer mockServer = new DefaultMockServer();

    private ClassInspectorConfigurationProperties config = new ClassInspectorConfigurationProperties(mockServer.getHostName(), mockServer.getPort(), false);

    @Test
    public void shouldHandleNesting() throws IOException {
        DataMapperClassInspector dataMapperClassInspector = new DataMapperClassInspector(infinispan.getCaches(), new RestTemplate(), config);

        mockServer.expect().get().withPath("/v2/atlas/java/class?className=twitter4j.StatusJSONImpl").andReturn(200, Resources.toString(getClass().getResource("/twitter4j.StatusJSONImpl.json"), Charset.defaultCharset())).always();
        mockServer.expect().get().withPath("/v2/atlas/java/class?className=twitter4j.Logger").andReturn(200, Resources.toString(getClass().getResource("/twitter4j.Logger.json"), Charset.defaultCharset())).always();
        mockServer.expect().get().withPath("/v2/atlas/java/class?className=twitter4j.LoggerFactory").andReturn(200, Resources.toString(getClass().getResource("/twitter4j.LoggerFactory.json"), Charset.defaultCharset())).always();
        List<String> paths = dataMapperClassInspector.getPaths("java", "twitter4j.StatusJSONImpl", null, null);

        Assert.assertNotNull(paths);
        Assert.assertTrue(paths.contains("id"));
        Assert.assertTrue(paths.contains("logger.infoEnabled"));
    }

    @Test
    public void shouldHandleInterfaces() throws IOException {
        DataMapperClassInspector dataMapperClassInspector = new DataMapperClassInspector(infinispan.getCaches(), new RestTemplate(), config);
        mockServer.expect().get().withPath("/v2/atlas/java/class?className=twitter4j.Status").andReturn(200, Resources.toString(getClass().getResource("/twitter4j.Status.json"), Charset.defaultCharset())).always();

        List<String> paths = dataMapperClassInspector.getPaths("java", "twitter4j.Status", null, null);

        Assert.assertNotNull(paths);
        Assert.assertTrue(paths.contains("id"));
    }

    @Test
    public void shouldExtractClassName() {
        Assert.assertEquals("Status", DataMapperClassInspector.getClassName("Status"));
        Assert.assertEquals("Status", DataMapperClassInspector.getClassName("twitter4j.Status"));
        Assert.assertEquals("Status", DataMapperClassInspector.getClassName("more.twitter4j.Status"));
    }
}
