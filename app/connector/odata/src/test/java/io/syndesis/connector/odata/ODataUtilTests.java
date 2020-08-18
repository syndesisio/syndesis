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

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Supplier;

import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.EdmMetadataRequest;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.http.HttpClientFactory;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ODataUtilTests extends AbstractODataTest {

    @Parameter(0)
    public Supplier<String> serviceUri;
    @Parameter(1)
    public Supplier<String> resource;
    @Parameter(2)
    public String data;
    @Parameter(3)
    public String expected;

    private static Edm testMetadata;
    private static Edm refMetadata;

    @BeforeClass
    public static void setup() {
        testMetadata = requestEdm(defaultTestServer.servicePlainUri());
        refMetadata = requestEdm(REF_SERVICE_URI);
    }

    @Parameters
    public static Iterable<Object[]> testData() {
        Supplier<String> testUriSupplier = () -> defaultTestServer.servicePlainUri();
        Supplier<String> testResPathSupplier = () -> defaultTestServer.resourcePath();

        Supplier<String> refUriSupplier = () -> REF_SERVICE_URI;
        Supplier<String> refResPathSupplier = () -> "Airports";

        return Arrays.asList(
            new Object[] { testUriSupplier, testResPathSupplier, Integer.toString(1),
                OPEN_BRACKET + 1 + CLOSE_BRACKET },
            new Object[] { testUriSupplier, testResPathSupplier,
                OPEN_BRACKET + 1 + CLOSE_BRACKET,
                OPEN_BRACKET + 1 + CLOSE_BRACKET },
            new Object[] { testUriSupplier, testResPathSupplier, "ID=1", "(ID=1)" },
            new Object[] { testUriSupplier, testResPathSupplier, "'ID'='1'", "(ID=1)" },
            new Object[] { testUriSupplier, testResPathSupplier, "'ID'=1", "(ID=1)" },
            new Object[] { testUriSupplier, testResPathSupplier, "ID='1'", "(ID=1)" },
            new Object[] { refUriSupplier, refResPathSupplier, "KSFO", "('KSFO')" },
            new Object[] { refUriSupplier, refResPathSupplier, "(KSFO)", "('KSFO')" },
            new Object[] { refUriSupplier, refResPathSupplier, "(KSFO)", "('KSFO')" },
            new Object[] { refUriSupplier, refResPathSupplier, "KSFO", "('KSFO')" },
            new Object[] { refUriSupplier, refResPathSupplier, "KSFO/Location", "('KSFO')/Location" },
            new Object[] { refUriSupplier, refResPathSupplier, "'KSFO'/Location", "('KSFO')/Location" },
            new Object[] { refUriSupplier, refResPathSupplier, "(KSFO)/Location", "('KSFO')/Location" },
            new Object[] { refUriSupplier, refResPathSupplier, "('KSFO')/Location", "('KSFO')/Location" },
            new Object[] { refUriSupplier, refResPathSupplier, "IcaoCode='KSFO'", "(IcaoCode='KSFO')" },
            new Object[] { refUriSupplier, refResPathSupplier, "'IcaoCode'='KSFO'", "(IcaoCode='KSFO')" },
            new Object[] { refUriSupplier, refResPathSupplier, "'IcaoCode'=KSFO", "(IcaoCode='KSFO')" },
            new Object[] { refUriSupplier, refResPathSupplier, "IcaoCode=KSFO", "(IcaoCode='KSFO')" },
            new Object[] { refUriSupplier, refResPathSupplier, "IcaoCode=KSFO/Location", "(IcaoCode='KSFO')/Location" }
        );
    }

    private static Edm requestEdm(String serviceUri) {
        ODataClient client = ODataClientFactory.getClient();
        HttpClientFactory factory = ODataUtil.newHttpFactory(new HashMap<>());
        client.getConfiguration().setHttpClientFactory(factory);

        EdmMetadataRequest request = client.getRetrieveRequestFactory().getMetadataRequest(serviceUri);
        ODataRetrieveResponse<Edm> response = request.execute();

        if (response.getStatusCode() != 200) {
            throw new IllegalStateException("Metadata response failure. Return code: " + response.getStatusCode());
        }

        return response.getBody();
    }

    @Test
    public void testFormattingKeyPredicate() {
        Parser testParser = new Parser(testMetadata, OData.newInstance());
        Parser refParser = new Parser(refMetadata, OData.newInstance());

        final String result = ODataUtil.formatKeyPredicate(data, true);
        assertEquals(expected, result);

        final Parser parser = serviceUri.get().equals(REF_SERVICE_URI) ? refParser : testParser;
        assertThatCode(() -> {
            parser.parseUri(resourcePath(result), null, null, serviceUri.get());
        }).doesNotThrowAnyException();
    }

    private String resourcePath(String keyPredicate) {
        return resource.get() + keyPredicate;
    }
}
