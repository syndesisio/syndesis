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
package io.syndesis.dv.openshift;

import static okhttp3.ConnectionSpec.CLEARTEXT;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.internal.SSLUtils;
import io.fabric8.kubernetes.client.utils.BackwardsCompatibilityInterceptor;
import io.fabric8.kubernetes.client.utils.ImpersonatorInterceptor;
import io.fabric8.kubernetes.client.utils.IpAddressMatcher;
import io.fabric8.kubernetes.client.utils.Utils;
import okhttp3.ConnectionSpec;
import okhttp3.Credentials;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

// This class is copied from the Kubernetes client to send a custom httpClientBuilder in constructor
public final class HttpClientUtils {

    private static final Pattern VALID_IPV4_PATTERN = Pattern.compile(
        "(http:\\/\\/|https:\\/\\/)?(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])(\\/[0-9]\\d|1[0-9]\\d|2[0-9]\\d|3[0-2]\\d)?",
        Pattern.CASE_INSENSITIVE);

    private HttpClientUtils() {
        // utility class
    }

    public static OkHttpClient createHttpClient(final Config config, OkHttpClient.Builder httpClientBuilder) {
        return createHttpClient(config, (b) -> {
        }, httpClientBuilder);
    }

    public static OkHttpClient createHttpClientForMockServer(final Config config) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        return createHttpClient(config, b -> b.protocols(Collections.singletonList(Protocol.HTTP_1_1)), httpClientBuilder);
    }

    @SuppressFBWarnings("REC_CATCH_EXCEPTION")
    @SuppressWarnings({"PMD.NPathComplexity", "PMD.ExcessiveMethodLength", "PMD.CyclomaticComplexity"}) // TODO refactor
    private static OkHttpClient createHttpClient(final Config config, final Consumer<OkHttpClient.Builder> additionalConfig,
        OkHttpClient.Builder httpClientBuilder) {
        try {
            // Follow any redirects
            httpClientBuilder.followRedirects(true);
            httpClientBuilder.followSslRedirects(true);

            if (config.isTrustCerts() || config.isDisableHostnameVerification()) {
                httpClientBuilder.hostnameVerifier((s, sslSession) -> true);
            }

            TrustManager[] trustManagers = SSLUtils.trustManagers(config);
            KeyManager[] keyManagers = SSLUtils.keyManagers(config);

            X509TrustManager trustManager = null;
            if (trustManagers != null && trustManagers.length == 1) {
                trustManager = (X509TrustManager) trustManagers[0];
            }

            try {
                SSLContext sslContext = SSLUtils.sslContext(keyManagers, trustManagers);
                if (trustManager != null) {
                    httpClientBuilder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
                } else {
                    // trustManager can be null, and sslSocketFactory throws NPE in that case
                    httpClientBuilder.sslSocketFactory(sslContext.getSocketFactory());
                }
            } catch (GeneralSecurityException e) {
                throw new IllegalStateException("Unable to setup TLS", e);
            }

            httpClientBuilder.addInterceptor(chain -> {
                if (Utils.isNotNullOrEmpty(config.getUsername()) && Utils.isNotNullOrEmpty(config.getPassword())) {
                    Request authReq = chain.request().newBuilder().addHeader("Authorization", Credentials.basic(config.getUsername(), config.getPassword()))
                        .build();
                    return chain.proceed(authReq);
                } else if (Utils.isNotNullOrEmpty(config.getOauthToken())) {
                    Request authReq = chain.request().newBuilder().addHeader("Authorization", "Bearer " + config.getOauthToken()).build();
                    return chain.proceed(authReq);
                }

                Request request = chain.request();
                return chain.proceed(request);
            }).addInterceptor(new ImpersonatorInterceptor(config))
                .addInterceptor(new BackwardsCompatibilityInterceptor());

            Logger reqLogger = LoggerFactory.getLogger(HttpLoggingInterceptor.class);
            if (reqLogger.isTraceEnabled()) {
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                httpClientBuilder.addNetworkInterceptor(loggingInterceptor);
            }

            if (config.getConnectionTimeout() > 0) {
                httpClientBuilder.connectTimeout(Duration.ofMillis(config.getConnectionTimeout()));
            }

            if (config.getRequestTimeout() > 0) {
                httpClientBuilder.readTimeout(Duration.ofMillis(config.getRequestTimeout()));
            }

            if (config.getWebsocketPingInterval() > 0) {
                httpClientBuilder.pingInterval(Duration.ofMillis(config.getWebsocketPingInterval()));
            }

            if (config.getMaxConcurrentRequestsPerHost() > 0) {
                Dispatcher dispatcher = new Dispatcher();
                dispatcher.setMaxRequests(config.getMaxConcurrentRequests());
                dispatcher.setMaxRequestsPerHost(config.getMaxConcurrentRequestsPerHost());
                httpClientBuilder.dispatcher(dispatcher);
            }

            // Only check proxy if it's a full URL with protocol
            if (config.getMasterUrl().toLowerCase(Locale.US).startsWith(Config.HTTP_PROTOCOL_PREFIX) || config.getMasterUrl().startsWith(Config.HTTPS_PROTOCOL_PREFIX)) {
                try {
                    URL proxyUrl = getProxyUrl(config);
                    if (proxyUrl != null) {
                        httpClientBuilder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyUrl.getHost(), proxyUrl.getPort())));

                        if (config.getProxyUsername() != null) {
                            httpClientBuilder.proxyAuthenticator((route, response) -> {

                                String credential = Credentials.basic(config.getProxyUsername(), config.getProxyPassword());
                                return response.request().newBuilder().header("Proxy-Authorization", credential).build();
                            });
                        }
                    }

                } catch (MalformedURLException e) {
                    throw new KubernetesClientException("Invalid proxy server configuration", e);
                }
            }

            if (config.getUserAgent() != null && !config.getUserAgent().isEmpty()) {
                httpClientBuilder.addNetworkInterceptor(chain -> {
                    Request agent = chain.request().newBuilder().header("User-Agent", config.getUserAgent()).build();
                    return chain.proceed(agent);
                });
            }

            if (config.getTlsVersions() != null && config.getTlsVersions().length > 0) {
                ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(config.getTlsVersions())
                    .build();
                httpClientBuilder.connectionSpecs(Arrays.asList(spec, CLEARTEXT));
            }

            if (config.isHttp2Disable()) {
                httpClientBuilder.protocols(Collections.singletonList(Protocol.HTTP_1_1));
            }

            if (additionalConfig != null) {
                additionalConfig.accept(httpClientBuilder);
            }

            return httpClientBuilder.build();
        } catch (Exception e) {
            throw KubernetesClientException.launderThrowable(e);
        }
    }

    private static URL getProxyUrl(Config config) throws MalformedURLException {
        URL master = new URL(config.getMasterUrl());
        String host = master.getHost();
        if (config.getNoProxy() != null) {
            for (String noProxy : config.getNoProxy()) {
                if (isIpAddress(noProxy)) {
                    if (new IpAddressMatcher(noProxy).matches(host)) {
                        return null;
                    }
                } else {
                    if (host.contains(noProxy)) {
                        return null;
                    }
                }
            }
        }
        String proxy = config.getHttpsProxy();
        if (master.getProtocol().equals("http")) {
            proxy = config.getHttpProxy();
        }
        if (proxy != null) {
            return new URL(proxy);
        }
        return null;
    }

    private static boolean isIpAddress(String ipAddress) {
        Matcher ipMatcher = VALID_IPV4_PATTERN.matcher(ipAddress);
        return ipMatcher.matches();
    }
}
