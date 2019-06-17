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
package io.syndesis.connector.odata;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.EdmMetadataRequest;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.http.HttpClientFactory;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.junit.Test;

public class ODataUtilTests extends AbstractODataTest {

    private static class TestData {

        public final String serviceUri;
        public final String resource;
        public final String data;
        public final String expected;

        public TestData(String serviceUri, String resource, String data, String expected) {
            this.serviceUri = serviceUri;
            this.resource = resource;
            this.data = data;
            this.expected = expected;
        }

        public String resourcePath(String keyPredicate) {
            return resource + keyPredicate;
        }
    }

    private static class TestDataList extends ArrayList<TestData> {

        private static final long serialVersionUID = 1L;

        public void add(String serviceUri, String resource, String data, String expected) {
            this.add(new TestData(serviceUri, resource, data, expected));
        }
    }

    private Edm requestEdm(String serviceUri) {
        ODataClient client = ODataClientFactory.getClient();
        HttpClientFactory factory = ODataUtil.newHttpFactory(new HashMap<>());
        client.getConfiguration().setHttpClientFactory(factory);

        EdmMetadataRequest request = client.getRetrieveRequestFactory().getMetadataRequest(serviceUri);
        ODataRetrieveResponse<Edm> response = request.execute();

        if (response.getStatusCode() != 200) {
            throw new IllegalStateException("Metatdata response failure. Return code: " + response.getStatusCode());
        }

        return response.getBody();
    }

    @Test
    public void testFormattingKeyPredicate() {
        String testUri = defaultTestServer.servicePlainUri();
        String testResPath = defaultTestServer.resourcePath();
        String refUri = REF_SERVICE_URI;

        TestDataList testData = new TestDataList();
        testData.add(testUri, testResPath, Integer.toString(1),
                     OPEN_BRACKET + Integer.toString(1) + CLOSE_BRACKET);
        testData.add(testUri, testResPath,
                     OPEN_BRACKET + Integer.toString(1) + CLOSE_BRACKET,
                     OPEN_BRACKET + Integer.toString(1) + CLOSE_BRACKET);
        testData.add(testUri, testResPath, "ID=1", "(ID=1)");
        testData.add(testUri, testResPath, "'ID'='1'", "(ID=1)");
        testData.add(testUri, testResPath, "'ID'=1", "(ID=1)");
        testData.add(testUri, testResPath, "ID='1'", "(ID=1)");
        testData.add(refUri, "Airports", "KSFO", "('KSFO')");
        testData.add(refUri, "Airports", "(KSFO)", "('KSFO')");
        testData.add(refUri, "Airports", "(KSFO)", "('KSFO')");
        testData.add(refUri, "Airports", "KSFO", "('KSFO')");
        testData.add(refUri, "Airports", "KSFO/Location", "('KSFO')/Location");
        testData.add(refUri, "Airports", "'KSFO'/Location", "('KSFO')/Location");
        testData.add(refUri, "Airports", "(KSFO)/Location", "('KSFO')/Location");
        testData.add(refUri, "Airports", "('KSFO')/Location", "('KSFO')/Location");
        testData.add(refUri, "Airports", "IcaoCode='KSFO'", "(IcaoCode='KSFO')");
        testData.add(refUri, "Airports", "'IcaoCode'='KSFO'", "(IcaoCode='KSFO')");
        testData.add(refUri, "Airports", "'IcaoCode'=KSFO", "(IcaoCode='KSFO')");
        testData.add(refUri, "Airports", "IcaoCode=KSFO", "(IcaoCode='KSFO')");
        testData.add(refUri, "Airports", "IcaoCode=KSFO/Location", "(IcaoCode='KSFO')/Location");

        Parser testParser = new Parser(requestEdm(testUri), OData.newInstance());
        Parser refParser = new Parser(requestEdm(refUri), OData.newInstance());

        for (TestData td : testData) {
            final String result = ODataUtil.formatKeyPredicate(td.data, true);
            assertEquals(td.expected, result);

            final Parser parser = td.serviceUri.equals(refUri) ? refParser : testParser;
            assertThatCode(() -> {
                parser.parseUri(td.resourcePath(result), null, null, td.serviceUri);
            }).doesNotThrowAnyException();
        }
    }
}
