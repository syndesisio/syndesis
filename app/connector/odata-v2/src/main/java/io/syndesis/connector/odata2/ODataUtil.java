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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.connector.support.util.KeyStoreHelper;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;

public class ODataUtil implements ODataConstants {

    private static final Pattern NUMBER_ONLY_PATTERN = Pattern.compile("-?\\d+");

    private static final Pattern KEY_PREDICATE_PATTERN = Pattern.compile("(\\(?'?[^/]+'?\\)?)/(.+)");

    interface ResponseHandler<T> {
        T handle(CloseableHttpResponse response) throws IOException, EdmException, EntityProviderException;
    }

    /**
     * One more method to check if URL is HTTPS or not.
     * @return whether url is an ssl (https) url or not.
     */
    public static boolean isServiceSSL(String url) {
        if (url == null) {
            return false;
        }

        HttpGet httpGet = new HttpGet(url);
        String scheme = httpGet.getURI().getScheme();
        return scheme != null && scheme.equals("https");
    }

    private static KeyStore createKeyStore(Map<String, Object> options) throws GeneralSecurityException, IOException {
        String certContent = ConnectorOptions.extractOption(options, SERVER_CERTIFICATE);
        if (ObjectHelper.isEmpty(certContent)) {
            return KeyStoreHelper.defaultKeyStore();
        }

        return KeyStoreHelper.createKeyStoreWithCustomCertificate("odata2", certContent);
    }

