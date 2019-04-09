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
package io.syndesis.connector.odata.meta;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.syndesis.connector.odata.AbstractODataTest;
import io.syndesis.connector.odata.server.ODataTestServer;

public class ODataMetaDataTest extends AbstractODataTest {

    @Before
    public void setup() throws Exception {
        context = new DefaultCamelContext();
        context.disableJMX();
        context.start();
    }

    @After
    public void tearDown() throws Exception {
        if (context != null) {
            context.stop();
            context = null;
        }
    }

    @Test
    public void testMetaDataExtensionRetrieval() throws Exception {
        ODataMetaDataExtension extension = new ODataMetaDataExtension(context);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(SERVICE_URI, defaultTestServer.servicePlainUri());

        Optional<MetaDataExtension.MetaData> meta = extension.meta(parameters);
        assertThat(meta).isPresent();

        Object payload = meta.get().getPayload();
        assertThat(payload).isInstanceOf(ODataMetadata.class);
        ODataMetadata odataMetadata = (ODataMetadata) payload;
        assertThat(odataMetadata.getEntityNames().size()).isEqualTo(1);
        assertThat(odataMetadata.getEntityNames().iterator().next()).isEqualTo(defaultTestServer.resourcePath());
    }

    /**
     * Needs to supply server certificate since the server is unknown to the default
     * certificate authorities that is loaded into the keystore by default
     */
    @Test
    public void testMetaDataExtensionRetrievalSSL() throws Exception {
        ODataMetaDataExtension extension = new ODataMetaDataExtension(context);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(SERVICE_URI, sslTestServer.serviceSSLUri());
        parameters.put(SERVER_CERTIFICATE, ODataTestServer.serverCertificate());

        Optional<MetaDataExtension.MetaData> meta = extension.meta(parameters);
        assertThat(meta).isPresent();

        Object payload = meta.get().getPayload();
        assertThat(payload).isInstanceOf(ODataMetadata.class);
        ODataMetadata odataMetadata = (ODataMetadata) payload;
        assertThat(odataMetadata.getEntityNames().size()).isEqualTo(1);
        assertThat(odataMetadata.getEntityNames().iterator().next()).isEqualTo(sslTestServer.resourcePath());
    }

    @Test
    public void testMetaDataExtensionOnRealServer() throws Exception {
        ODataMetaDataExtension extension = new ODataMetaDataExtension(context);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(SERVICE_URI, REF_SERVICE_URI);

        Optional<MetaDataExtension.MetaData> meta = extension.meta(parameters);
        assertThat(meta).isPresent();
        assertThat(meta.get().getPayload()).isInstanceOf(ODataMetadata.class);
    }

    @Test
    public void testMetaDataExtensionRetrievalOnceResourcePathSet() throws Exception {
        ODataMetaDataExtension extension = new ODataMetaDataExtension(context);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(SERVICE_URI, defaultTestServer.servicePlainUri());
        parameters.put(RESOURCE_PATH, "Products");
        Optional<MetaDataExtension.MetaData> meta = extension.meta(parameters);
        assertThat(meta).isPresent();

        Object payload = meta.get().getPayload();
        assertThat(payload).isInstanceOf(ODataMetadata.class);
        ODataMetadata odataMetadata = (ODataMetadata) payload;
        assertThat(odataMetadata.getEntityNames().size()).isEqualTo(1);
        assertThat(odataMetadata.getEntityNames().iterator().next()).isEqualTo(defaultTestServer.resourcePath());
    }

}
