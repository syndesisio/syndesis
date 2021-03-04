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
package io.syndesis.connector.odata2;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.client.api.ODataClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ODataUtilTests extends AbstractODataTest {

    private static Edm testMetadata;

    @BeforeAll
    public static void setup() throws IOException, EdmException, EntityProviderException {
        testMetadata = ODataUtil.readEdm(odataTestServer.getServiceUri(), Collections.emptyMap());
    }

    public static Iterable<Object[]> testData() {
        return Arrays.asList(
            new Object[] { DRIVERS, "1", "(1)" },
            new Object[] { DRIVERS, "(1)", "(1)" },
            new Object[] { DRIVERS, "(Id=1)", "(Id=1)" },
            new Object[] { DRIVERS, "Id=1", "(Id=1)" },
            new Object[] { DRIVERS, "'Id'=1", "(Id=1)" },
            new Object[] { MANUFACTURERS, "'1'", "('1')" },
            new Object[] { MANUFACTURERS, "('1')", "('1')" },
            new Object[] { MANUFACTURERS, "(Id='1')", "(Id='1')" },
            new Object[] { MANUFACTURERS, "Id='1'", "(Id='1')" },
            new Object[] { MANUFACTURERS, "'Id'='1'", "(Id='1')" },
            new Object[] { MANUFACTURERS, "ABC", "('ABC')" },
            new Object[] { MANUFACTURERS, "(ABC)", "('ABC')" },
            new Object[] { MANUFACTURERS, "('ABC')", "('ABC')" },
            new Object[] { DRIVERS, "1/Name", "(1)/Name" },
            new Object[] { DRIVERS, "(1)/Name", "(1)/Name" },
            new Object[] { MANUFACTURERS, "'1'/Address", "('1')/Address" },
            new Object[] { MANUFACTURERS, "'ABC'/Address", "('ABC')/Address" },
            new Object[] { MANUFACTURERS, "ABC/Address", "('ABC')/Address" },
            new Object[] { MANUFACTURERS, "('1')/Address", "('1')/Address" },
            new Object[] { MANUFACTURERS, "('ABC')/Address", "('ABC')/Address" },
            new Object[] { MANUFACTURERS, "(ABC)/Address", "('ABC')/Address" },
            new Object[] { MANUFACTURERS, "'1'/Address/Street", "('1')/Address/Street" },
            new Object[] { MANUFACTURERS, "'ABC'/Address/Street", "('ABC')/Address/Street" },
            new Object[] { MANUFACTURERS, "ABC/Address/Street", "('ABC')/Address/Street" }
        );
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testFormattingKeyPredicate(String resource, String data, String expected) {
        final String result = ODataUtil.formatKeyPredicate(data);
        assertEquals(expected, result);

        assertThatCode(() -> {
            ODataClient.newInstance().parseUri(testMetadata, resourcePath(resource, result));
        }).doesNotThrowAnyException();
    }

    private static String resourcePath(String resource, String keyPredicate) {
        return resource + keyPredicate;
    }
}
