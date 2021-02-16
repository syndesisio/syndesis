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
package io.syndesis.connector.odata2.meta;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.syndesis.connector.odata2.AbstractODataTest;
import io.syndesis.connector.odata2.server.Certificates;
import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ODataMetaDataTest extends AbstractODataTest {

    @BeforeEach
    public void setup() throws Exception {
        context = new DefaultCamelContext();
        context.disableJMX();
        context.start();
    }

    @AfterEach
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
        parameters.put(SERVICE_URI, odataTestServer.getServiceUri());

        Optional<MetaDataExtension.MetaData> meta = extension.meta(parameters);
        assertThat(meta).isPresent();

        Object payload = meta.get().getPayload();
        assertThat(payload).isInstanceOf(ODataMetadata.class);
        ODataMetadata odataMetadata = (ODataMetadata) payload;
        assertThat(odataMetadata.getEntityNames().size()).isEqualTo(3);
        assertThat(odataMetadata.getEntityNames()).contains(MANUFACTURERS, CARS, DRIVERS);
    }

    /**
     * Needs to supply server certificate since the server is unknown to the default
     * certificate authorities that is loaded into the keystore by default
     */
    @Test
    public void testMetaDataExtensionRetrievalSSL() throws Exception {
        ODataMetaDataExtension extension = new ODataMetaDataExtension(context);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(SERVICE_URI, sslTestServer.getSecuredServiceUri());
        parameters.put(SERVER_CERTIFICATE, Certificates.TEST_SERVICE.get());

        Optional<MetaDataExtension.MetaData> meta = extension.meta(parameters);
        assertThat(meta).isPresent();

        Object payload = meta.get().getPayload();
        assertThat(payload).isInstanceOf(ODataMetadata.class);
        ODataMetadata odataMetadata = (ODataMetadata) payload;
        assertThat(odataMetadata.getEntityNames().size()).isEqualTo(3);
        assertThat(odataMetadata.getEntityNames()).contains(MANUFACTURERS, CARS, DRIVERS);
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
        parameters.put(SERVICE_URI, odataTestServer.getServiceUri());
        parameters.put(RESOURCE_PATH, MANUFACTURERS);
        Optional<MetaDataExtension.MetaData> meta = extension.meta(parameters);
        assertThat(meta).isPresent();

        Object payload = meta.get().getPayload();
        assertThat(payload).isInstanceOf(ODataMetadata.class);
        ODataMetadata odataMetadata = (ODataMetadata) payload;
        assertThat(odataMetadata.getEntityNames().size()).isEqualTo(3);
        assertThat(odataMetadata.getEntityNames()).contains(MANUFACTURERS, CARS, DRIVERS);

        assertThat(odataMetadata.getEntityProperties()).hasSize(4);
        assertThat(odataMetadata.getEntityProperties()).anyMatch(pm -> pm.getName().equals("Id"));
        assertThat(odataMetadata.getEntityProperties()).anyMatch(pm -> pm.getName().equals("Name"));
        assertThat(odataMetadata.getEntityProperties()).anyMatch(pm -> pm.getName().equals("Founded"));
        assertThat(odataMetadata.getEntityProperties()).anyMatch(pm -> {
            try {
                assertThat(pm.getChildProperties()).hasSize(4);
                assertThat(pm.getChildProperties()).anyMatch(spm -> spm.getName().equals("Street"));
                assertThat(pm.getChildProperties()).anyMatch(spm -> spm.getName().equals("City"));
                assertThat(pm.getChildProperties()).anyMatch(spm -> spm.getName().equals("ZipCode"));
                assertThat(pm.getChildProperties()).anyMatch(spm -> spm.getName().equals("Country"));
            } catch (AssertionError e) {
                return false;
            }
            return pm.getName().equals("Address");
        });
    }

}
