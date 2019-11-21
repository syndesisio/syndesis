/*
 * Copyright (C) 2013 Red Hat, Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

public class SyndesisHttpUtil {

    private SyndesisHttpUtil() {}

    private static CloseableHttpClient buildHttpClient()
            throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        // no verification of host for now.
        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (certificate, authType) -> true)
                .build();

        CloseableHttpClient client = HttpClients.custom().setSSLContext(sslContext)
                .setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
        return client;
    }

    private static void addDefaultHeaders(HttpRequestBase request) {
        request.addHeader("Accept", "application/json");
        request.addHeader("X-Forwarded-User", "user");
        request.addHeader("SYNDESIS-XSRF-TOKEN", "awesome");
        request.addHeader("X-Forwarded-Access-Token", "supersecret");
        request.addHeader("Content-Type", "application/json");
    }

    public static InputStream executeGET(String url) {
        try {
            CloseableHttpClient client = buildHttpClient();
            HttpGet request = new HttpGet(url);
            addDefaultHeaders(request);

            HttpResponse response = client.execute(request);
            ResponseHandler<InputStream> handler = new AbstractResponseHandler<InputStream>(){
                @Override
                public InputStream handleEntity(final HttpEntity entity) throws IOException {
                    return entity.getContent();
                }
            };
            InputStream result = handler.handleResponse(response);
            return result;
        } catch (UnsupportedOperationException | IOException | KeyManagementException | NoSuchAlgorithmException
                | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream executePOST(String url, String payload) {
        try {
            CloseableHttpClient client = buildHttpClient();
            HttpPost request = new HttpPost(url);
            addDefaultHeaders(request);
            request.setEntity(new StringEntity(payload));
            HttpResponse response = client.execute(request);
            ResponseHandler<InputStream> handler = new AbstractResponseHandler<InputStream>(){
                @Override
                public InputStream handleEntity(final HttpEntity entity) throws IOException {
                    return entity.getContent();
                }
            };
            InputStream result = handler.handleResponse(response);
            return result;
        } catch (UnsupportedOperationException | IOException | KeyManagementException | NoSuchAlgorithmException
                | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream executeDELETE(String url) {
        try {
            CloseableHttpClient client = buildHttpClient();
            HttpDelete request = new HttpDelete(url);
            addDefaultHeaders(request);
            HttpResponse response = client.execute(request);
            ResponseHandler<InputStream> handler = new AbstractResponseHandler<InputStream>(){
                @Override
                public InputStream handleEntity(final HttpEntity entity) throws IOException {
                    return entity.getContent();
                }
            };
            InputStream result = handler.handleResponse(response);
            return result;
        } catch (UnsupportedOperationException | IOException | KeyManagementException | NoSuchAlgorithmException
                | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }
}
