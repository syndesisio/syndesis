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
package io.syndesis.integration.runtime.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import io.syndesis.common.util.SyndesisServerException;

import java.io.StringWriter;

/**
 * Simple utility class used to construct a json encoded object.
 */
public final class JsonSupport {

    private JsonSupport(){
    }

    public static String toJsonObject(Object... fields) {
        try {
            StringWriter w = new StringWriter();
            JsonGenerator jg = new JsonFactory().createGenerator(w);

            jg.writeStartObject();
            for (int i = 0; i + 1 < fields.length; i += 2) {
                Object key = fields[i];
                Object value = fields[i+1];
                if (key != null && value != null) {
                    jg.writeFieldName(key.toString());
                    if (value instanceof Boolean ) {
                        jg.writeBoolean((Boolean) value);
                    } else if (value instanceof Number ) {
                        jg.writeNumber(((Number) value).longValue());
                    } else {
                        jg.writeString(value.toString());
                    }
                }
            }
            jg.writeEndObject();

            jg.close();
            return w.toString();
        } catch (java.io.IOException e) {
            throw new SyndesisServerException(e);
        }
    }

}
