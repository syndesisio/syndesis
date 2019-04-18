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
package io.syndesis.connector.odata.customizer.json;

import java.io.IOException;
import org.apache.olingo.client.api.domain.ClientEnumValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.syndesis.common.util.StringConstants;

public class ClientEnumValueSerializer
    extends StdSerializer<ClientEnumValue> implements StringConstants {

    private static final long serialVersionUID = 1L;

    public ClientEnumValueSerializer() {
        this(null);
    }

    public ClientEnumValueSerializer(Class<ClientEnumValue> t) {
        super(t);
    }

    @Override
    public Class<ClientEnumValue> handledType() {
        return ClientEnumValue.class;
    }

    @Override
    public void serialize(ClientEnumValue value, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeString(value.getValue());
    }
}