    public static SSLContext createSSLContext(Map<String, Object> options) {
        String serviceUrl = ConnectorOptions.extractOption(options, SERVICE_URI);
        if (!isServiceSSL(serviceUrl)) {
            return null;
        }

        try {
            KeyStore keyStore = createKeyStore(options);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());
            return sslContext;
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalArgumentException("Unable to configure TLS context", e);
        }
    }

    private static CredentialsProvider createCredentialProvider(Map<String, Object> options) {
        String basicUser = ConnectorOptions.extractOption(options, BASIC_USER_NAME);

        if (ObjectHelper.isEmpty(basicUser)) {
            return null;
        }

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        String basicPswd = ConnectorOptions.extractOption(options, BASIC_PASSWORD);
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(basicUser, basicPswd));
        return credentialsProvider;
    }

    /**
     * Creates a new {@link HttpClientBuilder} for the given options.
     * @return the new http client builder
     */
    public static HttpClientBuilder createHttpClientBuilder(Map<String, Object> options) {
        HttpClientBuilder builder = HttpClientBuilder.create();

        SSLContext sslContext = createSSLContext(options);
        if (sslContext != null) {
            // Skip verifying hostname
            HostnameVerifier allowAllHosts = new NoopHostnameVerifier();
            builder.setSSLContext(sslContext);
            builder.setSSLHostnameVerifier(allowAllHosts);
        }

        CredentialsProvider credentialsProvider = createCredentialProvider(options);
        if (credentialsProvider != null) {
            builder.setDefaultCredentialsProvider(credentialsProvider).build();
        }

        return builder;
    }

    /**
     * Creates a new {@link HttpClientBuilder} for the given options.
     * @return the new http client builder
     */
    public static HttpAsyncClientBuilder createHttpAsyncClientBuilder(Map<String, Object> options) {
        HttpAsyncClientBuilder builder = HttpAsyncClientBuilder.create();

        SSLContext sslContext = createSSLContext(options);
        if (sslContext != null) {
            // Skip verifying hostname
            HostnameVerifier allowAllHosts = new NoopHostnameVerifier();
            builder.setSSLContext(sslContext);
            builder.setSSLHostnameVerifier(allowAllHosts);
        }

        CredentialsProvider credentialsProvider = createCredentialProvider(options);
        if (credentialsProvider != null) {
            builder.setDefaultCredentialsProvider(credentialsProvider).build();
        }

        return builder;
    }

    /**
     * Creates a new {@link CloseableHttpClient} for the given options.
     * @return the new http(s) client
     */
    public static CloseableHttpClient createHttpClient(Map<String, Object> options) {
        return createHttpClientBuilder(options).build();
    }

    /**
     * Remove the slashes at the end of the given string
     * @return string sans slashes
     */
    public static String removeEndSlashes(String path) {
        return Optional.ofNullable(path)
            .filter(str -> str.length() != 0)
            .map(str -> StringUtils.stripEnd(path, FORWARD_SLASH))
            .orElse(path);
    }

    private static String stripQuotesAndBrackets(final String value) {
        String ret = value;

        if (ret.startsWith(OPEN_BRACKET)) {
            ret = ret.substring(1);
        }

        if (ret.startsWith(QUOTE_MARK)) {
            ret = ret.substring(1);
        }

        if (ret.endsWith(CLOSE_BRACKET)) {
            ret = ret.substring(0, ret.length() - 1);
        }

        if (ret.endsWith(QUOTE_MARK)) {
            ret = ret.substring(0, ret.length() - 1);
        }
        return ret;
    }

    private static String stripBrackets(final String value) {
        String ret = value;

        if (ret.startsWith(OPEN_BRACKET)) {
            ret = ret.substring(1);
        }

        if (ret.endsWith(CLOSE_BRACKET)) {
            ret = ret.substring(0, ret.length() - 1);
        }
        return ret;
    }

    private static boolean hasQuotes(final String value) {
        return value.trim().startsWith(QUOTE_MARK) && value.trim().endsWith(QUOTE_MARK);
    }

    private static boolean isNumber(String keyPredicate) {
        Matcher numberOnlyMatcher = NUMBER_ONLY_PATTERN.matcher(keyPredicate);
        return numberOnlyMatcher.matches();
    }

    /**
     * Another method to format key predicates.
     * @param keyPredicate the predicate to be formatted
     * @return the keyPredicate formatted with quotes and brackets
     */
    public static String formatKeyPredicate(final String keyPredicate) {
        String subPredicate = null;

        Matcher kp1Matcher = KEY_PREDICATE_PATTERN.matcher(keyPredicate);
        String keyPredicateToUse = keyPredicate;
        if (kp1Matcher.matches()) {
            keyPredicateToUse = kp1Matcher.group(1);
            subPredicate = kp1Matcher.group(2);
        }

        keyPredicateToUse = stripBrackets(keyPredicateToUse);

        StringBuilder buf = new StringBuilder();
        buf.append(OPEN_BRACKET);

        if (isNumber(keyPredicateToUse)) {
            //
            // if keyPredicate is a number only, it doesn't need quotes
            //
            buf.append(keyPredicateToUse);
        } else if (keyPredicateToUse.contains(EQUALS)) {
            //
            // keyPredicate contains an equals so acting as a filter
            //
            String[] clauses = keyPredicateToUse.split(EQUALS, 2);

            // Strip off brackets on both values
            String keyName = stripQuotesAndBrackets(clauses[0]);
            String keyValue = stripBrackets(clauses[1]);

            // KeyName has no quotes
            buf.append(keyName).append(EQUALS);

            // Check if key value is a number. If not, use quotes.
            if (isNumber(keyValue) || hasQuotes(keyValue)) {
                buf.append(keyValue);
            } else {
                buf.append(QUOTE_MARK).append(keyValue).append(QUOTE_MARK);
            }
        } else if (hasQuotes(keyPredicateToUse)) {
            buf.append(keyPredicateToUse);
        } else {
            buf.append(QUOTE_MARK).append(keyPredicateToUse).append(QUOTE_MARK);
        }

        buf.append(CLOSE_BRACKET);

        if (subPredicate != null) {
            buf.append(FORWARD_SLASH).append(subPredicate);
        }

        return buf.toString();
    }

    /**
     * Reads Edm metadata from given OData service.
     * @param serviceUrl base url of the OData service
     * @param options set of Http client options
     * @return the Edm metadata
     */
    public static Edm readEdm(String serviceUrl, Map<String, Object> options) throws IOException, EdmException, EntityProviderException {
        return doWithServiceResponse(ODataUtil.removeEndSlashes(serviceUrl) + METADATA_ENDPOINT, options, response -> {
            InputStream content = response.getEntity().getContent();
            return EntityProvider.readMetadata(content, false);
        });
    }

    /**
     * Read OData entry for given resource path on OData service.
     * @param edm service metadata
     * @param resourcePath entity set name
     * @param serviceUrl base url of the OData service
     * @param options set of Http client options
     * @return the OData entry
     */
    public static ODataFeed readFeed(Edm edm, String resourcePath, String serviceUrl, Map<String, Object> options) throws IOException, EdmException, EntityProviderException {
        return doWithServiceResponse(serviceUrl, options, response -> {
            EdmEntityContainer entityContainer = edm.getDefaultEntityContainer();

            InputStream content = response.getEntity().getContent();
            return EntityProvider.readFeed(ContentType.APPLICATION_XML.getMimeType(),
                entityContainer.getEntitySet(resourcePath),
                content,
                EntityProviderReadProperties.init().build());
        });
    }

    /**
     * Read OData entry for given resource path on OData service.
     * @param edm service metadata
     * @param resourcePath entity set name
     * @param serviceUrl base url of the OData service
     * @param options set of Http client options
     * @return the OData entry
     */
    public static ODataEntry readEntry(Edm edm, String resourcePath, String serviceUrl, Map<String, Object> options) throws IOException, EdmException, EntityProviderException {
        return doWithServiceResponse(serviceUrl, options, response -> {
            EdmEntityContainer entityContainer = edm.getDefaultEntityContainer();

            InputStream content = response.getEntity().getContent();
            return EntityProvider.readEntry(ContentType.APPLICATION_XML.getMimeType(),
                entityContainer.getEntitySet(resourcePath),
                content,
                EntityProviderReadProperties.init().build());
        });
    }

    /**
     * Performs service call on OData service and provides response to given handler.
     * @param serviceUrl the base OData service URL.
     * @param options optional set of Http client options.
     * @param responseHandler handler extracting data from service response.
     * @param <T> type of extracted response data.
     * @return extracted response data.
     */
    private static <T> T doWithServiceResponse(String serviceUrl, Map<String, Object> options, ResponseHandler<T> responseHandler) throws IOException, EdmException, EntityProviderException {
        HttpGet httpGet = new HttpGet(serviceUrl);
        httpGet.setHeader(HttpHeaders.ACCEPT, ACCEPT_MIME_TYPE);

        try (CloseableHttpClient httpClient = ODataUtil.createHttpClient(options);
             CloseableHttpResponse response = httpClient.execute(httpGet)) {
            if (response.getStatusLine().getStatusCode() == 401) {
                throw new IllegalStateException(String.format("Failed to authenticate to OData service. Return code: %s - %s",
                    response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
            } else if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() > 299) {
                throw new IllegalStateException(String.format("Failed to call OData service. Return code: %s - %s",
                    response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
            }

            return responseHandler.handle(response);
        }
    }
}
