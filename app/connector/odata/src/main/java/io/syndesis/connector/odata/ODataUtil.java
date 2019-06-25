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

import java.net.URI;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.olingo.client.api.http.HttpClientFactory;
import org.apache.olingo.commons.api.http.HttpMethod;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.connector.support.util.KeyStoreHelper;

@SuppressWarnings("PMD")
public class ODataUtil implements ODataConstants {

    public static class ODataHttpClientFactory implements HttpClientFactory {

        private final Map<String, Object> options;

        public ODataHttpClientFactory(Map<String, Object> options) {
            this.options = options;
        }

        @Override
        public HttpClient create(HttpMethod method, URI uri) {
            try {
                return createHttpClient(options);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        @SuppressWarnings( "deprecation" )
        @Override
        public void close(HttpClient httpClient) {
            httpClient.getConnectionManager().shutdown();
        }

    }

    private static final Pattern NUMBER_ONLY_PATTERN = Pattern.compile("-?\\d+");

    private static final Pattern KEY_PREDICATE_PATTERN = Pattern.compile("\\(?'?(.+?)'?\\)?\\/(.+)");

    /**
     * @param url
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

    private static KeyStore createKeyStore(Map<String, Object> options) throws Exception {
        String certContent = ConnectorOptions.extractOption(options, SERVER_CERTIFICATE);
        if (ObjectHelper.isEmpty(certContent)) {
            return KeyStoreHelper.defaultKeyStore();
        }

        return KeyStoreHelper.createKeyStoreWithCustomCertificate("odata", certContent);
    }

    public static SSLContext createSSLContext(Map<String, Object> options) throws Exception {
        String serviceUrl = ConnectorOptions.extractOption(options, SERVICE_URI);
        if (! isServiceSSL(serviceUrl)) {
            return null;
        }

        KeyStore keyStore = createKeyStore(options);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());
        return sslContext;
    }

    private static CredentialsProvider createCredentialProvider(Map<String, Object> options) {
        String basicUser = ConnectorOptions.extractOption(options, BASIC_USER_NAME);
        String basicPswd = ConnectorOptions.extractOption(options, BASIC_PASSWORD);

        if (ObjectHelper.isEmpty(basicUser)) {
            return null;
        }

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(basicUser, basicPswd));
        return credentialsProvider;
    }

    /**
     * Creates a new {@link HttpClientBuilder} for the given options.
     *
     * @param options
     *
     * @return the new http client builder
     *
     * @throws Exception
     */
    public static HttpClientBuilder createHttpClientBuilder(Map<String, Object> options)
                                                       throws Exception {
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
     *
     * @param options
     *
     * @return the new http client builder
     *
     * @throws Exception
     */
    public static HttpAsyncClientBuilder createHttpAsyncClientBuilder(Map<String, Object> options)
                                                       throws Exception {
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
     * @param options
     *
     * @return the new http(s) client
     *
     * @throws Exception
     */
    public static CloseableHttpClient createHttpClient(Map<String, Object> options)
                                                                   throws Exception {
        return createHttpClientBuilder(options).build();
    }

    public static HttpClientFactory newHttpFactory(Map<String, Object> options) {
        return new ODataHttpClientFactory(options);
    }

    /**
     * Remove the slashes at the end of the given string
     *
     * @param path
     * @return string sans slashes
     */
    public static String removeEndSlashes(String path) {
        return Optional.ofNullable(path)
            .filter(str -> str.length() != 0)
            .map(str -> StringUtils.stripEnd(path, FORWARD_SLASH))
            .orElse(path);
    }

    private static String stripQuotesAndBrackets(String value) {
        if (value.startsWith(OPEN_BRACKET)) {
            value = value.substring(1);
        }

        if (value.startsWith(QUOTE_MARK)) {
            value = value.substring(1);
        }

        if (value.endsWith(CLOSE_BRACKET)) {
            value = value.substring(0, value.length() - 1);
        }

        if (value.endsWith(QUOTE_MARK)) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private static boolean isNumber(String keyPredicate) {
        Matcher numberOnlyMatcher = NUMBER_ONLY_PATTERN.matcher(keyPredicate);
        return numberOnlyMatcher.matches();
    }

    /**
     * @param keyPredicate the predicate to be formatted
     * @param includeBrackets whether brackets should be added around the key predicate string
     * @return the keyPredicate formatted with quotes and brackets
     */
    @SuppressWarnings("PMD")
    public static String formatKeyPredicate(String keyPredicate, boolean includeBrackets) {
        String subPredicate = null;

        Matcher kp1Matcher = KEY_PREDICATE_PATTERN.matcher(keyPredicate);
        if (kp1Matcher.matches()) {
            keyPredicate = kp1Matcher.group(1);
            subPredicate = kp1Matcher.group(2);
        }

        keyPredicate = stripQuotesAndBrackets(keyPredicate);

        StringBuilder buf = new StringBuilder();
        if (includeBrackets || subPredicate != null) {
            buf.append(OPEN_BRACKET);
        }

        if (isNumber(keyPredicate)) {
            //
            // if keyPredicate is a number only, it doesn't need quotes
            //
            buf.append(keyPredicate);
        } else if (keyPredicate.contains(EQUALS)) {
            //
            // keyPredicate contains an equals so acting as a filter
            //
            String[] clauses = keyPredicate.split(EQUALS, 2);

            // Strip off quotes on both values
            String keyName = stripQuotesAndBrackets(clauses[0]);
            String keyValue = stripQuotesAndBrackets(clauses[1]);

            // KeyName has no quotes
            buf.append(keyName).append(EQUALS);

            // Check if key value is a number. If not, use quotes.
            if (isNumber(keyValue)) {
                buf.append(keyValue);
            } else {
                buf.append(QUOTE_MARK).append(keyValue).append(QUOTE_MARK);
            }

        } else {
            buf.append(QUOTE_MARK).append(keyPredicate).append(QUOTE_MARK);
        }

        if (includeBrackets || subPredicate != null) {
            buf.append(CLOSE_BRACKET);
        }

        if (subPredicate != null) {
            buf.append(FORWARD_SLASH).append(subPredicate);
        }

        return buf.toString();
    }
}
