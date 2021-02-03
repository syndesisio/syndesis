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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.syndesis.connector.support.util.ConnectorOptions;

import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.metadata.AbstractMetaDataExtension;
import org.apache.camel.component.extension.metadata.MetaDataBuilder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.singletonList;

public class SlackMetaDataExtension extends AbstractMetaDataExtension implements WithHttpClient {

    private static final Logger LOG = LoggerFactory.getLogger(SlackMetaDataExtension.class);

    SlackMetaDataExtension(final CamelContext context) {
        super(context);
    }

    @Override
    public Optional<MetaData> meta(final Map<String, Object> parameters) {
        final String token = ConnectorOptions.extractOption(parameters, "token");

        if (token == null) {
            LOG.warn("Unable to fetch list of channels, token not provided");
            return Optional.empty();
        }

        final JSONObject conversationList = conversationList(token);

        @SuppressWarnings("unchecked")
        final List<JSONObject> channels = (List<JSONObject>) conversationList.get("channels");

        final Set<String> channelNames = channels.stream()
            .map(c -> c.get("name"))
            .filter(Objects::nonNull)
            .map(String.class::cast)
            .collect(Collectors.toSet());

        @SuppressWarnings("resource")
        final MetaData metaData = MetaDataBuilder.on(getCamelContext())
            .withAttribute(MetaData.CONTENT_TYPE, "text/plain")
            .withAttribute(MetaData.JAVA_TYPE, String.class)
            .withPayload(channelNames)
            .build();

        return Optional.of(metaData);
    }

    JSONObject conversationList(final String token) {
        LOG.debug("Retrieving channels for connection to slack with token {}", token);

        try (CloseableHttpClient client = createHttpClient()) {
            final HttpPost httpPost = new HttpPost("https://slack.com/api/conversations.list");
            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.addHeader("Accept", "application/json");

            final BasicNameValuePair tokenParameter = new BasicNameValuePair("token", token);
            final UrlEncodedFormEntity form = new UrlEncodedFormEntity(singletonList(tokenParameter));
            httpPost.setEntity(form);

            return client.execute(httpPost, SlackResponseHandler.INSTANCE);
        } catch (final IOException e) {
            LOG.error("Unable to list channels", e);
            throw new IllegalStateException(
                "Get information about channels failed with token has failed.", e);
        }
    }
}
