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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.metadata.AbstractMetaDataExtension;
import org.apache.camel.component.extension.metadata.MetaDataBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.syndesis.connector.support.util.ConnectorOptions;
import static io.syndesis.connector.slack.utils.SlackUtils.readResponse;

public class SlackMetaDataExtension extends AbstractMetaDataExtension {

    private static final Logger LOG = LoggerFactory.getLogger(SlackMetaDataExtension.class);

    SlackMetaDataExtension(CamelContext context) {
        super(context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<MetaData> meta(Map<String, Object> parameters) {
        final String token = ConnectorOptions.extractOption(parameters, "token");

        if (token != null) {
            LOG.debug("Retrieving channels for connection to slack with token {}", token);
            HttpClient client = HttpClientBuilder.create().useSystemProperties().build();
            HttpPost httpPost = new HttpPost("https://slack.com/api/channels.list");

            List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
            params.add(new BasicNameValuePair("token", token));
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(params));

                HttpResponse response = client.execute(httpPost);

                String jsonString = readResponse(response);
                JSONParser parser = new JSONParser();

                JSONObject c = (JSONObject) parser.parse(jsonString);
                List<Object> list = (List<Object>) c.get("channels");
                Set<String> setChannels = new HashSet<String>();
                Iterator<Object> it = list.iterator();
                while (it.hasNext()) {
                    Object object = it.next();
                    JSONObject singleChannel = (JSONObject) object;
                    if (singleChannel.get("name") != null) {
                        setChannels.add((String) singleChannel.get("name"));
                    }
                }

            return Optional
                        .of(MetaDataBuilder.on(getCamelContext()).withAttribute(MetaData.CONTENT_TYPE, "text/plain")
                                .withAttribute(MetaData.JAVA_TYPE, String.class).withPayload(setChannels).build());
            } catch (Exception e) {
                throw new IllegalStateException(
                    "Get information about channels failed with token " + token + " has failed.", e);
            }
        } else {
            return Optional.empty();
        }
    }
}
