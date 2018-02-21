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
package io.syndesis.connector.http4;

import java.net.UnknownHostException;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.ComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.DefaultComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorHelper;
import org.apache.camel.component.http4.CompositeHttpConfigurer;
import org.apache.camel.component.http4.HttpUtil;
import org.apache.camel.http.common.HttpHelper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class Http4ComponentVerifierExtension extends DefaultComponentVerifierExtension {

    public Http4ComponentVerifierExtension(String defaultScheme, CamelContext context) {
        super(defaultScheme, context);
    }

    @Override
    protected Result verifyParameters(Map<String, Object> parameters) {

        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.PARAMETERS)
                .error(ResultErrorHelper.requiresOption("httpUri", parameters));

        return builder.build();

    }

    @Override
    @SuppressWarnings({ "PMD.AvoidCatchingGenericException", "PMD.CyclomaticComplexity" })
    protected Result verifyConnectivity(Map<String, Object> parameters) {

        // Default is success
        final ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK,
                ComponentVerifierExtension.Scope.CONNECTIVITY);

        String httpUri = (String) parameters.get("httpUri");

        try {
            CloseableHttpClient httpclient = createHttpClient(parameters);
            HttpUriRequest request = new HttpGet(httpUri);

            try (CloseableHttpResponse response = httpclient.execute(request)) {
                int code = response.getStatusLine().getStatusCode();
                String okCodes = "200-299";

                if (!HttpHelper.isStatusCodeOk(code, okCodes)) {
                    if (code == 401) {
                        builder.error(ResultErrorBuilder.withHttpCode(code)
                                .description(response.getStatusLine().getReasonPhrase()).build());
                    } else if (code >= 300 && code < 400) {
                        // redirect
                        builder.error(ResultErrorBuilder.withHttpCode(code)
                                .description(response.getStatusLine().getReasonPhrase()).parameterKey("httpUri")
                                .detail(VerificationError.HttpAttribute.HTTP_REDIRECT,
                                        () -> HttpUtil.responseHeaderValue(response, "location"))
                                .build());
                    } else if (code >= 400) {
                        // generic http error
                        builder.error(ResultErrorBuilder.withHttpCode(code)
                                .description(response.getStatusLine().getReasonPhrase()).build());
                    }
                }
            } catch (UnknownHostException e) {
                builder.error(ResultErrorBuilder.withException(e).parameterKey("httpUri").build());
            }
        } catch (Exception e) {
            builder.error(ResultErrorBuilder.withException(e).build());
        }

        return builder.build();
    }

    @SuppressWarnings({ "PMD.SignatureDeclareThrowsException", "PMD.UnusedFormalParameter" })
    private CloseableHttpClient createHttpClient(Map<String, Object> parameters) throws Exception {
        CompositeHttpConfigurer configurer = new CompositeHttpConfigurer();

        HttpClientBuilder builder = HttpClientBuilder.create();
        configurer.configureHttpClient(builder);

        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();

        return builder.setDefaultRequestConfig(requestConfigBuilder.build()).build();
    }
}
