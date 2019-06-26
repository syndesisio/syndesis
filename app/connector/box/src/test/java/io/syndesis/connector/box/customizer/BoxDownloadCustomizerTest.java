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
package io.syndesis.connector.box.customizer;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import io.syndesis.connector.box.BoxFile;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;

public class BoxDownloadCustomizerTest extends CamelTestSupport {

    private BoxDownloadCustomizer customizer;
    private ComponentProxyComponent component;

    @Before
    public void setupCustomizer() {
        customizer = new BoxDownloadCustomizer();
        component = new ComponentProxyComponent("box-test", "box");
    }

    @Test
    public void testAfterProducer() throws Exception {
        String id = "12345";
        String content = "Test content: åäö";
        String encoding = StandardCharsets.ISO_8859_1.name();
        int size = content.getBytes(encoding).length;
        Map<String, Object> options = new HashMap<>();
        options.put("fileId", id);
        options.put("encoding", encoding);

        customizer.customize(component, options);

        Exchange inbound = new DefaultExchange(createCamelContext());
        setBody(inbound, content, encoding);
        component.getAfterProducer().process(inbound);

        BoxFile file = inbound.getIn().getBody(BoxFile.class);
        assertNotNull(file);
        assertEquals(id, file.getId());
        assertEquals(content, file.getContent());
        assertEquals(size, file.getSize());
    }

    private void setBody(Exchange inbound, String content, String encoding) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(content.getBytes(encoding));
        inbound.getIn().setBody(output);
    }
}
