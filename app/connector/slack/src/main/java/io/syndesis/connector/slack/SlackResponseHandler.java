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
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public final class SlackResponseHandler extends AbstractResponseHandler<JSONObject> {

    public static final ResponseHandler<JSONObject> INSTANCE = new SlackResponseHandler();

    @Override
    public JSONObject handleEntity(final HttpEntity entity) throws IOException {
        final JSONParser parser = new JSONParser();

        final Charset charset = determineCharset(entity);
        try (InputStreamReader reader = new InputStreamReader(entity.getContent(), charset)) {
            return (JSONObject) parser.parse(reader);
        } catch (final ParseException e) {
            throw new IOException("Unable to parse response JSON", e);
        } finally {
            EntityUtils.consumeQuietly(entity);
        }
    }

    static Charset determineCharset(final HttpEntity entity) {
        final ContentType contentType = ContentType.get(entity);

        if (contentType == null) {
            // RFC 4329, sec 4.2, best I could find, we don't want to sniff for
            // charset
            return StandardCharsets.UTF_8;
        }

        return contentType.getCharset();
    }

}
