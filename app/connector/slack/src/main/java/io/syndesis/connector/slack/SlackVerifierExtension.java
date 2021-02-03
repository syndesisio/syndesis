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
package io.syndesis.connector.slack;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.syndesis.connector.support.util.ConnectorOptions;

import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.verifier.DefaultComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;
import org.apache.camel.util.ObjectHelper;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

public class SlackVerifierExtension extends DefaultComponentVerifierExtension implements WithHttpClient {

    public static final String TOKEN = "token";
    public static final String WEBHOOK_URL = "webhookUrl";

    protected SlackVerifierExtension(final String defaultScheme, final CamelContext context) {
        super(defaultScheme, context);
    }

    void testToken(final CloseableHttpClient client, final String token, final ResultBuilder builder) {
        try {
            final HttpPost httpPost = new HttpPost("https://slack.com/api/conversations.list");

            final List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(TOKEN, token));
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            final JSONObject responseJson = client.execute(httpPost, SlackResponseHandler.INSTANCE);

            final Object ok = responseJson.get("ok");

            if (ok != null && ok.equals(false)) {
                builder.error(
                    ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.AUTHENTICATION, "Invalid token").parameterKey(TOKEN)
                        .build());
            }
        } catch (final Exception e) {
            builder.error(ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.AUTHENTICATION, "Invalid token").parameterKey(TOKEN)
                .detail(VerificationError.ExceptionAttribute.EXCEPTION_INSTANCE, e)
                .detail(VerificationError.ExceptionAttribute.EXCEPTION_CLASS, e.getClass().getName())
                .build());
        }
    }

    void testWebhookUrl(final CloseableHttpClient client, final String webhookUrl, final ResultBuilder builder) {
        final HttpPost httpPost = new HttpPost(webhookUrl);

        // Set the post body
        final String json = slackMessage("Test connection");
        final StringEntity body = new StringEntity(json, StandardCharsets.US_ASCII);

        httpPost.setEntity(body);
        // Do the post
        try (CloseableHttpResponse response = client.execute(httpPost)) {
            // 2xx is OK, anything else we regard as failure
            if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() > 299) {
                builder
                    .error(ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.AUTHENTICATION, "Invalid webhookUrl")
                        .parameterKey(WEBHOOK_URL).build());
            }
        } catch (final Exception e) {
            builder.error(ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.AUTHENTICATION, "Invalid webhookUrl")
                .parameterKey(WEBHOOK_URL)
                .detail(VerificationError.ExceptionAttribute.EXCEPTION_INSTANCE, e)
                .detail(VerificationError.ExceptionAttribute.EXCEPTION_CLASS, e.getClass().getName())
                .build());
        }
    }

    private void verifyCredentials(final ResultBuilder builder, final Map<String, Object> parameters) {
        final String webhookUrl = ConnectorOptions.extractOption(parameters, WEBHOOK_URL);
        final String token = ConnectorOptions.extractOption(parameters, TOKEN);

        if (ObjectHelper.isEmpty(webhookUrl) && ObjectHelper.isEmpty(token)) {
            return;
        }

        try (CloseableHttpClient client = createHttpClient()) {
            if (ObjectHelper.isNotEmpty(webhookUrl)) {
                testWebhookUrl(client, webhookUrl, builder);
            }

            if (ObjectHelper.isNotEmpty(token)) {
                testToken(client, token, builder);
            }
        } catch (final IOException e) {
            throw new UncheckedIOException("Unable to close HTTP client", e);
        }
    }

    protected String slackMessage(final String message) {
        final Map<String, Object> jsonMap = new HashMap<>();

        // Put the values in a map
        jsonMap.put("text", message);

        // Generate a JSONObject
        final JSONObject jsonObject = new JSONObject(jsonMap);

        // Return the string based on the JSON Object
        return JSONObject.toJSONString(jsonObject);
    }

    // *********************************
    // Connectivity validation
    // *********************************
    @Override
    protected Result verifyConnectivity(final Map<String, Object> parameters) {
        return ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.CONNECTIVITY).error(parameters, this::verifyCredentials).build();
    }

    // *********************************
    // Parameters validation
    // *********************************
    @Override
    protected Result verifyParameters(final Map<String, Object> parameters) {
        final ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.PARAMETERS);

        if (ObjectHelper.isEmpty(ConnectorOptions.extractOption(parameters, TOKEN))
            || ObjectHelper.isEmpty(ConnectorOptions.extractOption(parameters, WEBHOOK_URL))) {
            builder.error(ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.GENERIC,
                "You must specify a webhookUrl and a token").parameterKey(WEBHOOK_URL).parameterKey(TOKEN).build());
        }
        return builder.build();
    }

}
