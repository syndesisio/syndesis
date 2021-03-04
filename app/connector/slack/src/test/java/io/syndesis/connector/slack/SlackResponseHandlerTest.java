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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SlackResponseHandlerTest {

    @Test
    public void shouldParseJsonResponses() throws HttpResponseException, IOException {
        final ResponseHandler<JSONObject> handler = SlackResponseHandler.INSTANCE;

        final HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
        final BasicHttpEntity entity = new BasicHttpEntity();
        final byte[] jsonBytes = "{\"hello\":\"world\"}".getBytes(StandardCharsets.US_ASCII);
        entity.setContent(new ByteArrayInputStream(jsonBytes));
        response.setEntity(entity);
        final JSONObject object = handler.handleResponse(response);

        assertThat((Object) object).isNotNull();
        assertThat(object.get("hello")).isEqualTo("world");
    }
}
