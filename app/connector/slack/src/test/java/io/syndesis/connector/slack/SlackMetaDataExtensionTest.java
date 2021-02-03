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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.component.extension.MetaDataExtension.MetaData;
import org.json.simple.JSONObject;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.iterable;
import static org.mockito.Mockito.mock;

public class SlackMetaDataExtensionTest {

    @Test
    public void shouldNotFetchChannelListWithoutToken() {
        @SuppressWarnings("resource")
        final CamelContext context = mock(CamelContext.class);
        final SlackMetaDataExtension extension = new SlackMetaDataExtension(context);

        final Optional<MetaDataExtension.MetaData> meta = extension.meta(Collections.emptyMap());

        assertThat(meta).isEmpty();
    }

    @Test
    public void shouldRetrieveChannelList() {
        @SuppressWarnings("resource")
        final CamelContext context = mock(CamelContext.class);
        final SlackMetaDataExtension extension = new SlackMetaDataExtension(context) {
            @SuppressWarnings("unchecked")
            @Override
            JSONObject conversationList(final String token) {
                final JSONObject json = new JSONObject();
                final List<JSONObject> channels = Arrays.asList(channelJson("channel1"), channelJson("channel2"));

                json.put("channels", channels);

                return json;
            }
        };

        final Map<String, Object> properties = new HashMap<>();
        properties.put("token", "token");

        final Optional<MetaDataExtension.MetaData> maybeMeta = extension.meta(properties);

        assertThat(maybeMeta).isPresent();
        final MetaData metaData = maybeMeta.get();

        assertThat(metaData.getPayload()).isInstanceOf(Set.class).asInstanceOf(iterable(String.class)).containsOnly("channel1", "channel2");
        assertThat(metaData.getAttributes()).containsEntry(MetaDataExtension.MetaData.JAVA_TYPE, String.class);
        assertThat(metaData.getAttributes()).containsEntry(MetaDataExtension.MetaData.CONTENT_TYPE, "text/plain");
    }

    JSONObject channelJson(final String name) {
        return new JSONObject(Collections.singletonMap("name", name));
    }

}
