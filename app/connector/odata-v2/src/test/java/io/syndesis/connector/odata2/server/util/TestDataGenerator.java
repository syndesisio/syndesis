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
package io.syndesis.connector.odata2.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import io.syndesis.connector.odata2.server.ODataTestServer;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

/**
 * Data generator for annotation sample service.
 * <p>
 * Taken from the tutorial:
 * http://olingo.apache.org/doc/odata2/tutorials/OlingoV2BasicClientSample.html
 */
public class TestDataGenerator {
    private static final String HTTP_METHOD_POST = "POST";

    private static final String HTTP_HEADER_CONTENT_TYPE = HttpHeaders.CONTENT_TYPE;
    private static final String HTTP_HEADER_ACCEPT = HttpHeaders.ACCEPT;

    private static final String APPLICATION_JSON = ContentType.APPLICATION_JSON.getMimeType();

    private static final Logger LOG = LoggerFactory.getLogger(TestDataGenerator.class);
    private static final boolean PRINT_RAW_CONTENT = true;

    private TestDataGenerator() {
        // prevent instantiation of utility class
    }

    public static void main(String[] args) {
        String serviceUrl = String.format("http://localhost:8080/%s", ODataTestServer.DEMO_FORMULA_SVC);
        if (args.length == 1) {
            serviceUrl = args[0];
        }
        generateData(serviceUrl);
    }

    public static void generateData(String serviceUrl) {
        print("Generate sample data for service on url: " + serviceUrl);
        String usedFormat = APPLICATION_JSON;

        String manufacturerStar = "{\"Id\":\"1\",\"Name\":\"Star Powered Racing\",\"Founded\":\"/Date(-489024000000+0060)/\"," +
            "\"Address\":{\"Street\":\"Star Street 137\",\"City\":\"Stuttgart\",\"ZipCode\":\"70173\",\"Country\":\"Germany\"}}";
        String manufacturerHorse = "{\"Id\":\"2\",\"Name\":\"Horse Powered Racing\",\"Founded\":\"/Date(-1266278400000+0060)/\"," +
            "\"Address\":{\"Street\":\"Horse Street 1\",\"City\":\"Maranello\",\"ZipCode\":\"41053\",\"Country\":\"Italy\"}}";

        String manufacturersUri = serviceUrl + "/Manufacturers";
        createEntity(manufacturersUri, manufacturerStar, usedFormat);
        createEntity(manufacturersUri, manufacturerHorse, usedFormat);

        String carOneWithInlineDriverOne =
            "{\"Id\":\"1\",\"Model\":\"F1 W02\",\"Price\":\"167189.0\",\"ModelYear\":2011,\"Updated\":\"/Date(1392989833964)/\"," +
                "\"Driver\":{\"Id\":\"1\",\"Name\":\"Mic\",\"Lastname\":\"Shoemaker\",\"Nickname\":\"The Fast\",\"Birthday\":\"/Date(488671200000)/\"}}";
        String carTwoWithInlineDriverTwo =
            "{\"Id\":\"2\",\"Model\":\"F1 W04\",\"Price\":\"242189.99\",\"ModelYear\":2013,\"Updated\":\"/Date(1392990355793)/\"," +
                "\"Driver\":{\"Id\":\"2\",\"Name\":\"Nico\",\"Lastname\":\"Mulemountain\",\"Nickname\":null,\"Birthday\":\"/Date(-31366800000)/\"}}";
        String carThreeWithInlineDriverThree =
            "{\"Id\":\"3\",\"Model\":\"FF2013\",\"Price\":\"199189.11\",\"ModelYear\":2013,\"Updated\":\"/Date(1392990355793)/\"," +
                "\"Driver\":{\"Id\":\"3\",\"Name\":\"Kimi\",\"Lastname\":\"Heikkinen\",\"Nickname\":\"Iceman\",\"Birthday\":\"/Date(308962800000)/\"}}";
        String carFour = "{\"Id\":\"4\",\"Model\":\"FF2014\",\"Price\":\"299189.11\",\"ModelYear\":2014,\"Updated\":\"/Date(1392973616419)/\"}";

        createEntity(manufacturersUri + "('1')/Cars", carOneWithInlineDriverOne, usedFormat);
        createEntity(manufacturersUri + "('1')/Cars", carTwoWithInlineDriverTwo, usedFormat);
        createEntity(manufacturersUri + "('2')/Cars", carThreeWithInlineDriverThree, usedFormat);
        createEntity(manufacturersUri + "('2')/Cars", carFour, usedFormat);
    }

    private static void createEntity(String absoluteUri, String content, String contentType) {
        try {
            writeEntity(absoluteUri, content, contentType);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Exception during data source initialization generation.", e);
        }
    }

    private static void writeEntity(String absoluteUri, String content, String contentType)
        throws IOException, URISyntaxException {

        print(TestDataGenerator.HTTP_METHOD_POST + " request on uri: " + absoluteUri + ":\n  " + content + "\n");
        //
        HttpURLConnection connection = initializeConnection(absoluteUri, contentType);
        byte[] buffer = content.getBytes(StandardCharsets.UTF_8);
        connection.getOutputStream().write(buffer);

        // if a entity is created (via POST request) the response body contains the new created entity
        HttpStatusCodes statusCode = HttpStatusCodes.fromStatusCode(connection.getResponseCode());
        if (statusCode == HttpStatusCodes.CREATED) {
            // get the content as InputStream and de-serialize it into an ODataEntry object
            InputStream responseContent = connection.getInputStream();
            logRawContent(responseContent);
        } else if (statusCode == HttpStatusCodes.NO_CONTENT) {
            print("No content.");
        } else {
            checkStatus(connection);
        }

        //
        connection.disconnect();
    }

    private static void print(String content) {
        LOG.info(content);
    }


    private static void checkStatus(HttpURLConnection connection) throws IOException {
        HttpStatusCodes httpStatusCode = HttpStatusCodes.fromStatusCode(connection.getResponseCode());
        if (400 <= httpStatusCode.getStatusCode() && httpStatusCode.getStatusCode() <= 599) {
            connection.disconnect();
            throw new RuntimeException("Http Connection failed with status " + httpStatusCode.getStatusCode() + " " + httpStatusCode.toString());
        }
    }

    private static void logRawContent(InputStream content) throws IOException {
        if (PRINT_RAW_CONTENT) {
            print(TestDataGenerator.HTTP_METHOD_POST + " response:\n" + StreamUtils.copyToString(content, StandardCharsets.UTF_8) + "\n");
        }
    }


    private static HttpURLConnection initializeConnection(String absoluteUri, String contentType)
        throws IOException {
        URL url = new URL(absoluteUri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(TestDataGenerator.HTTP_METHOD_POST);
        connection.setRequestProperty(HTTP_HEADER_ACCEPT, contentType);
        connection.setDoOutput(true);
        connection.setRequestProperty(HTTP_HEADER_CONTENT_TYPE, contentType);

        return connection;
    }
}
